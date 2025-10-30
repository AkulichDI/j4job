/* =====================================================================================
 * СКРИПТ АВТОМАТИЧЕСКОГО ПЕРЕНАЗНАЧЕНИЯ ЗАДАЧ И ЛИЦЕНЗИЙ
 * Версия: 2.0 (Final) для Naumen Service Desk 4.17
 * 
 * Назначение: Переназначение незавершённых задач сотрудников на их OU/Team
 *             и установка лицензии notLicensed по списку ФИО из CSV
 * 
 * ВАЖНО: Всегда начинайте с DRY_RUN = true для тестирования!
 * ===================================================================================== */

/* ===================== ВСТАВЬТЕ CSV ЗДЕСЬ ===================== */
/* Формат: "Фамилия Имя Отчество" в первой колонке, разделитель — запятая.
 * Обрабатывайте порциями по 200-250 строк за раз. */
def CSV_TEXT = $/
Иванов Иван Иванович,любые,другие,данные
Петров Пётр Петрович,можно,игнорировать
/$

/* ===================== ОСНОВНЫЕ ПАРАМЕТРЫ ===================== */
char    DELIM   = ','           // разделитель в CSV
int     FIO_COL = 0             // индекс колонки с ФИО (0 = первая)
boolean DRY_RUN = true          // ОБЯЗАТЕЛЬНО true для первого запуска!
long    SLEEP_MS = 100          // пауза между операциями (мс) для снижения нагрузки

// Приоритет назначения: сначала подразделение, потом команда
List<String> TARGET_PRIORITY = ['ou', 'team']

// Классы объектов для поиска связанных задач
List<String> CLASSES = ['serviceCall', 'task']
// При необходимости добавьте: 'changeRequest', 'problem', 'incident'

// Поля связи с сотрудниками (где сотрудник может быть указан)
List<String> REL_ATTRS = [
    'responsibleEmployee', 'executor', 'assignee', 'author',
    'clientEmployee', 'initiator', 'manager', 'observer',
    'performer', 'responsible'
]

// Поля назначения для подразделений и команд (адаптируйте под вашу конфигурацию)
List<String> OU_TARGET_FIELDS   = ['responsibleOu', 'ou', 'department']
List<String> TEAM_TARGET_FIELDS = ['responsibleTeam', 'team', 'executorTeam', 'assignedTeam']

// Статусы, которые НЕ обрабатываем (завершённые задачи)
Set<String> SKIP_STATUS_CODES = [
    'resolved', 'closed', 'canceled', 'cancelled', 
    'done', 'completed', 'finished', 'archived'
] as Set

Set<String> SKIP_STATUS_TITLES = [
    'разрешен', 'разрешено', 'разрешён', 'закрыт', 'закрыто', 
    'отклонен', 'отклонено', 'отклонён', 'выполнен', 'выполнено',
    'решено', 'решён', 'завершен', 'завершено', 'завершён',
    'отменен', 'отменено', 'отменён', 'архив'
] as Set

// Лимиты безопасности (чтобы не "положить" систему)
int MAX_EDITS_PER_EMPLOYEE = 500
int MAX_TOTAL_EDITS        = 20000
int MAX_PROCESSING_TIME_MS = 1800000  // 30 минут максимум

/* ===================== СИСТЕМА ЛОГИРОВАНИЯ И УТИЛИТЫ ===================== */

// Безопасный логгер с fallback
def log = (this.metaClass.hasProperty(this, 'logger') ? logger : [
    info:  { Object m -> println("[INFO] ${m}") },
    warn:  { Object m -> println("[WARN] ${m}") },
    error: { Object m -> println("[ERROR] ${m}") },
    debug: { Object m -> println("[DEBUG] ${m}") }
])

// Отчёт для возврата в консоль
def report = new StringBuilder()
def startTime = System.currentTimeMillis()

// Универсальная функция логирования
def say = { String level, String msg ->
    report.append(msg).append('\n')
    try {
        switch(level.toLowerCase()) {
            case 'i': case 'info':  log.info(msg);  break
            case 'w': case 'warn':  log.warn(msg);  break
            case 'e': case 'error': log.error(msg); break
            case 'd': case 'debug': log.debug(msg); break
            default: log.info(msg)
        }
    } catch (Exception ignore) {}
}

// Проверка таймаута
def checkTimeout = {
    if (System.currentTimeMillis() - startTime > MAX_PROCESSING_TIME_MS) {
        say('w', "TIMEOUT: Достигнут лимит времени выполнения (30 мин), остановка")
        return true
    }
    return false
}

// Пауза между операциями
def sleepIfNeeded = { 
    if (SLEEP_MS > 0) {
        try { Thread.sleep(SLEEP_MS) } catch (Exception ignore) {}
    }
}

// Безопасное выполнение в транзакции
def inTx = { Closure c ->
    try {
        if (this.metaClass.hasProperty(this, 'api') && api?.tx) {
            return api.tx.call { c.call() }
        }
        return c.call()
    } catch (Exception e) {
        say('e', "Ошибка транзакции: ${e.message}")
        return null
    }
}

/* ===================== ПАРСИНГ CSV ===================== */

// Продвинутый CSV-парсер с обработкой кавычек
def splitCsv = { String line ->
    def result = []
    def current = new StringBuilder()
    boolean inQuotes = false
    
    for (int i = 0; i < line.length(); i++) {
        char ch = line.charAt(i)
        
        if (ch == '"') {
            if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                // Двойная кавычка внутри строки
                current.append('"')
                i++ // пропускаем следующую кавычку
            } else {
                // Обычная кавычка - переключаем режим
                inQuotes = !inQuotes
            }
        } else if (ch == DELIM && !inQuotes) {
            // Разделитель вне кавычек
            result.add(current.toString().trim())
            current.setLength(0)
        } else {
            current.append(ch)
        }
    }
    
    result.add(current.toString().trim())
    return result
}

// Извлечение ФИО из CSV с нормализацией
def buildFioList = { String csvText ->
    return csvText.readLines()
        .findAll { line ->
            def trimmed = line?.trim()
            return trimmed && !trimmed.startsWith('#') && !trimmed.startsWith('//')
        }
        .collect { line ->
            try {
                def cols = splitCsv(line)
                def fioCell = cols.size() > FIO_COL ? cols[FIO_COL] : ''
                
                // Нормализация ФИО: убираем лишние пробелы, неразрывные пробелы
                def normalized = fioCell
                    ?.replace('\u00A0', ' ')    // неразрывный пробел
                    ?.replaceAll(/\s+/, ' ')    // множественные пробелы
                    ?.trim()
                
                if (!normalized) return null
                
                // Берём только первые 3 слова (Фамилия Имя Отчество)
                def words = normalized.tokenize(' ')
                if (words.size() < 2) return null  // минимум Фамилия + Имя
                
                return words.take(3).join(' ')
            } catch (Exception e) {
                say('w', "Ошибка парсинга строки CSV: '${line}' - ${e.message}")
                return null
            }
        }
        .findAll { it != null }
        .unique()  // убираем дубликаты
}

/* ===================== НОРМАЛИЗАЦИЯ UUID ДЛЯ NAUMEN SD 4.17 ===================== */

// Приведение UUID к формату Naumen: класс$ID
def normalizeUuid = { obj, String expectedClass = null ->
    if (!obj?.UUID) return null
    
    def uuid = obj.UUID.toString()
    
    // Если уже есть префикс с $ - возвращаем как есть
    if (uuid.contains('$')) return uuid
    
    // Определяем класс объекта
    def className = expectedClass
    if (!className) {
        try {
            className = obj.getClass().getSimpleName().toLowerCase()
        } catch (Exception ignore) {
            try {
                className = obj?.metaClass?.getTheClass()?.getSimpleName()?.toLowerCase()
            } catch (Exception ignore2) {
                return uuid  // не удалось определить - возвращаем как есть
            }
        }
    }
    
    // Приводим к стандартным именам Naumen
    switch(className?.toLowerCase()) {
        case 'employee': case 'emp': return "employee\$${uuid}"
        case 'organizationalunit': case 'ou': return "ou\$${uuid}"
        case 'team': return "team\$${uuid}"
        case 'department': case 'dept': return "ou\$${uuid}"  // часто department = ou
        default: return "${className}\$${uuid}"
    }
}

// Сравнение UUID с учётом префиксов
def compareUuid = { String uuid1, String uuid2 ->
    if (!uuid1 || !uuid2) return false
    
    // Извлекаем числовую часть для сравнения
    def extractId = { String uuid ->
        return uuid.contains('$') ? uuid.split('\\$', 2)[1] : uuid
    }
    
    return extractId(uuid1) == extractId(uuid2)
}

/* ===================== ПОИСК СОТРУДНИКА ПО ФИО ===================== */

boolean ENABLE_DEBUG_FIND = true
def sayDbg = { String s -> 
    if (ENABLE_DEBUG_FIND) {
        say('d', "[SEARCH] ${s}")
    }
}

// Нормализация ФИО для поиска
def normalizeFio = { String s ->
    return (s ?: '')
        .replace('\u00A0', ' ')           // неразрывный пробел
        .replaceAll(/\s+/, ' ')          // множественные пробелы
        .replace('ё', 'е').replace('Ё', 'Е')  // ё -> е
        .trim()
}

// Безопасное преобразование к объекту
def toObj = { any ->
    try {
        return (any instanceof String) ? utils.get(any) : any
    } catch (Exception e) {
        sayDbg("Ошибка преобразования к объекту: ${e.message}")
        return any
    }
}

def toObjList = { lst -> 
    return (lst ?: []).collect { toObj(it) }.findAll { it != null }
}

// Основная функция поиска сотрудника
def findEmployeeByFio = { String fioInput ->
    try {
        def fio = normalizeFio(fioInput)
        if (!fio) return null
        
        sayDbg("Ищем сотрудника: '${fio}'")
        
        // 1. Точный поиск по title
        def byTitleExact = utils.find('employee', [title: fio], sp.ignoreCase())
        if (byTitleExact?.size() == 1) {
            def emp = toObj(byTitleExact[0])
            sayDbg("Найден по точному title: ${emp?.title}")
            return emp
        }
        
        // 2. LIKE поиск по title
        def byTitleLike = utils.find('employee', [title: op.like("%${fio}%")], sp.ignoreCase())
        if (byTitleLike?.size() == 1) {
            def emp = toObj(byTitleLike[0])
            sayDbg("Найден по LIKE title: ${emp?.title}")
            return emp
        }
        
        // 3. Разбор на части: Фамилия Имя [Отчество]
        def parts = fio.tokenize(' ')
        String lastName  = parts.size() >= 1 ? parts[0] : null
        String firstName = parts.size() >= 2 ? parts[1] : null
        String middleName = parts.size() >= 3 ? parts[2] : null
        
        if (!lastName || !firstName) {
            sayDbg("Недостаточно частей ФИО: '${fio}'")
            return null
        }
        
        // 4. Поиск по трём полям (если есть отчество)
        List found = []
        if (middleName) {
            try {
                found = utils.find('employee', [
                    lastName:   op.like("%${lastName}%"),
                    firstName:  op.like("%${firstName}%"),
                    middleName: op.like("%${middleName}%")
                ], sp.ignoreCase())
                
                if (found?.size() == 1) {
                    def emp = toObj(found[0])
                    sayDbg("Найден по 3 полям: ${emp?.title}")
                    return emp
                }
            } catch (Exception e) {
                sayDbg("Ошибка поиска по 3 полям: ${e.message}")
            }
        }
        
        // 5. Поиск по двум полям (фамилия + имя)
        try {
            found = utils.find('employee', [
                lastName:  op.like("%${lastName}%"),
                firstName: op.like("%${firstName}%")
            ], sp.ignoreCase())
            
            if (found?.size() == 1) {
                def emp = toObj(found[0])
                sayDbg("Найден по 2 полям: ${emp?.title}")
                return emp
            }
        } catch (Exception e) {
            sayDbg("Ошибка поиска по 2 полям: ${e.message}")
        }
        
        // 6. Точное совпадение title среди найденных
        List candidates = toObjList(found ?: byTitleLike)
        def exactMatch = candidates.find { emp ->
            normalizeFio(emp?.title ?: '').equalsIgnoreCase(fio)
        }
        if (exactMatch) {
            sayDbg("Найдено точное совпадение среди кандидатов: ${exactMatch?.title}")
            return exactMatch
        }
        
        // 7. Расширенный поиск только по фамилии + фильтрация
        try {
            def byLastName = utils.find('employee', [lastName: op.like("%${lastName}%")], sp.ignoreCase())
            List filtered = toObjList(byLastName).findAll { emp ->
                def title = normalizeFio(emp?.title ?: '').toLowerCase()
                return title.contains(firstName.toLowerCase()) &&
                       (!middleName || title.contains(middleName.toLowerCase()))
            }
            
            if (filtered?.size() == 1) {
                sayDbg("Найден расширенным поиском: ${filtered[0]?.title}")
                return filtered[0]
            }
        } catch (Exception e) {
            sayDbg("Ошибка расширенного поиска: ${e.message}")
        }
        
        // 8. Диагностика для отчёта
        if (ENABLE_DEBUG_FIND && candidates) {
            def candidateNames = candidates.take(5).collect { it?.title ?: "ID:${it?.UUID}" }.join(' | ')
            sayDbg("Множественные кандидаты для '${fio}': ${candidateNames}")
        }
        
        return null
        
    } catch (Exception e) {
        sayDbg("Критическая ошибка поиска '${fioInput}': ${e.message}")
        return null
    }
}

/* ===================== РАБОТА СО СТАТУСАМИ И НАЗНАЧЕНИЯМИ ===================== */

// Получение информации о статусе объекта
def getStatusInfo = { obj ->
    try {
        if (!obj) return ['', '']
        
        def code = ''
        def title = ''
        
        // Пробуем разные варианты полей статуса
        ['status', 'state', 'stage'].each { field ->
            try {
                def statusObj = obj."${field}"
                if (statusObj) {
                    if (!code)  code  = statusObj.code?.toString()?.toLowerCase() ?: ''
                    if (!title) title = statusObj.title?.toString()?.toLowerCase() ?: ''
                }
            } catch (Exception ignore) {}
        }
        
        return [code, title]
        
    } catch (Exception e) {
        sayDbg("Ошибка получения статуса для ${obj?.UUID}: ${e.message}")
        return ['', '']
    }
}

// Определение цели назначения (OU или Team)
def pickTarget = { emp ->
    try {
        for (priority in TARGET_PRIORITY) {
            if (priority == 'ou' && emp?.ou?.UUID) {
                def ouUuid = normalizeUuid(emp.ou, 'ou')
                if (ouUuid) {
                    sayDbg("Выбрано OU: ${ouUuid} для ${emp.title}")
                    return [ouUuid, OU_TARGET_FIELDS, 'ou']
                }
            }
            if (priority == 'team' && emp?.team?.UUID) {
                def teamUuid = normalizeUuid(emp.team, 'team')
                if (teamUuid) {
                    sayDbg("Выбрано Team: ${teamUuid} для ${emp.title}")
                    return [teamUuid, TEAM_TARGET_FIELDS, 'team']
                }
            }
        }
        
        sayDbg("Не найдено OU/Team для назначения: ${emp?.title}")
        return [null, null, null]
        
    } catch (Exception e) {
        sayDbg("Ошибка выбора цели для ${emp?.title}: ${e.message}")
        return [null, null, null]
    }
}

// Проверка, уже ли назначен объект на указанную цель
def alreadyAssignedTo = { obj, String field, String targetUuid ->
    try {
        // Проверяем существование поля
        def value = obj."${field}"
        if (!value) return false
        
        // Получаем UUID текущего назначения
        def currentUuid = (value?.UUID ?: (value instanceof String ? value : null))?.toString()
        if (!currentUuid) return false
        
        // Сравниваем с учётом префиксов Naumen
        boolean isAssigned = compareUuid(currentUuid, targetUuid)
        
        if (isAssigned) {
            sayDbg("Объект ${obj.UUID} уже назначен: ${field}=${currentUuid}")
        }
        
        return isAssigned
        
    } catch (Exception e) {
        sayDbg("Ошибка проверки назначения ${field} для ${obj?.UUID}: ${e.message}")
        return false
    }
}

// Попытка назначения на одно из полей
def tryAssign = { obj, List<String> fields, String targetUuid ->
    for (field in fields) {
        try {
            // Проверяем, уже назначено ли на эту цель
            if (alreadyAssignedTo(obj, field, targetUuid)) {
                return 'already_assigned'
            }
            
            // Выполняем назначение (или эмуляцию в DRY_RUN)
            if (DRY_RUN) {
                say('i', "DRY_RUN: ${obj.UUID} -> ${field} := ${targetUuid}")
                return 'assigned'
            } else {
                inTx { 
                    utils.edit(obj, [(field): targetUuid]) 
                }
                say('i', "ASSIGNED: ${obj.UUID} -> ${field} := ${targetUuid}")
                return 'assigned'
            }
            
        } catch (Exception e) {
            sayDbg("Ошибка назначения ${field}=${targetUuid} для ${obj.UUID}: ${e.message}")
            // Продолжаем с следующим полем
        }
    }
    
    return 'failed'
}

/* ===================== ОБРАБОТКА ЛИЦЕНЗИЙ ===================== */

def updateLicense = { emp ->
    try {
        def currentLicense = emp?.license
        boolean alreadyNotLicensed = false
        
        // Проверяем разные форматы лицензии в Naumen
        if (currentLicense instanceof String) {
            alreadyNotLicensed = currentLicense.toLowerCase().contains('notlicensed') ||
                               currentLicense.toLowerCase().contains('нелиценз')
        } else if (currentLicense?.code) {
            alreadyNotLicensed = currentLicense.code.toString().toLowerCase().contains('notlicensed')
        } else if (currentLicense?.title) {
            def title = currentLicense.title.toString().toLowerCase()
            alreadyNotLicensed = title.contains('notlicensed') || title.contains('нелиценз')
        }
        
        if (alreadyNotLicensed) {
            sayDbg("Лицензия уже notLicensed: ${emp.title}")
            return false
        }
        
        // Устанавливаем notLicensed
        if (DRY_RUN) {
            say('i', "DRY_RUN: ${emp.title} license: ${currentLicense} -> notLicensed")
        } else {
            inTx { 
                utils.edit(emp, [license: 'notLicensed']) 
            }
            say('i', "LICENSE: ${emp.title} license: ${currentLicense} -> notLicensed")
        }
        
        return true
        
    } catch (Exception e) {
        say('e', "Ошибка установки лицензии для ${emp?.title}: ${e.message}")
        return false
    }
}

/* ===================== ОСНОВНОЙ ПРОЦЕСС ОБРАБОТКИ ===================== */

try {
    say('i', "=== НАЧАЛО ОБРАБОТКИ ===")
    say('i', "Режим: ${DRY_RUN ? 'DRY_RUN (тестирование)' : 'РЕАЛЬНЫЕ ИЗМЕНЕНИЯ'}")
    say('i', "Версия: 2.0 для Naumen SD 4.17")
    
    // Парсинг CSV
    def fioList = buildFioList(CSV_TEXT)
    say('i', "Извлечено ФИО из CSV: ${fioList.size()}")
    
    if (fioList.empty) {
        say('w', "CSV не содержит корректных ФИО для обработки")
        return report.toString()
    }
    
    // Инициализация счётчиков
    int processed = 0
    int employeesFound = 0
    int employeesNotFound = 0
    int totalTasksReassigned = 0
    int totalLicensesChanged = 0
    int skippedResolved = 0
    int skippedAlreadyAssigned = 0
    
    def notFoundList = []
    def licenseChangedList = []
    def reassignmentLog = []
    
    // Кэш для найденных сотрудников (оптимизация)
    def employeeCache = [:]
    
    // Основной цикл обработки
    fioList.each { fio ->
        processed++
        
        // Проверки безопасности
        if (totalTasksReassigned >= MAX_TOTAL_EDITS) {
            say('w', "Достигнут лимит переназначений (${MAX_TOTAL_EDITS}), остановка")
            return
        }
        
        if (checkTimeout()) {
            return
        }
        
        say('i', "--- Обработка ${processed}/${fioList.size()}: ${fio} ---")
        
        // Поиск сотрудника (с кэшированием)
        def emp = employeeCache[fio]
        if (emp === null) {  // null означает "ещё не искали"
            emp = findEmployeeByFio(fio)
            employeeCache[fio] = emp ?: false  // false означает "не найден"
        }
        
        if (!emp) {
            employeesNotFound++
            notFoundList << fio
            say('w', "Сотрудник не найден: ${fio}")
            return  // переходим к следующему
        }
        
        employeesFound++
        say('i', "Найден сотрудник: ${emp.title} (${emp.UUID})")
        
        // Определяем цель назначения
        def (targetUuid, targetFields, targetKind) = pickTarget(emp)
        
        int tasksReassignedForEmployee = 0
        
        if (targetUuid && targetFields) {
            say('i', "Назначение на ${targetKind.toUpperCase()}: ${targetUuid}")
            
            // Собираем связанные объекты
            def relatedObjects = []
            def seenUuids = new HashSet<String>()
            
            CLASSES.each { className ->
                REL_ATTRS.each { relationField ->
                    try {
                        def objects = utils.find(className, [(relationField): emp])
                        objects?.each { obj ->
                            def objInstance = toObj(obj)
                            if (objInstance?.UUID && seenUuids.add(objInstance.UUID)) {
                                relatedObjects << objInstance
                            }
                        }
                    } catch (Exception e) {
                        sayDbg("Ошибка поиска ${className}.${relationField}: ${e.message}")
                    }
                }
            }
            
            say('i', "Найдено связанных объектов: ${relatedObjects.size()}")
            
            // Обрабатываем каждый связанный объект
            relatedObjects.each { obj ->
                // Проверки лимитов
                if (tasksReassignedForEmployee >= MAX_EDITS_PER_EMPLOYEE ||
                    totalTasksReassigned >= MAX_TOTAL_EDITS) {
                    return
                }
                
                try {
                    // Проверяем статус (пропускаем завершённые)
                    def (statusCode, statusTitle) = getStatusInfo(obj)
                    
                    if ((statusCode && SKIP_STATUS_CODES.contains(statusCode)) ||
                        (statusTitle && SKIP_STATUS_TITLES.any { statusTitle.contains(it) })) {
                        skippedResolved++
                        sayDbg("Пропущен (завершён): ${obj.UUID} статус: ${statusCode}/${statusTitle}")
                        return
                    }
                    
                    // Пытаемся назначить на основную цель
                    def assignResult = tryAssign(obj, targetFields, targetUuid)
                    
                    // Если не получилось - пытаемся альтернативу
                    if (assignResult == 'failed') {
                        def altTargetUuid = (targetKind == 'ou') ? 
                            normalizeUuid(emp?.team, 'team') : 
                            normalizeUuid(emp?.ou, 'ou')
                        def altTargetFields = (targetKind == 'ou') ? 
                            TEAM_TARGET_FIELDS : 
                            OU_TARGET_FIELDS
                        
                        if (altTargetUuid) {
                            say('i', "Пробуем альтернативное назначение: ${altTargetUuid}")
                            assignResult = tryAssign(obj, altTargetFields, altTargetUuid)
                        }
                    }
                    
                    // Обновляем счётчики
                    if (assignResult == 'assigned') {
                        tasksReassignedForEmployee++
                        totalTasksReassigned++
                        reassignmentLog << "${emp.title} -> ${obj.UUID}"
                        sleepIfNeeded()
                    } else if (assignResult == 'already_assigned') {
                        skippedAlreadyAssigned++
                    }
                    
                } catch (Exception e) {
                    say('e', "Ошибка обработки объекта ${obj?.UUID}: ${e.message}")
                }
            }
            
        } else {
            say('w', "Нет OU/Team для назначения у сотрудника: ${emp.title}")
        }
        
        say('i', "Переназначено задач для сотрудника: ${tasksReassignedForEmployee}")
        
        // Обработка лицензии
        try {
            if (updateLicense(emp)) {
                totalLicensesChanged++
                licenseChangedList << emp.title
            }
        } catch (Exception e) {
            say('e', "Ошибка обработки лицензии: ${e.message}")
        }
        
        sleepIfNeeded()
    }
    
    /* ===================== ИТОГОВЫЙ ОТЧЁТ ===================== */
    
    def processingTime = (System.currentTimeMillis() - startTime) / 1000
    
    say('i', "")
    say('i', "=== ИТОГОВЫЙ ОТЧЁТ ===")
    say('i', "Режим выполнения: ${DRY_RUN ? 'DRY_RUN (без изменений)' : 'РЕАЛЬНЫЕ ИЗМЕНЕНИЯ'}")
    say('i', "Время выполнения: ${processingTime} сек")
    say('i', "")
    say('i', "📊 СТАТИСТИКА:")
    say('i', "  • Всего ФИО обработано: ${processed}")
    say('i', "  • Сотрудников найдено: ${employeesFound}")
    say('i', "  • Сотрудников НЕ найдено: ${employeesNotFound}")
    say('i', "  • Задач переназначено: ${totalTasksReassigned}")
    say('i', "  • Лицензий изменено: ${totalLicensesChanged}")
    say('i', "  • Пропущено (завершённые): ${skippedResolved}")
    say('i', "  • Пропущено (уже назначено): ${skippedAlreadyAssigned}")
    
    if (notFoundList) {
        say('w', "")
        say('w', "❌ НЕ НАЙДЕНЫ СОТРУДНИКИ (${notFoundList.size()}):")
        notFoundList.each { say('w', "  • ${it}") }
    }
    
    if (licenseChangedList && !DRY_RUN) {
        say('i', "")
        say('i', "🔑 ИЗМЕНЕНЫ ЛИЦЕНЗИИ (${licenseChangedList.size()}):")
        licenseChangedList.take(10).each { say('i', "  • ${it}") }
        if (licenseChangedList.size() > 10) {
            say('i', "  • ... и ещё ${licenseChangedList.size() - 10}")
        }
    }
    
    if (DRY_RUN) {
        say('i', "")
        say('i', "🧪 РЕЖИМ ТЕСТИРОВАНИЯ")
        say('i', "Для применения изменений установите: DRY_RUN = false")
    }
    
    say('i', "")
    say('i', "=== ЗАВЕРШЕНО ===")
    
} catch (Exception e) {
    say('e', "КРИТИЧЕСКАЯ ОШИБКА: ${e.message}")
    say('e', "Трассировка: ${e.getStackTrace().take(5).join(' | ')}")
} finally {
    // Финальная очистка ресурсов если необходимо
    try {
        if (ENABLE_DEBUG_FIND) {
            say('d', "Очистка кэшей и ресурсов...")
        }
    } catch (Exception ignore) {}
}

// Возвращаем отчёт в консоль Naumen
return report.toString()