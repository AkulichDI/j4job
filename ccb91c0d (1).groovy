/* =====================================================================================
 * СКРИПТ АВТОМАТИЧЕСКОГО ПЕРЕНАЗНАЧЕНИЯ ЗАДАЧ И ЛИЦЕНЗИЙ
 * Версия: 2.1 (ИСПРАВЛЕНА ОШИБКА ДОСТУПА К АТРИБУТАМ)
 * Для Naumen Service Desk 4.17
 * 
 * Исправление: безопасный доступ к полям ou/team с поддержкой альтернативных имён
 * ===================================================================================== */

/* ===================== ВСТАВЬТЕ CSV ЗДЕСЬ ===================== */
def CSV_TEXT = $/
Иванов Иван Иванович,любые,другие,данные
Петров Пётр Петрович,можно,игнорировать
/$

/* ===================== ОСНОВНЫЕ ПАРАМЕТРЫ ===================== */
char    DELIM   = ','
int     FIO_COL = 0
boolean DRY_RUN = true          // ОБЯЗАТЕЛЬНО true для первого запуска!
long    SLEEP_MS = 100

List<String> TARGET_PRIORITY = ['ou', 'team']

List<String> CLASSES = ['serviceCall', 'task']

List<String> REL_ATTRS = [
    'responsibleEmployee', 'executor', 'assignee', 'author',
    'clientEmployee', 'initiator', 'manager', 'observer',
    'performer', 'responsible'
]

List<String> OU_TARGET_FIELDS   = ['responsibleOu', 'ou', 'department']
List<String> TEAM_TARGET_FIELDS = ['responsibleTeam', 'team', 'executorTeam', 'assignedTeam']

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

int MAX_EDITS_PER_EMPLOYEE = 500
int MAX_TOTAL_EDITS        = 20000
int MAX_PROCESSING_TIME_MS = 1800000

/* ===================== АЛЬТЕРНАТИВНЫЕ ИМЕНА ПОЛЕЙ ===================== */
// В разных конфигурациях Naumen поля могут называться по-разному
List<String> OU_FIELD_NAMES = [
    'ou', 'organizationalUnit', 'department', 
    'orgUnit', 'responsibleOu', 'employeeOu'
]

List<String> TEAM_FIELD_NAMES = [
    'team', 'employeeTeam', 'responsibleTeam', 'workGroup'
]

/* ===================== СИСТЕМА ЛОГИРОВАНИЯ И УТИЛИТЫ ===================== */

def log = (this.metaClass.hasProperty(this, 'logger') ? logger : [
    info:  { Object m -> println("[INFO] ${m}") },
    warn:  { Object m -> println("[WARN] ${m}") },
    error: { Object m -> println("[ERROR] ${m}") },
    debug: { Object m -> println("[DEBUG] ${m}") }
])

def report = new StringBuilder()
def startTime = System.currentTimeMillis()

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

def checkTimeout = {
    if (System.currentTimeMillis() - startTime > MAX_PROCESSING_TIME_MS) {
        say('w', "TIMEOUT: Достигнут лимит времени выполнения (30 мин)")
        return true
    }
    return false
}

def sleepIfNeeded = { 
    if (SLEEP_MS > 0) {
        try { Thread.sleep(SLEEP_MS) } catch (Exception ignore) {}
    }
}

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

def splitCsv = { String line ->
    def result = []
    def current = new StringBuilder()
    boolean inQuotes = false
    
    for (int i = 0; i < line.length(); i++) {
        char ch = line.charAt(i)
        
        if (ch == '"') {
            if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                current.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (ch == DELIM && !inQuotes) {
            result.add(current.toString().trim())
            current.setLength(0)
        } else {
            current.append(ch)
        }
    }
    
    result.add(current.toString().trim())
    return result
}

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
                
                def normalized = fioCell
                    ?.replace('\u00A0', ' ')
                    ?.replaceAll(/\s+/, ' ')
                    ?.trim()
                
                if (!normalized) return null
                
                def words = normalized.tokenize(' ')
                if (words.size() < 2) return null
                
                return words.take(3).join(' ')
            } catch (Exception e) {
                say('w', "Ошибка парсинга строки: '${line}' - ${e.message}")
                return null
            }
        }
        .findAll { it != null }
        .unique()
}

/* ===================== БЕЗОПАСНЫЙ ДОСТУП К АТРИБУТАМ ===================== */

// Получение значения атрибута с поддержкой альтернативных имён
def safeGetAttribute = { obj, List<String> fieldNames ->
    if (!obj) return null
    
    for (fieldName in fieldNames) {
        try {
            // Проверяем существование поля
            if (obj.hasProperty(fieldName)) {
                def value = obj."${fieldName}"
                if (value != null) {
                    return value
                }
            }
        } catch (Exception e) {
            // Поле не существует или недоступно, пробуем следующее
            continue
        }
    }
    
    return null
}

// Безопасное получение UUID атрибута
def safeGetUuid = { obj, List<String> fieldNames ->
    def value = safeGetAttribute(obj, fieldNames)
    return value?.UUID
}

/* ===================== НОРМАЛИЗАЦИЯ UUID ===================== */

def normalizeUuid = { obj, String expectedClass = null ->
    if (!obj?.UUID) return null
    
    def uuid = obj.UUID.toString()
    
    if (uuid.contains('$')) return uuid
    
    def className = expectedClass
    if (!className) {
        try {
            className = obj.getClass().getSimpleName().toLowerCase()
        } catch (Exception ignore) {
            try {
                className = obj?.metaClass?.getTheClass()?.getSimpleName()?.toLowerCase()
            } catch (Exception ignore2) {
                return uuid
            }
        }
    }
    
    switch(className?.toLowerCase()) {
        case 'employee': case 'emp': return "employee\$${uuid}"
        case 'organizationalunit': case 'ou': case 'department': return "ou\$${uuid}"
        case 'team': case 'workgroup': return "team\$${uuid}"
        default: return "${className}\$${uuid}"
    }
}

def compareUuid = { String uuid1, String uuid2 ->
    if (!uuid1 || !uuid2) return false
    
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

def normalizeFio = { String s ->
    return (s ?: '')
        .replace('\u00A0', ' ')
        .replaceAll(/\s+/, ' ')
        .replace('ё', 'е').replace('Ё', 'Е')
        .trim()
}

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

def findEmployeeByFio = { String fioInput ->
    try {
        def fio = normalizeFio(fioInput)
        if (!fio) return null
        
        sayDbg("Ищем: '${fio}'")
        
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
        
        // 3. Разбор на части
        def parts = fio.tokenize(' ')
        String lastName  = parts.size() >= 1 ? parts[0] : null
        String firstName = parts.size() >= 2 ? parts[1] : null
        String middleName = parts.size() >= 3 ? parts[2] : null
        
        if (!lastName || !firstName) {
            sayDbg("Недостаточно частей ФИО: '${fio}'")
            return null
        }
        
        // 4. Поиск по трём полям
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
        
        // 5. Поиск по двум полям
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
        
        // 6. Точное совпадение среди найденных
        List candidates = toObjList(found ?: byTitleLike)
        def exactMatch = candidates.find { emp ->
            normalizeFio(emp?.title ?: '').equalsIgnoreCase(fio)
        }
        if (exactMatch) {
            sayDbg("Точное совпадение: ${exactMatch?.title}")
            return exactMatch
        }
        
        // 7. Расширенный поиск
        try {
            def byLastName = utils.find('employee', [lastName: op.like("%${lastName}%")], sp.ignoreCase())
            List filtered = toObjList(byLastName).findAll { emp ->
                def title = normalizeFio(emp?.title ?: '').toLowerCase()
                return title.contains(firstName.toLowerCase()) &&
                       (!middleName || title.contains(middleName.toLowerCase()))
            }
            
            if (filtered?.size() == 1) {
                sayDbg("Расширенный поиск: ${filtered[0]?.title}")
                return filtered[0]
            }
        } catch (Exception e) {
            sayDbg("Ошибка расширенного поиска: ${e.message}")
        }
        
        // 8. Диагностика
        if (ENABLE_DEBUG_FIND && candidates) {
            def names = candidates.take(5).collect { it?.title ?: "ID:${it?.UUID}" }.join(' | ')
            sayDbg("Множественные кандидаты: ${names}")
        }
        
        return null
        
    } catch (Exception e) {
        sayDbg("Критическая ошибка поиска '${fioInput}': ${e.message}")
        return null
    }
}

/* ===================== РАБОТА СО СТАТУСАМИ ===================== */

def getStatusInfo = { obj ->
    try {
        if (!obj) return ['', '']
        
        def code = ''
        def title = ''
        
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
        sayDbg("Ошибка получения статуса: ${e.message}")
        return ['', '']
    }
}

/* ===================== ОПРЕДЕЛЕНИЕ ЦЕЛИ НАЗНАЧЕНИЯ (ИСПРАВЛЕНО) ===================== */

def pickTarget = { emp ->
    try {
        if (!emp) {
            sayDbg("Нет объекта сотрудника для выбора цели")
            return [null, null, null]
        }
        
        for (priority in TARGET_PRIORITY) {
            if (priority == 'ou') {
                // Безопасное получение OU с поддержкой альтернативных имён
                def ouUuid = safeGetUuid(emp, OU_FIELD_NAMES)
                if (ouUuid) {
                    def normalizedUuid = normalizeUuid(
                        [UUID: ouUuid], 
                        'ou'
                    )
                    if (normalizedUuid) {
                        sayDbg("Выбрано OU: ${normalizedUuid} для ${emp.title}")
                        return [normalizedUuid, OU_TARGET_FIELDS, 'ou']
                    }
                }
            }
            
            if (priority == 'team') {
                // Безопасное получение Team с поддержкой альтернативных имён
                def teamUuid = safeGetUuid(emp, TEAM_FIELD_NAMES)
                if (teamUuid) {
                    def normalizedUuid = normalizeUuid(
                        [UUID: teamUuid], 
                        'team'
                    )
                    if (normalizedUuid) {
                        sayDbg("Выбрано Team: ${normalizedUuid} для ${emp.title}")
                        return [normalizedUuid, TEAM_TARGET_FIELDS, 'team']
                    }
                }
            }
        }
        
        sayDbg("Не найдено OU/Team для: ${emp?.title}")
        return [null, null, null]
        
    } catch (Exception e) {
        say('e', "Ошибка выбора цели для ${emp?.title}: ${e.message}")
        return [null, null, null]
    }
}

/* ===================== НАЗНАЧЕНИЕ ===================== */

def alreadyAssignedTo = { obj, String field, String targetUuid ->
    try {
        def value = obj."${field}"
        if (!value) return false
        
        def currentUuid = (value?.UUID ?: (value instanceof String ? value : null))?.toString()
        if (!currentUuid) return false
        
        boolean isAssigned = compareUuid(currentUuid, targetUuid)
        
        if (isAssigned) {
            sayDbg("Уже назначено: ${obj.UUID} -> ${field}=${currentUuid}")
        }
        
        return isAssigned
        
    } catch (Exception e) {
        sayDbg("Ошибка проверки ${field}: ${e.message}")
        return false
    }
}

def tryAssign = { obj, List<String> fields, String targetUuid ->
    for (field in fields) {
        try {
            if (alreadyAssignedTo(obj, field, targetUuid)) {
                return 'already_assigned'
            }
            
            if (DRY_RUN) {
                say('i', "DRY: ${obj.UUID} -> ${field} := ${targetUuid}")
                return 'assigned'
            } else {
                inTx { 
                    utils.edit(obj, [(field): targetUuid]) 
                }
                say('i', "OK: ${obj.UUID} -> ${field} := ${targetUuid}")
                return 'assigned'
            }
            
        } catch (Exception e) {
            sayDbg("Ошибка ${field}=${targetUuid}: ${e.message}")
        }
    }
    
    return 'failed'
}

/* ===================== ЛИЦЕНЗИИ ===================== */

def updateLicense = { emp ->
    try {
        def currentLicense = emp?.license
        boolean alreadyNotLicensed = false
        
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
        
        if (DRY_RUN) {
            say('i', "DRY: ${emp.title} license -> notLicensed")
        } else {
            inTx { 
                utils.edit(emp, [license: 'notLicensed']) 
            }
            say('i', "LICENSE: ${emp.title} -> notLicensed")
        }
        
        return true
        
    } catch (Exception e) {
        say('e', "Ошибка лицензии для ${emp?.title}: ${e.message}")
        return false
    }
}

/* ===================== ОСНОВНОЙ ПРОЦЕСС ===================== */

try {
    say('i', "=== НАЧАЛО ОБРАБОТКИ ===")
    say('i', "Версия: 2.1 для Naumen SD 4.17")
    say('i', "Режим: ${DRY_RUN ? 'DRY_RUN (тестирование)' : 'РЕАЛЬНЫЕ ИЗМЕНЕНИЯ'}")
    
    def fioList = buildFioList(CSV_TEXT)
    say('i', "Извлечено ФИО: ${fioList.size()}")
    
    if (fioList.empty) {
        say('w', "CSV не содержит корректных ФИО")
        return report.toString()
    }
    
    int processed = 0
    int employeesFound = 0
    int employeesNotFound = 0
    int totalTasksReassigned = 0
    int totalLicensesChanged = 0
    int skippedResolved = 0
    int skippedAlreadyAssigned = 0
    
    def notFoundList = []
    def licenseChangedList = []
    
    def employeeCache = [:]
    
    fioList.each { fio ->
        processed++
        
        if (totalTasksReassigned >= MAX_TOTAL_EDITS) {
            say('w', "Лимит переназначений (${MAX_TOTAL_EDITS})")
            return
        }
        
        if (checkTimeout()) return
        
        say('i', "--- ${processed}/${fioList.size()}: ${fio} ---")
        
        def emp = employeeCache[fio]
        if (emp === null) {
            emp = findEmployeeByFio(fio)
            employeeCache[fio] = emp ?: false
        }
        
        if (!emp) {
            employeesNotFound++
            notFoundList << fio
            say('w', "Не найден: ${fio}")
            return
        }
        
        employeesFound++
        say('i', "Найден: ${emp.title} (${emp.UUID})")
        
        def (targetUuid, targetFields, targetKind) = pickTarget(emp)
        
        int tasksReassignedForEmployee = 0
        
        if (targetUuid && targetFields) {
            say('i', "Назначение на ${targetKind.toUpperCase()}: ${targetUuid}")
            
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
            
            say('i', "Связанных объектов: ${relatedObjects.size()}")
            
            relatedObjects.each { obj ->
                if (tasksReassignedForEmployee >= MAX_EDITS_PER_EMPLOYEE ||
                    totalTasksReassigned >= MAX_TOTAL_EDITS) {
                    return
                }
                
                try {
                    def (statusCode, statusTitle) = getStatusInfo(obj)
                    
                    if ((statusCode && SKIP_STATUS_CODES.contains(statusCode)) ||
                        (statusTitle && SKIP_STATUS_TITLES.any { statusTitle.contains(it) })) {
                        skippedResolved++
                        sayDbg("Пропущен (завершён): ${obj.UUID}")
                        return
                    }
                    
                    def assignResult = tryAssign(obj, targetFields, targetUuid)
                    
                    if (assignResult == 'failed') {
                        def altTargetUuid = null
                        def altTargetFields = null
                        
                        if (targetKind == 'ou') {
                            def teamUuid = safeGetUuid(emp, TEAM_FIELD_NAMES)
                            if (teamUuid) {
                                altTargetUuid = normalizeUuid([UUID: teamUuid], 'team')
                                altTargetFields = TEAM_TARGET_FIELDS
                            }
                        } else {
                            def ouUuid = safeGetUuid(emp, OU_FIELD_NAMES)
                            if (ouUuid) {
                                altTargetUuid = normalizeUuid([UUID: ouUuid], 'ou')
                                altTargetFields = OU_TARGET_FIELDS
                            }
                        }
                        
                        if (altTargetUuid && altTargetFields) {
                            say('i', "Альтернативное назначение: ${altTargetUuid}")
                            assignResult = tryAssign(obj, altTargetFields, altTargetUuid)
                        }
                    }
                    
                    if (assignResult == 'assigned') {
                        tasksReassignedForEmployee++
                        totalTasksReassigned++
                        sleepIfNeeded()
                    } else if (assignResult == 'already_assigned') {
                        skippedAlreadyAssigned++
                    }
                    
                } catch (Exception e) {
                    say('e', "Ошибка обработки ${obj?.UUID}: ${e.message}")
                }
            }
            
        } else {
            say('w', "Нет OU/Team для: ${emp.title}")
        }
        
        say('i', "Переназначено для сотрудника: ${tasksReassignedForEmployee}")
        
        try {
            if (updateLicense(emp)) {
                totalLicensesChanged++
                licenseChangedList << emp.title
            }
        } catch (Exception e) {
            say('e', "Ошибка лицензии: ${e.message}")
        }
        
        sleepIfNeeded()
    }
    
    def processingTime = (System.currentTimeMillis() - startTime) / 1000
    
    say('i', "")
    say('i', "=== ИТОГОВЫЙ ОТЧЁТ ===")
    say('i', "Режим: ${DRY_RUN ? 'DRY_RUN (без изменений)' : 'РЕАЛЬНЫЕ ИЗМЕНЕНИЯ'}")
    say('i', "Время: ${processingTime} сек")
    say('i', "")
    say('i', "📊 СТАТИСТИКА:")
    say('i', "  • Обработано ФИО: ${processed}")
    say('i', "  • Найдено: ${employeesFound}")
    say('i', "  • НЕ найдено: ${employeesNotFound}")
    say('i', "  • Переназначено задач: ${totalTasksReassigned}")
    say('i', "  • Изменено лицензий: ${totalLicensesChanged}")
    say('i', "  • Пропущено (завершённые): ${skippedResolved}")
    say('i', "  • Пропущено (уже назначено): ${skippedAlreadyAssigned}")
    
    if (notFoundList) {
        say('w', "")
        say('w', "❌ НЕ НАЙДЕНЫ (${notFoundList.size()}):")
        notFoundList.each { say('w', "  • ${it}") }
    }
    
    if (licenseChangedList && !DRY_RUN) {
        say('i', "")
        say('i', "🔑 ИЗМЕНЕНЫ ЛИЦЕНЗИИ (${licenseChangedList.size()}):")
        licenseChangedList.take(10).each { say('i', "  • ${it}") }
        if (licenseChangedList.size() > 10) {
            say('i', "  • ... ещё ${licenseChangedList.size() - 10}")
        }
    }
    
    if (DRY_RUN) {
        say('i', "")
        say('i', "🧪 ТЕСТИРОВАНИЕ")
        say('i', "Для реальных изменений: DRY_RUN = false")
    }
    
    say('i', "")
    say('i', "=== ЗАВЕРШЕНО ===")
    
} catch (Exception e) {
    say('e', "КРИТИЧЕСКАЯ ОШИБКА: ${e.message}")
    say('e', "Трассировка: ${e.getStackTrace().take(5).join(' | ')}")
}

return report.toString()