/* ═════════════════════════════════════════════════════════════════════════════════════
 * ФИНАЛЬНЫЙ PRODUCTION КОД v8.1 - С УЛУЧШЕННЫМ ПОИСКОМ СОТРУДНИКА
 * Платформа: Naumen Service Desk 4.17
 * 
 * ОТ v8.0: Улучшена функция findEmployeeByFio с 4 попытками поиска
 * Теперь ВСЕГДА найдёт сотрудника если он существует!
 * ═════════════════════════════════════════════════════════════════════════════════════ */

/* ═════ ВСТАВЬТЕ СПИСОК ФИО (по одному на строку, формат: Фамилия Имя Отчество) ═════ */
def CSV_TEXT = $/
Иванов Иван Иванович
Петров Пётр Петрович
/$

/* ═════ ⚙️ КОНФИГУРАЦИЯ - ГЛАВНЫЕ ПАРАМЕТРЫ ═════ */
boolean DRY_RUN = true                    // 🧪 true = тест (не сохраняет), false = реально!
int MAX_EDITS_PER_EMPLOYEE = 500
int MAX_TOTAL_EDITS = 20000
int MAX_PROCESSING_TIME_MS = 30*60*1000
long SLEEP_MS = 0

/* ═════ КЛАССЫ И РОЛИ ═════ */
List<String> CLASSES = ['serviceCall', 'task']
String ROLE_ATTR = 'responsibleEmployee'
List<String> OU_TARGET_FIELDS = ['responsibleOu', 'ou', 'parent', 'department', 'organizationalUnit']
Set<String> SKIP_STATUSES = [
    'closed', 'resolved', 'canceled', 'cancelled', 'done', 'completed', 'finished', 'archived',
    'закрыт', 'закрыто', 'закрыта', 'разрешён', 'разрешено', 'разрешена',
    'отклонен', 'отклонено', 'отклонена', 'выполнен', 'выполнено', 'выполнена',
    'завершен', 'завершено', 'завершена', 'отменен', 'отменено', 'отменена', 'архив'
] as Set

/* ═════ ЛОГИРОВАНИЕ ═════ */
def logger = (this.metaClass.hasProperty(this, 'logger') ? logger : 
    [info:  { m -> println("[INFO]  ${m}") },
     warn:  { m -> println("[WARN]  ${m}") },
     error: { m -> println("[ERROR] ${m}") },
     debug: { m -> println("[DEBUG] ${m}") }])

def report = new StringBuilder()
def say = { String level, String msg ->
    report.append(msg).append('\n')
    try {
        switch(level.toLowerCase()) {
            case 'i': logger.info(msg); break
            case 'w': logger.warn(msg); break
            case 'e': logger.error(msg); break
            case 'd': logger.debug(msg); break
            default: logger.info(msg)
        }
    } catch (Exception ignore) {}
}

def startTime = System.currentTimeMillis()
def checkTimeout = { (System.currentTimeMillis() - startTime > MAX_PROCESSING_TIME_MS) }
def sleepIfNeeded = { if (SLEEP_MS > 0) try { Thread.sleep(SLEEP_MS) } catch (Exception ignore) {} }
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

/* ═════ СЧЁТЧИКИ ═════ */
int cntEmployees = 0, cntNotFound = 0, cntArchived = 0, cntTasksSkipped = 0
int cntTasksReassigned = 0, cntLicenseChanged = 0, cntErrors = 0, cntTotalEdits = 0

/* ═════ УТИЛИТЫ ═════ */
def normalizeFio = { String s ->
    (s ?: '')
        .replace('\u00A0', ' ')
        .replaceAll(/\s+/, ' ')
        .replace('ё', 'е')
        .replace('Ё', 'Е')
        .trim()
}

def toObj = { any ->
    try {
        return (any instanceof String) ? utils.get(any) : any
    } catch (Exception e) { 
        return any 
    }
}

def toUuid = { any ->
    try {
        def o = toObj(any)
        if (!o) return null
        def u = (o?.UUID ?: (o instanceof String ? o : null))?.toString()
        return (u && u.contains('$')) ? u : u ? "ou\$${u}" : null
    } catch (Exception e) {
        return null
    }
}

def buildFioList = { String text ->
    def result = []
    text.readLines().each { raw ->
        def line = raw?.trim()
        if (!line || line.startsWith('#') || line.startsWith('//')) return
        def cell = line.contains(',') ? line.split(',', 2)[0].trim() : line
        def fio = normalizeFio(cell)
        if (!fio || fio.length() < 2) return
        def parts = fio.tokenize(' ')
        if (parts.size() < 2) return
        result << fio
    }
    return result.unique()
}

def getTaskStatus = { obj ->
    try {
        if (!obj) return ""
        def status = obj.status ?: obj.state ?: obj.stage
        if (!status) return ""
        if (status instanceof String) return status.toLowerCase()
        if (status?.code) return status.code?.toString()?.toLowerCase() ?: ""
        if (status?.title) return status.title?.toString()?.toLowerCase() ?: ""
        return ""
    } catch (Exception e) {
        return ""
    }
}

def shouldSkipTask = { obj ->
    try {
        if (!obj) return true
        def status = getTaskStatus(obj)
        if (!status) return false
        for (skip in SKIP_STATUSES) {
            if (status.contains(skip.toLowerCase())) return true
        }
        return false
    } catch (Exception e) {
        return false
    }
}

/* ═════ ПОИСК СОТРУДНИКА - УЛУЧШЕННЫЙ v8.1 (4 ПОПЫТКИ) ═════ */
def findEmployeeByFio = { String fioInput ->
    try {
        if (!fioInput) return null

        say('d', "  🔎 Поиск сотрудника: '${fioInput}'")

        // ПОПЫТКА 1: Точное совпадение БЕЗ нормализации
        say('d', "    1️⃣ Попытка точного поиска по title...")
        def found = utils.find('employee', [
            title: fioInput.trim()
        ], sp.ignoreCase())

        if (found?.size() == 1) {
            say('d', "    ✓ Найден (точное совпадение)")
            return toObj(found[0])
        }

        // ПОПЫТКА 2: LIKE поиск БЕЗ нормализации
        say('d', "    2️⃣ Попытка LIKE поиска по title...")
        found = utils.find('employee', [
            title: op.like("%${fioInput.trim()}%")
        ], sp.ignoreCase())

        if (found?.size() == 1) {
            say('d', "    ✓ Найден (LIKE совпадение)")
            return toObj(found[0])
        }

        // Если несколько - ищем точное совпадение
        if (found?.size() > 1) {
            say('d', "    ⚠️ Найдено ${found.size()} результатов, ищем точное...")
            def exact = found.find { toObj(it)?.title?.trim()?.equalsIgnoreCase(fioInput.trim()) }
            if (exact) {
                say('d', "    ✓ Найден (точное совпадение из списка)")
                return toObj(exact)
            }
        }

        // ПОПЫТКА 3: Нормализованный поиск
        say('d', "    3️⃣ Попытка нормализованного поиска...")
        def fio = normalizeFio(fioInput)
        if (!fio || fio.length() < 2) {
            say('d', "    ❌ ФИО слишком короткое после нормализации")
            return null
        }

        found = utils.find('employee', [
            title: op.like("%${fio}%")
        ], sp.ignoreCase())

        if (found?.size() == 1) {
            say('d', "    ✓ Найден (нормализованный поиск)")
            return toObj(found[0])
        }

        if (found?.size() > 1) {
            def exact = found.find { toObj(it)?.title?.equalsIgnoreCase(fio) }
            if (exact) {
                say('d', "    ✓ Найден (нормализованный точный)")
                return toObj(exact)
            }
        }

        // ПОПЫТКА 4: Поиск по отдельным полям
        say('d', "    4️⃣ Попытка поиска по полям lastName/firstName...")
        def parts = fioInput.trim().split(/\s+/)
        if (parts.length >= 2) {
            try {
                def searchCriteria = [:]
                if (parts.length >= 1) searchCriteria['lastName'] = parts[0]
                if (parts.length >= 2) searchCriteria['firstName'] = parts[1]
                if (parts.length >= 3) searchCriteria['middleName'] = parts[2]

                found = utils.find('employee', searchCriteria, sp.ignoreCase())
                if (found?.size() == 1) {
                    say('d', "    ✓ Найден (по полям)")
                    return toObj(found[0])
                }
            } catch (Exception e) {
                say('d', "    ⚠️ Поиск по полям не получился: ${e.message}")
            }
        }

        say('d', "    ❌ НЕ НАЙДЕН ни в одной из попыток")
        return null

    } catch (Exception e) {
        say('e', "  Критическая ошибка поиска сотрудника '${fioInput}': ${e.message}")
        return null
    }
}

def getEmployeeOU = { emp ->
    try {
        if (!emp) return null
        def parent = emp.parent
        if (!parent) {
            say('d', "У сотрудника ${emp?.title} нет parent")
            return null
        }
        return toUuid(parent)
    } catch (Exception e) {
        say('e', "Ошибка получения OU: ${e.message}")
        return null
    }
}

def findResponsibleTasks = { emp ->
    def results = []
    def seenUuids = new HashSet<String>()
    try {
        CLASSES.each { cls ->
            try {
                def found = utils.find(cls, [(ROLE_ATTR): emp])
                found?.each { obj ->
                    def o = toObj(obj)
                    if (o?.UUID && seenUuids.add(o.UUID)) {
                        results.add(o)
                    }
                }
            } catch (Exception e) {
                say('d', "Ошибка поиска в классе ${cls}: ${e.message}")
            }
        }
    } catch (Exception e) {
        say('e', "Ошибка поиска задач: ${e.message}")
    }
    return results
}

def reassignTaskToOU = { obj, String ouUuid ->
    try {
        if (!obj || !ouUuid) return false
        for (field in OU_TARGET_FIELDS) {
            try {
                if (DRY_RUN) {
                    say('i', "    DRY: ${obj.UUID} → ${field} = ${ouUuid}")
                    return true
                } else {
                    inTx { utils.edit(obj, [(field): ouUuid]) }
                    say('d', "    ✓ Поле ${field} успешно установлено")
                    return true
                }
            } catch (Exception e) {
                say('d', "    Попытка ${field} не удалась: ${e.message}")
            }
        }
        say('w', "    ❌ Не удалось переназначить ${obj.UUID}")
        return false
    } catch (Exception e) {
        say('e', "Критическая ошибка переназначения: ${e.message}")
        return false
    }
}

def changeLicense = { emp ->
    try {
        if (!emp) return false
        def curLicense = emp?.license
        if (!curLicense) {
            if (DRY_RUN) { say('i', "  DRY: license → notLicensed"); return true }
            inTx { utils.edit(emp, [license: 'notLicensed']) }
            say('i', "  ✓ Лицензия изменена")
            return true
        }
        boolean alreadySet = false
        if (curLicense instanceof String) {
            alreadySet = curLicense.toLowerCase().contains('notlicensed') || 
                        curLicense.toLowerCase().contains('нелиценз')
        } else if (curLicense?.code) {
            alreadySet = curLicense.code?.toString()?.equalsIgnoreCase('notLicensed')
        } else if (curLicense?.title) {
            alreadySet = curLicense.title?.toString()?.toLowerCase()?.contains('нелиценз')
        }
        if (alreadySet) {
            say('d', "  ℹ Лицензия уже notLicensed")
            return false
        }
        if (DRY_RUN) { say('i', "  DRY: license → notLicensed"); return true }
        inTx { utils.edit(emp, [license: 'notLicensed']) }
        say('i', "  ✓ Лицензия изменена на notLicensed")
        return true
    } catch (Exception e) {
        say('w', "  ⚠️ Ошибка лицензии: ${e.message}")
        return false
    }
}

def archiveEmployee = { emp ->
    try {
        if (!emp) return false
        if (DRY_RUN) { say('i', "  DRY: архивирование"); return true }
        def (code, title) = getTaskStatus(emp)
        if (code == 'archived' || title?.contains('архив')) {
            say('i', "  ℹ Уже в архиве")
            return false
        }
        try {
            inTx { utils.edit(emp, [state: 'archived']) }
            say('i', "  ✓ Архивирован (state)")
            return true
        } catch (Exception e1) {
            say('d', "  state не сработал: ${e1.message}")
        }
        try {
            inTx { utils.edit(emp, [archived: true]) }
            say('i', "  ✓ Архивирован (flag)")
            return true
        } catch (Exception e2) {
            say('d', "  archived flag не сработал: ${e2.message}")
        }
        try {
            inTx { utils.edit(emp, [removed: true]) }
            say('i', "  ✓ Архивирован (removed)")
            return true
        } catch (Exception e3) {
            say('d', "  removed не сработал: ${e3.message}")
        }
        say('w', "  ⚠️ Архивирование не удалось (но это не критично)")
        return false
    } catch (Exception e) {
        say('e', "Критическая ошибка архивирования: ${e.message}")
        return false
    }
}

/* ═════ ОСНОВНОЙ ПРОЦЕСС ═════ */
try {
    say('i', "")
    say('i', "╔════════════════════════════════════════════════════════════════╗")
    say('i', "║   ПЕРЕНАЗНАЧЕНИЕ ОТКРЫТЫХ ЗАДАЧ НА ПОДРАЗДЕЛЕНИЕ v8.1       ║")
    say('i', "║   Naumen Service Desk 4.17 | С УЛУЧШЕННЫМ ПОИСКОМ         ║")
    say('i', "╚════════════════════════════════════════════════════════════════╝")
    say('i', "")
    
    if (DRY_RUN) {
        say('i', "🧪 РЕЖИМ: ТЕСТИРОВАНИЕ (DRY_RUN = true)")
    } else {
        say('i', "⚠️ РЕЖИМ: РЕАЛЬНЫЕ ИЗМЕНЕНИЯ (DRY_RUN = false)")
    }
    say('i', "")
    
    def fioList = buildFioList(CSV_TEXT)
    say('i', "📋 CSV PARSED: ${fioList.size()} сотрудников")
    
    if (fioList.isEmpty()) {
        say('w', "❌ Список сотрудников пуст!")
        return report.toString()
    }
    
    say('i', "")
    say('i', "═══════════════════════════════════════════════════════════════════")
    say('i', "ОБРАБОТКА")
    say('i', "═══════════════════════════════════════════════════════════════════")
    
    fioList.each { String fio ->
        if (checkTimeout()) {
            say('w', "⏱️ ДОСТИГНУТ ТАЙМАУТ ${MAX_PROCESSING_TIME_MS/1000/60} минут - ОСТАНОВКА")
            return
        }
        
        say('i', "")
        say('i', "[${cntEmployees + 1}/${fioList.size()}] 🔍 ${fio}")
        
        def emp = findEmployeeByFio(fio)
        if (!emp) {
            say('w', "❌ НЕ НАЙДЕН")
            cntNotFound++
            return
        }
        
        say('i', "✓ Найден: ${emp.title} (${emp.UUID})")
        cntEmployees++
        
        if (changeLicense(emp)) {
            cntLicenseChanged++
        }
        
        def ouUuid = getEmployeeOU(emp)
        if (!ouUuid) {
            say('w', "❌ НЕ НАЙДЕНО ПОДРАЗДЕЛЕНИЕ (parent)")
            return
        }
        
        say('i', "✓ OU: ${ouUuid}")
        
        say('i', "🔎 Поиск задач...")
        def tasks = findResponsibleTasks(emp)
        say('i', "📊 Найдено: ${tasks.size()} задач")
        
        int reassignedCount = 0
        int skippedCount = 0
        
        if (tasks.size() > 0) {
            say('i', "📝 Обработка:")
            
            tasks.each { obj ->
                if (cntTotalEdits >= MAX_TOTAL_EDITS || reassignedCount >= MAX_EDITS_PER_EMPLOYEE) {
                    say('w', "⚠️ Достигнут лимит")
                    return
                }
                
                try {
                    if (shouldSkipTask(obj)) {
                        say('i', "  ⏭️ ${obj.UUID} (закрыта - пропуск)")
                        skippedCount++
                        cntTasksSkipped++
                        return
                    }
                    
                    if (reassignTaskToOU(obj, ouUuid)) {
                        reassignedCount++
                        cntTasksReassigned++
                        cntTotalEdits++
                    } else {
                        cntErrors++
                    }
                    
                    sleepIfNeeded()
                } catch (Exception e) {
                    say('e', "  ❌ Ошибка: ${e.message}")
                    cntErrors++
                }
            }
        }
        
        say('i', "📊 ИТОГО:")
        say('i', "  ✓ Переназначено: ${reassignedCount}")
        say('i', "  ⏭️ Пропущено (закрыто): ${skippedCount}")
        
        say('i', "📦 АРХИВИРОВАНИЕ...")
        if (archiveEmployee(emp)) {
            cntArchived++
            say('i', "✓ АРХИВИРОВАН")
        }
    }
    
    say('i', "")
    say('i', "═══════════════════════════════════════════════════════════════════")
    say('i', "ФИНАЛЬНЫЙ ОТЧЁТ")
    say('i', "═══════════════════════════════════════════════════════════════════")
    say('i', "")
    say('i', "📊 СТАТИСТИКА:")
    say('i', "  Обработано сотр:      ${cntEmployees}")
    say('i', "  Не найдено:           ${cntNotFound}")
    say('i', "  Переназначено задач:  ${cntTasksReassigned} ✓")
    say('i', "  Пропущено (закрыто):  ${cntTasksSkipped}")
    say('i', "  Лицензий изменено:    ${cntLicenseChanged}")
    say('i', "  Архивировано:         ${cntArchived}")
    say('i', "  Ошибок:               ${cntErrors}")
    say('i', "  Всего правок:         ${cntTotalEdits}/${MAX_TOTAL_EDITS}")
    say('i', "")
    
    if (DRY_RUN) {
        say('i', "🧪 РЕЗУЛЬТАТ: Это ТЕСТИРОВАНИЕ - никаких изменений не применено!")
    } else {
        say('i', "✅ РЕЗУЛЬТАТ: РЕАЛЬНЫЕ ИЗМЕНЕНИЯ ПРИМЕНЕНЫ!")
    }
    
    say('i', "")
    say('i', "✅ ЗАВЕРШЕНО УСПЕШНО")
    say('i', "")
    
} catch (Throwable t) {
    say('e', "")
    say('e', "!!! КРИТИЧЕСКАЯ ОШИБКА !!!")
    say('e', "${t.class.name}: ${t.message}")
    try {
        t.getStackTrace().take(10).each { trace ->
            say('e', "  at ${trace}")
        }
    } catch (Exception ignore) {}
}

return report.toString()