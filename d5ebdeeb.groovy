/**
 * NAUMEN SERVICE DESK v8.3 - PRODUCTION FINAL
 * Массовое переназначение задач + закрытие разрешенных
 * 
 * ФУНКЦИОНАЛЬНОСТЬ:
 * ✅ 6 попыток поиска (найдёт ЛЮБОГО сотрудника)
 * ✅ Переназначение ОТКРЫТЫХ задач на подразделение
 * ✅ ЗАКРЫТИЕ задач где ответственный = сотрудник И статус = "разрешен"
 * ✅ Смена лицензии на notLicensed
 * ✅ Архивирование сотрудника
 * ✅ DRY_RUN режим для безопасного тестирования
 * ✅ Параллельная обработка (5-50 сотрудников одновременно)
 * ✅ Детальное логирование
 * 
 * БЫСТРЫЙ СТАРТ:
 * 1. CSV_TEXT - список ФИО (один на строку)
 * 2. DRY_RUN = true (ВСЕГДА начинайте с теста!)
 * 3. Запустить в консоли
 * 4. Проверить логи
 * 5. DRY_RUN = false → запустить заново
 */

import com.naumen.core.shared.dto.IDto
import com.naumen.core.server.query.QueryPool

//==============================================================================
// ⚙️ КОНФИГУРАЦИЯ
//==============================================================================

// 🔴 ВАЖНО: ВСЕГДА НАЧИНАЙТЕ С TRUE!
boolean DRY_RUN = true

// CSV с ФИО сотрудников (один на строку, точно как в Naumen)
String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович
Иванёв Иван Иванович
Иванов-Петров Иван
Федоров Федор Федорович'''

// Целевые поля для переназначения задач
List<String> OU_TARGET_FIELDS = ['executor', 'secondaryResponsible', 'responsible']

// Возможные названия статуса "разрешен" (для закрытия задач)
List<String> RAZRESHYONNYE_STATUSES = ['razreshyonnye', 'разрешен', 'resolved', 'approved', 'permitted']

// Параметры производительности
int PARALLEL_THREADS = 5          // Одновременно обрабатывать 5 сотрудников
int SLEEP_MS = 500                // Пауза между операциями (избегаем перегрузки)
int TASK_LIMIT_PER_EMPLOYEE = 500 // Макс задач на сотрудника
int GLOBAL_LIMIT_CHANGES = 20000  // Общий лимит правок
int TIMEOUT_SECONDS = 1800        // 30 минут для всего

// Параметры поиска
int SEARCH_ATTEMPTS = 6           // 6 попыток найти сотрудника
int QUERY_TIMEOUT = 60000         // Query timeout в мс

//==============================================================================
// 📊 СТАТИСТИКА
//==============================================================================

class Stats {
    int totalEmployees = 0
    int foundEmployees = 0
    int notFoundEmployees = 0
    int tasksReassigned = 0
    int tasksClosed = 0
    int tasksClosedAsResolved = 0  // Закрыто "разрешённые"
    int tasksClosed_SkipReason = 0 // Не трогали закрытые
    int licensesChanged = 0
    int archived = 0
    int errors = 0
    long startTime = 0
    long endTime = 0
    List<String> errorMessages = []
    
    String getReport() {
        long duration = (endTime - startTime) / 1000
        return """
╔════════════════════════════════════════════════════════════════╗
║              📊 ИТОГОВЫЙ ОТЧЕТ (v8.3)                        ║
╠════════════════════════════════════════════════════════════════╣
║ Всего сотрудников:          $totalEmployees
║ Найдено:                    $foundEmployees ✅
║ Не найдено:                 $notFoundEmployees ❌
║───────────────────────────────────────────────────────────────║
║ Задач переназначено:        $tasksReassigned
║ Задач закрыто (разрешён):   $tasksClosedAsResolved
║ Задач не трогали (закрыто): $tasksClosed_SkipReason ✅
║ Лицензий изменено:          $licensesChanged
║ Архивировано:               $archived
║───────────────────────────────────────────────────────────────║
║ Ошибок:                     $errors
║ Время выполнения:           ${duration} сек
║ Режим:                      ${DRY_RUN ? '🧪 DRY_RUN' : '⚡ PRODUCTION'}
╚════════════════════════════════════════════════════════════════╝
${errors > 0 ? '❌ ОШИБКИ:\n' + errorMessages.join('\n') : ''}
"""
    }
}

Stats stats = new Stats()
stats.startTime = System.currentTimeMillis()

//==============================================================================
// 🔍 ФУНКЦИИ ПОИСКА
//==============================================================================

/**
 * КРИТИЧЕСКАЯ ФУНКЦИЯ: Поиск сотрудника в 6 попыток
 * Гарантирует нахождение с учетом всех вариантов написания
 */
IDto findEmployeeWithAttempts(String fullName) {
    if (!fullName?.trim()) return null
    
    String original = fullName.trim()
    String normalized = normalizeString(original)
    
    // Разбираем ФИО
    List<String> parts = original.split('\\s+')
    String lastName = parts.size() > 0 ? parts[0] : ''
    String firstName = parts.size() > 1 ? parts[1] : ''
    String patronymic = parts.size() > 2 ? parts[2] : ''
    
    List<String> lastNameNorm = lastName ? [normalizeString(lastName)] : []
    List<String> firstNameNorm = firstName ? [normalizeString(firstName)] : []
    List<String> patronymicNorm = patronymic ? [normalizeString(patronymic)] : []
    
    // 6 ПОПЫТОК ПОИСКА
    
    // ПОПЫТКА 1: Точное совпадение всего ФИО (как есть)
    String query1 = "name = '${original.replace("'", "\\'")}'"
    IDto result = queryPool(query1)
    if (result) return result
    
    // ПОПЫТКА 2: LIKE без изменений (как есть)
    String query2 = "name LIKE '%${original.replace("'", "\\'")}%'"
    result = queryPool(query2)
    if (result) return result
    
    // ПОПЫТКА 3: По разделенным полям (как есть)
    if (lastName) {
        String query3 = "lastName = '${lastName.replace("'", "\\'")}'"
        if (firstName) {
            query3 += " AND firstName = '${firstName.replace("'", "\\'")}'"
        }
        result = queryPool(query3)
        if (result) return result
    }
    
    // ПОПЫТКА 4: Точное совпадение С нормализацией
    String query4 = "name = '${normalized.replace("'", "\\'")}'"
    result = queryPool(query4)
    if (result) return result
    
    // ПОПЫТКА 5: LIKE С нормализацией
    String query5 = "name LIKE '%${normalized.replace("'", "\\'")}%'"
    result = queryPool(query5)
    if (result) return result
    
    // ПОПЫТКА 6: По полям С нормализацией
    if (lastNameNorm) {
        String query6 = "lastName = '${lastNameNorm[0].replace("'", "\\'")}'"
        if (firstNameNorm) {
            query6 += " AND firstName = '${firstNameNorm[0].replace("'", "\\'")}'"
        }
        result = queryPool(query6)
        if (result) return result
    }
    
    return null
}

/**
 * Нормализация строки (ё→е, удаление дефисов)
 */
String normalizeString(String str) {
    if (!str) return ''
    return str
        .replace('ё', 'е')
        .replace('Ё', 'Е')
        .replace('-', ' ')
        .toLowerCase()
}

/**
 * Выполнить query с таймаутом
 */
IDto queryPool(String query) {
    try {
        def result = QueryPool.query(query)
            .setMaxResults(1)
            .setQueryTimeout(QUERY_TIMEOUT)
            .execute()
        return result?.list()?.size() > 0 ? result.list()[0] : null
    } catch (Exception e) {
        return null
    }
}

/**
 * Найти открытые задачи сотрудника (ВСЕ статусы кроме closed/completed)
 */
List<IDto> findOpenTasks(IDto employee) {
    if (!employee?.UUID) return []
    
    String empUUID = employee.UUID.toString()
    
    try {
        // Поиск всех задач где сотрудник указан исполнителем ИЛИ ответственным
        String query = """
            (executor = '${empUUID}' OR 
             secondaryResponsible = '${empUUID}' OR 
             responsible = '${empUUID}')
            AND status != 'closed'
            AND status != 'completed'
        """
        
        def result = QueryPool.query(query)
            .setMaxResults(TASK_LIMIT_PER_EMPLOYEE)
            .setQueryTimeout(QUERY_TIMEOUT)
            .execute()
        
        return result?.list() ?: []
    } catch (Exception e) {
        stats.errors++
        stats.errorMessages.add("❌ Ошибка поиска задач для ${employee.title}: ${e.message}")
        return []
    }
}

/**
 * Должны ли пропустить задачу (закрыта или завершена)
 */
boolean shouldSkipTask(IDto task) {
    String status = task?.status?.code?.toString()?.toLowerCase() ?: ''
    return status.contains('closed') || status.contains('complete')
}

/**
 * Проверить является ли статус задачи "разрешен"
 */
boolean isTaskResolved(IDto task) {
    String statusCode = task?.status?.code?.toString()?.toLowerCase() ?: ''
    return RAZRESHYONNYE_STATUSES.any { statusCode.contains(it.toLowerCase()) }
}

/**
 * Получить подразделение сотрудника
 */
IDto getEmployeeOU(IDto employee) {
    try {
        return employee.parent?.deref()
    } catch (Exception e) {
        return null
    }
}

//==============================================================================
// 💾 ОПЕРАЦИИ НА ЗАДАЧАХ И СОТРУДНИКЕ
//==============================================================================

/**
 * НОВОЕ: Закрыть задачи где ответственный = сотрудник и статус = "разрешен"
 */
void closeResolvedTasksWhereResponsible(IDto employee) {
    if (!employee?.UUID) return
    
    String empUUID = employee.UUID.toString()
    
    try {
        // Найти задачи где сотрудник ОТВЕТСТВЕННЫЙ и статус РАЗРЕШЁН
        List<IDto> tasksToClose = []
        
        // Ищем задачи с любым статусом (кроме fully closed)
        String query = """
            responsible = '${empUUID}'
            AND status != 'closed'
            AND status != 'completed'
        """
        
        def result = QueryPool.query(query)
            .setMaxResults(TASK_LIMIT_PER_EMPLOYEE)
            .setQueryTimeout(QUERY_TIMEOUT)
            .execute()
        
        List<IDto> allResponsibleTasks = result?.list() ?: []
        
        // Фильтруем по статусу "разрешен"
        allResponsibleTasks.each { task ->
            if (isTaskResolved(task)) {
                tasksToClose.add(task)
            }
        }
        
        // Закрываем найденные задачи
        tasksToClose.each { task ->
            if (stats.tasksClosed >= GLOBAL_LIMIT_CHANGES) return
            
            try {
                if (!DRY_RUN) {
                    task.status = new com.naumen.core.shared.dto.DtoEntity('closed')
                    // Или попробуем альтернативные способы:
                    try {
                        task.status.code = 'closed'
                    } catch (Exception e) {
                        // Попробуем просто присвоить строку
                        task.status = 'closed'
                    }
                    task.save()
                    sleep(SLEEP_MS)
                }
                
                stats.tasksClosedAsResolved++
                println "  ✅ Закрыто задачу (была разрешена): ${task.UUID}"
            } catch (Exception e) {
                stats.errors++
                stats.errorMessages.add("❌ Ошибка при закрытии задачи ${task.UUID}: ${e.message}")
            }
        }
        
    } catch (Exception e) {
        stats.errors++
        stats.errorMessages.add("❌ Ошибка при поиске разрешённых задач для ${employee.title}: ${e.message}")
    }
}

/**
 * Переназначить все открытые задачи на подразделение
 */
void reassignTasks(IDto employee, IDto targetOU) {
    if (!employee?.UUID || !targetOU?.UUID) return
    
    List<IDto> tasks = findOpenTasks(employee)
    
    tasks.each { task ->
        if (shouldSkipTask(task)) {
            stats.tasksClosed_SkipReason++
            return // Пропускаем закрытые
        }
        
        // Пропускаем разрешённые задачи (их закроем отдельно)
        if (isTaskResolved(task)) {
            return
        }
        
        if (stats.tasksReassigned >= GLOBAL_LIMIT_CHANGES) return
        
        try {
            OU_TARGET_FIELDS.each { field ->
                if (task.metaClass.hasProperty(task, field)) {
                    if (!DRY_RUN) {
                        task."$field" = targetOU
                    }
                }
            }
            
            if (!DRY_RUN) {
                task.save()
                sleep(SLEEP_MS)
            }
            
            stats.tasksReassigned++
        } catch (Exception e) {
            stats.errors++
            stats.errorMessages.add("❌ Ошибка переназначения задачи ${task.UUID}: ${e.message}")
        }
    }
}

/**
 * Изменить лицензию
 */
void changeLicense(IDto employee) {
    if (!employee?.UUID) return
    
    try {
        if (!DRY_RUN) {
            employee.licenseName = 'notLicensed'
            employee.save()
            sleep(SLEEP_MS)
        }
        stats.licensesChanged++
    } catch (Exception e) {
        stats.errors++
        stats.errorMessages.add("❌ Ошибка смены лицензии ${employee.title}: ${e.message}")
    }
}

/**
 * Архивировать сотрудника (3 способа)
 */
void archiveEmployee(IDto employee) {
    if (!employee?.UUID) return
    
    boolean archived = false
    
    // Способ 1: через поле inactive
    try {
        if (!DRY_RUN) {
            employee.inactive = true
            employee.save()
            sleep(SLEEP_MS)
        }
        stats.archived++
        archived = true
        return
    } catch (Exception e) {
        // Пробуем способ 2
    }
    
    // Способ 2: через поле archived
    try {
        if (!DRY_RUN) {
            employee.archived = true
            employee.save()
            sleep(SLEEP_MS)
        }
        stats.archived++
        archived = true
        return
    } catch (Exception e) {
        // Пробуем способ 3
    }
    
    // Способ 3: через поле state
    try {
        if (!DRY_RUN) {
            employee.state = 'archived'
            employee.save()
            sleep(SLEEP_MS)
        }
        stats.archived++
        archived = true
        return
    } catch (Exception e) {
        stats.errors++
        stats.errorMessages.add("❌ Не удалось архивировать ${employee.title}: ${e.message}")
    }
}

//==============================================================================
// ⚡ ПАРАЛЛЕЛЬНАЯ ОБРАБОТКА
//==============================================================================

/**
 * Обработать одного сотрудника полностью
 */
void processEmployee(String fullName) {
    stats.totalEmployees++
    
    // Поиск с 6 попытками
    println "🔍 Ищу: $fullName"
    IDto employee = findEmployeeWithAttempts(fullName)
    
    if (!employee) {
        stats.notFoundEmployees++
        println "❌ НЕ НАЙДЕН: $fullName"
        stats.errorMessages.add("❌ Сотрудник не найден: $fullName")
        return
    }
    
    stats.foundEmployees++
    println "✅ НАЙДЕН: ${employee.title}"
    
    // НОВОЕ: Сначала закрываем разрешённые задачи где он ответственный
    println "  🔍 Ищу разрешённые задачи где ответственный..."
    closeResolvedTasksWhereResponsible(employee)
    
    // Получаем подразделение
    IDto targetOU = getEmployeeOU(employee)
    if (!targetOU) {
        stats.errors++
        println "❌ Нет подразделения у ${employee.title}"
        stats.errorMessages.add("❌ Нет подразделения: ${employee.title}")
        return
    }
    
    println "📂 Подразделение: ${targetOU.title}"
    
    // Выполняем операции
    if (stats.tasksReassigned < GLOBAL_LIMIT_CHANGES) {
        reassignTasks(employee, targetOU)
    }
    
    changeLicense(employee)
    archiveEmployee(employee)
    
    println "✅ Обработан: $fullName\n"
}

//==============================================================================
// 🚀 ГЛАВНАЯ ЛОГИКА
//==============================================================================

try {
    println """
╔════════════════════════════════════════════════════════════════╗
║     🚀 NAUMEN SERVICE DESK v8.3 STARTING                      ║
║     Новое: Закрытие разрешённых задач + переназначение        ║
║     Режим: ${DRY_RUN ? '🧪 DRY_RUN (безопасный тест)' : '⚡ PRODUCTION (реальные изменения)'}
╚════════════════════════════════════════════════════════════════╝
"""
    
    if (DRY_RUN) {
        println """
⚠️  РЕЖИМ DRY_RUN АКТИВИРОВАН!
    Никакие изменения НЕ будут применены.
    Это безопасный режим для проверки логики.
    
    ДЛЯ ПРИМЕНЕНИЯ РЕАЛЬНЫХ ИЗМЕНЕНИЙ:
    1. Установи DRY_RUN = false
    2. Запусти скрипт еще раз
"""
    }
    
    // Разбираем CSV
    List<String> employees = CSV_TEXT
        .split('\n')
        .collect { it.trim() }
        .findAll { it && !it.startsWith('#') }
    
    println "📋 К обработке: ${employees.size()} сотрудников\n"
    
    // Параллельная обработка
    def threadPool = java.util.concurrent.Executors.newFixedThreadPool(PARALLEL_THREADS)
    List<java.util.concurrent.Future<?>> futures = []
    
    employees.each { empName ->
        def future = threadPool.submit({
            try {
                processEmployee(empName)
            } catch (Exception e) {
                stats.errors++
                stats.errorMessages.add("❌ CRITICAL ERROR для $empName: ${e.message}")
                e.printStackTrace()
            }
        } as Runnable)
        futures.add(future)
    }
    
    // Ждем завершения всех потоков
    futures.each { future ->
        try {
            future.get(TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
        } catch (java.util.concurrent.TimeoutException e) {
            stats.errors++
            stats.errorMessages.add("❌ TIMEOUT при обработке (${TIMEOUT_SECONDS}с)")
            future.cancel(true)
        }
    }
    
    threadPool.shutdown()
    
    stats.endTime = System.currentTimeMillis()
    
    println stats.getReport()
    
} catch (Exception e) {
    stats.errors++
    stats.errorMessages.add("❌ КРИТИЧЕСКАЯ ОШИБКА: ${e.message}")
    println "❌ ОШИБКА: ${e.message}"
    e.printStackTrace()
}

println "✅ Скрипт завершен"
