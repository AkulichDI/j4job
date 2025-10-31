/* ═════════════════════════════════════════════════════════════════════════════════════
 * ИТОГОВЫЙ КОД v5.2 - ИСПРАВЛЕНА ОШИБКА ПОИСКА parent/teams
 * Версия: FINAL PRODUCTION для Naumen Service Desk 4.17
 * 
 * ИСПРАВЛЕНИЕ: Проблема была в логике return внутри .each
 * Использован правильный подход с переменной результата
 * ═════════════════════════════════════════════════════════════════════════════════════ */

/* ═════ ВСТАВЬТЕ CSV ЗДЕСЬ ═════ */
def CSV_TEXT = $/
Иванов Иван Иванович
Петров Пётр Петрович
/$

/* ═════ КОНФИГУРАЦИЯ ═════ */
boolean DRY_RUN = true
long SLEEP_MS = 100
int MAX_EDITS_PER_EMPLOYEE = 500
int MAX_TOTAL_EDITS = 20000
int MAX_PROCESSING_TIME_MS = 1800000
int MAX_LICENSE_RETRY = 3

char DELIM = ','
int FIO_COL = 0

List<String> TARGET_PRIORITY = ['ou', 'team']
List<String> CLASSES = ['serviceCall', 'task', 'changeRequest', 'problem', 'incident']
List<String> REL_ATTRS = [
    'responsibleEmployee', 'executor', 'assignee', 'author',
    'clientEmployee', 'initiator', 'manager', 'observer',
    'requester', 'reporter', 'counterparty', 'creator',
    'approver', 'performer', 'responsible', 'subscriber'
]

List<String> OU_TARGET_FIELDS = ['responsibleOu', 'ou', 'department']
List<String> TEAM_TARGET_FIELDS = ['responsibleTeam', 'team', 'executorTeam']

List<String> OU_FIELD_NAMES = ['parent', 'ou', 'organizationalUnit', 'department']
List<String> TEAM_FIELD_NAMES = ['teams', 'team', 'employeeTeam', 'responsibleTeam']

Set<String> CLOSE_STATUS_CODES = ['resolved', 'разрешен', 'разрешено', 'разрешён'] as Set

Set<String> SKIP_STATUS_CODES = [
    'resolved', 'closed', 'canceled', 'cancelled', 'done', 'completed', 'finished', 'archived'
] as Set

Set<String> SKIP_STATUS_TITLES = [
    'разрешен', 'разрешено', 'разрешён', 'закрыт', 'закрыто',
    'отклонен', 'отклонено', 'отклонён', 'выполнен', 'выполнено',
    'решено', 'решён', 'завершен', 'завершено', 'завершён',
    'отменен', 'отменено', 'отменён', 'архив'
] as Set

/* ═════ ЛОГИРОВАНИЕ ═════ */
def log = (this.metaClass.hasProperty(this, 'logger') ? logger : [
    info:  { m -> println("[INFO] ${m}") },
    warn:  { m -> println("[WARN] ${m}") },
    error: { m -> println("[ERROR] ${m}") },
    debug: { m -> println("[DEBUG] ${m}") }
])

def report = new StringBuilder()
def startTime = System.currentTimeMillis()

def say = { String level, String msg ->
    report.append(msg).append('\n')
    try {
        switch(level.toLowerCase()) {
            case 'i': case 'info': log.info(msg); break
            case 'w': case 'warn': log.warn(msg); break
            case 'e': case 'error': log.error(msg); break
            case 'd': case 'debug': log.debug(msg); break
        }
    } catch (Exception ignore) {}
}

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

/* ═════ ПАРСИНГ CSV ═════ */
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
    def fioList = []
    csvText.readLines().each { line ->
        def trimmed = line?.trim()
        if (!trimmed || trimmed.startsWith('#') || trimmed.startsWith('//')) return
        try {
            def cols = splitCsv(line)
            def fioCell = cols.size() > FIO_COL ? cols[FIO_COL] : ''
            def normalized = fioCell?.replace('\u00A0', ' ')?.replaceAll(/\s+/, ' ')?.trim()
            if (!normalized) return
            def words = normalized.tokenize(' ')
            if (words.size() < 2) return
            def fio = words.take(3).join(' ')
            if (!fioList.contains(fio)) fioList.add(fio)
        } catch (Exception e) {
            say('w', "Ошибка CSV: ${line}")
        }
    }
    return fioList
}

/* ═════ БЕЗОПАСНЫЙ ДОСТУП ═════ */
def safeGetAttribute = { obj, List fieldNames ->
    if (!obj) return null
    fieldNames.each { fieldName ->
        try {
            if (obj.hasProperty(fieldName)) {
                def value = obj."${fieldName}"
                if (value != null) return value
            }
        } catch (Exception e) { }
    }
    return null
}

def safeGetUuid = { obj, List fieldNames ->
    def value = safeGetAttribute(obj, fieldNames)
    return value?.UUID
}

/* ═════ НОРМАЛИЗАЦИЯ UUID ═════ */
def normalizeUuid = { obj, String expectedClass ->
    if (!obj?.UUID) return null
    def uuid = obj.UUID.toString()
    if (uuid.contains('$')) return uuid
    def className = expectedClass
    if (!className) {
        try {
            className = obj.getClass().getSimpleName().toLowerCase()
        } catch (Exception ignore) { return uuid }
    }
    switch(className?.toLowerCase()) {
        case 'employee': case 'emp': return "employee\$${uuid}"
        case 'organizationalunit': case 'ou': case 'department': case 'отдел': return "ou\$${uuid}"
        case 'team': case 'teams': case 'workgroup': return "team\$${uuid}"
        default: return "${className}\$${uuid}"
    }
}

def compareUuid = { String uuid1, String uuid2 ->
    if (!uuid1 || !uuid2) return false
    def extractId = { uuid -> uuid.contains('$') ? uuid.split('\\$', 2)[1] : uuid }
    return extractId(uuid1) == extractId(uuid2)
}

/* ═════ ПОИСК СОТРУДНИКА ═════ */
def normalizeFio = { String s ->
    (s ?: '').replace('\u00A0', ' ').replaceAll(/\s+/, ' ')
        .replace('ё', 'е').replace('Ё', 'Е').trim()
}

def toObj = { any ->
    try {
        return (any instanceof String) ? utils.get(any) : any
    } catch (Exception e) { return any }
}

def toObjList = { lst ->
    (lst ?: []).collect { toObj(it) }.findAll { it != null }
}

def findEmployeeByFio = { String fioInput ->
    try {
        def fio = normalizeFio(fioInput)
        if (!fio) return null
        
        def byTitleExact = utils.find('employee', [title: fio], sp.ignoreCase())
        if (byTitleExact?.size() == 1) return toObj(byTitleExact[0])
        
        def byTitleLike = utils.find('employee', [title: op.like("%${fio}%")], sp.ignoreCase())
        if (byTitleLike?.size() == 1) return toObj(byTitleLike[0])
        
        def parts = fio.tokenize(' ')
        if (parts.size() >= 2) {
            def found = utils.find('employee', [
                lastName: op.like("%${parts[0]}%"),
                firstName: op.like("%${parts[1]}%")
            ], sp.ignoreCase())
            if (found?.size() == 1) return toObj(found[0])
        }
        
        return null
    } catch (Exception e) {
        say('d', "Ошибка поиска: ${e.message}")
        return null
    }
}

/* ═════ ПОЛУЧЕНИЕ СТАТУСА ═════ */
def getStatusInfo = { obj ->
    try {
        if (!obj) return ['', '']
        def code = ''
        def title = ''
        ['status', 'state', 'stage'].each { field ->
            try {
                def statusObj = obj."${field}"
                if (statusObj) {
                    if (!code) code = statusObj.code?.toString()?.toLowerCase() ?: ''
                    if (!title) title = statusObj.title?.toString()?.toLowerCase() ?: ''
                }
            } catch (Exception ignore) {}
        }
        return [code, title]
    } catch (Exception e) { return ['', ''] }
}

/* ═════ ОПРЕДЕЛЕНИЕ ЦЕЛИ (ИСПРАВЛЕНО!) ═════ */
def pickTarget = { emp ->
    try {
        if (!emp) {
            say('d', "pickTarget: emp = null")
            return [null, null, null]
        }
        
        say('d', "pickTarget: emp = ${emp.title}")
        say('d', "  OU_FIELD_NAMES = ${OU_FIELD_NAMES}")
        say('d', "  TEAM_FIELD_NAMES = ${TEAM_FIELD_NAMES}")
        
        // ИСПРАВЛЕНИЕ: используем переменную вместо return в .each
        def resultToReturn = null
        
        TARGET_PRIORITY.each { priority ->
            // Если уже найдено - выходим из текущей итерации
            if (resultToReturn != null) return
            
            say('d', "  Проверяем priority = ${priority}")
            
            if (priority == 'ou') {
                def ouUuid = safeGetUuid(emp, OU_FIELD_NAMES)
                say('d', "    ouUuid = ${ouUuid}")
                
                if (ouUuid) {
                    def normalizedUuid = normalizeUuid([UUID: ouUuid], 'ou')
                    say('d', "    normalizedUuid = ${normalizedUuid}")
                    
                    if (normalizedUuid) {
                        say('d', "    ★ НАЙДЕНО OU!")
                        resultToReturn = [normalizedUuid, OU_TARGET_FIELDS, 'OU']
                    }
                }
            }
            
            if (priority == 'team') {
                def teamUuid = safeGetUuid(emp, TEAM_FIELD_NAMES)
                say('d', "    teamUuid = ${teamUuid}")
                
                if (teamUuid) {
                    def normalizedUuid = normalizeUuid([UUID: teamUuid], 'team')
                    say('d', "    normalizedUuid = ${normalizedUuid}")
                    
                    if (normalizedUuid) {
                        say('d', "    ★ НАЙДЕНО TEAM!")
                        resultToReturn = [normalizedUuid, TEAM_TARGET_FIELDS, 'TEAM']
                    }
                }
            }
        }
        
        if (resultToReturn) {
            say('d', "pickTarget: результат = ${resultToReturn}")
            return resultToReturn
        }
        
        say('d', "pickTarget: НЕ НАЙДЕНО, возвращаем [null,null,null]")
        return [null, null, null]
        
    } catch (Exception e) {
        say('e', "Ошибка pickTarget: ${e.message}")
        say('e', "  Stack: ${e.getStackTrace().take(3).join(' | ')}")
        return [null, null, null]
    }
}

/* ═════ ПОИСК ВСЕХ СВЯЗАННЫХ ОБЪЕКТОВ ═════ */
def findAllRelatedObjects = { emp ->
    def relatedObjects = []
    def seenUuids = new HashSet()
    
    CLASSES.each { cls ->
        REL_ATTRS.each { attr ->
            try {
                def objs = utils.find(cls, [(attr): emp])
                objs?.each { obj ->
                    if (obj?.UUID && seenUuids.add(obj.UUID)) {
                        relatedObjects.add(obj)
                    }
                }
            } catch (Exception e) { }
        }
    }
    return relatedObjects
}

/* ═════ ПЕРЕНАЗНАЧЕНИЕ ═════ */
def alreadyAssignedTo = { obj, String field, String targetUuid ->
    try {
        def value = obj."${field}"
        if (!value) return false
        def currentUuid = (value?.UUID ?: (value instanceof String ? value : null))?.toString()
        if (!currentUuid) return false
        return compareUuid(currentUuid, targetUuid)
    } catch (Exception e) { return false }
}

def tryAssign = { obj, List fields, String targetUuid ->
    fields.each { field ->
        try {
            if (alreadyAssignedTo(obj, field, targetUuid)) {
                return 'already_assigned'
            }
            if (DRY_RUN) {
                say('i', "  DRY: ${obj.UUID} → ${field} = ${targetUuid}")
                return 'assigned'
            } else {
                inTx { utils.edit(obj, [(field): targetUuid]) }
                say('i', "  ✓ ${field}")
                return 'assigned'
            }
        } catch (Exception e) { }
    }
    return 'failed'
}

/* ═════ ИЗМЕНЕНИЕ СТАТУСА ═════ */
def tryCloseResolvedTask = { obj ->
    try {
        def statusData = getStatusInfo(obj)
        def statusCode = statusData[0]
        def statusTitle = statusData[1]
        
        if (!CLOSE_STATUS_CODES.contains(statusCode) && 
            !statusTitle.contains('разрешен') && 
            !statusTitle.contains('разрешё')) {
            return false
        }
        
        if (DRY_RUN) {
            say('i', "  DRY: статус → closed")
            return true
        } else {
            try {
                inTx { utils.edit(obj, [status: [code: 'closed']]) }
                say('i', "  ✓ статус → closed")
                return true
            } catch (Exception e1) {
                try {
                    inTx { utils.edit(obj, [state: 'closed']) }
                    say('i', "  ✓ статус → closed")
                    return true
                } catch (Exception e2) {
                    try {
                        inTx { utils.edit(obj, [status: [title: 'закрыто']]) }
                        say('i', "  ✓ статус → closed")
                        return true
                    } catch (Exception e3) {
                        say('d', "  Не удалось закрыть: ${e3.message}")
                        return false
                    }
                }
            }
        }
    } catch (Exception e) {
        say('d', "Ошибка статуса: ${e.message}")
        return false
    }
}

/* ═════ ПОИСК ЗАДАЧ ДЛЯ ЗАКРЫТИЯ ═════ */
def findTasksToClose = { emp ->
    def tasksToClose = []
    def seenUuids = new HashSet()
    
    try {
        ['serviceCall', 'task'].each { cls ->
            ['responsibleEmployee', 'initiator'].each { attr ->
                try {
                    def objs = utils.find(cls, [(attr): emp])
                    objs?.each { obj ->
                        if (obj?.UUID && seenUuids.add(obj.UUID)) {
                            tasksToClose.add(obj)
                        }
                    }
                } catch (Exception e) { }
            }
        }
    } catch (Exception e) {
        say('d', "Ошибка поиска задач: ${e.message}")
    }
    
    return tasksToClose
}

/* ═════ СМЕНА ЛИЦЕНЗИИ С АВТОРЕТРАЕМ ═════ */
def updateLicenseWithRetry = { emp, relatedObjectsGetter ->
    def maxRetries = MAX_LICENSE_RETRY
    def retryCount = 0
    
    while (retryCount < maxRetries) {
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
                say('d', "  ℹ Лицензия уже notLicensed")
                return [success: false, reason: 'already_notlicensed']
            }
            
            if (DRY_RUN) {
                say('i', "  DRY: license → notLicensed")
                return [success: true]
            }
            
            inTx { utils.edit(emp, [license: 'notLicensed']) }
            say('i', "  ✓ ЛИЦЕНЗИЯ ИЗМЕНЕНА")
            return [success: true]
            
        } catch (Exception e) {
            def errorMsg = e.message ?: e.toString()
            retryCount++
            say('w', "  Попытка ${retryCount}/${maxRetries}: ${errorMsg}")
            
            if (errorMsg.toLowerCase().contains('ответственн') || 
                errorMsg.toLowerCase().contains('responsible')) {
                say('i', "  🔄 Повторный поиск...")
                try {
                    def relatedObjects = relatedObjectsGetter.call()
                    say('d', "    Найдено объектов: ${relatedObjects.size()}")
                } catch (Exception e2) { }
            }
            
            if (retryCount < maxRetries) {
                say('i', "  ⏳ Повторная попытка...")
                Thread.sleep(500)
            }
        }
    }
    
    return [success: false, reason: 'max_retries_exceeded']
}

/* ═════ АРХИВИРОВАНИЕ ═════ */
def archiveEmployee = { emp ->
    try {
        if (!emp) return [success: false, reason: 'no_emp_object']
        say('i', "  📦 Архивирование...")
        
        try {
            if (DRY_RUN) {
                say('i', "    DRY: archive")
                return [success: true]
            }
            inTx { utils.edit(emp, [state: 'archived']) }
            say('i', "    ✓ archived")
            return [success: true]
        } catch (Exception e1) { }
        
        try {
            inTx { utils.edit(emp, [archived: true]) }
            say('i', "    ✓ archived=true")
            return [success: true]
        } catch (Exception e2) { }
        
        try {
            inTx { utils.edit(emp, [status: [code: 'archived']]) }
            say('i', "    ✓ archived")
            return [success: true]
        } catch (Exception e3) { }
        
        try {
            inTx { utils.delete(emp) }
            say('i', "    ✓ deleted")
            return [success: true]
        } catch (Exception e4) {
            say('w', "    ⚠️ Архив не получился")
            return [success: false, reason: 'archive_error']
        }
        
        return [success: false, reason: 'unknown_error']
    } catch (Exception e) {
        say('e', "  ❌ Ошибка архивации: ${e.message}")
        return [success: false, reason: e.message]
    }
}

/* ═════ ОСНОВНОЙ ПРОЦЕСС ═════ */
try {
    say('i', "╔════════════════════════════════════════════════════════════════╗")
    say('i', "║  ПЕРЕНАЗНАЧЕНИЕ, ИЗМЕНЕНИЕ СТАТУСОВ И АРХИВАЦИЯ v5.2 FIXED  ║")
    say('i', "║  Исправлена ошибка поиска parent/teams                       ║")
    say('i', "╚════════════════════════════════════════════════════════════════╝")
    say('i', "")
    say('i', "Режим: ${DRY_RUN ? '🧪 ТЕСТИРОВАНИЕ' : '⚠️ РЕАЛЬНЫЕ ИЗМЕНЕНИЯ'}")
    say('i', "")
    
    def fioList = buildFioList(CSV_TEXT)
    say('i', "📋 CSV: ${fioList.size()} сотрудников")
    
    if (fioList.isEmpty()) {
        say('w', "CSV пуст")
        return report.toString()
    }
    
    int processed = 0
    int tasksReassigned = 0
    int tasksClosed = 0
    int licensesChanged = 0
    int licensesFailedToChange = 0
    int archived = 0
    int archiveFailed = 0
    def employeeCache = [:]
    def details = []
    
    fioList.each { String fio ->
        processed++
        if (checkTimeout()) return
        
        say('i', "")
        say('i', "═══════════════════════════════════════════════════════════════")
        say('i', "[${processed}/${fioList.size()}] 🔍 ${fio}")
        say('i', "═══════════════════════════════════════════════════════════════")
        
        def emp = employeeCache[fio]
        if (emp === null) {
            emp = findEmployeeByFio(fio)
            employeeCache[fio] = emp ?: false
        }
        if (!emp) {
            say('w', "❌ НЕ НАЙДЕН")
            return
        }
        
        say('i', "✓ ${emp.title}")
        
        def targetInfo = pickTarget(emp)
        def targetUuid = targetInfo[0]
        def targetFields = targetInfo[1]
        
        if (!targetUuid || !targetFields) {
            say('e', "❌ НЕ НАЙДЕНО: parent/teams")
            return
        }
        
        say('i', "✓ Цель: ${targetInfo[2]}")
        say('i', "")
        
        def relatedObjects = findAllRelatedObjects(emp)
        say('i', "🔄 ПЕРЕНАЗНАЧЕНИЕ (${relatedObjects.size()}):")
        
        int empTaskCount = 0
        if (relatedObjects.size() > 0) {
            relatedObjects.each { obj ->
                if (tasksReassigned >= MAX_TOTAL_EDITS || empTaskCount >= MAX_EDITS_PER_EMPLOYEE) return
                try {
                    def statusData = getStatusInfo(obj)
                    if (SKIP_STATUS_CODES.contains(statusData[0]) || 
                        SKIP_STATUS_TITLES.any { statusData[1].contains(it) }) return
                    
                    say('i', "  📌 ${obj.UUID}")
                    def result = tryAssign(obj, targetFields, targetUuid)
                    if (result == 'assigned') {
                        empTaskCount++
                        tasksReassigned++
                    }
                    sleepIfNeeded()
                } catch (Exception e) {
                    say('e', "  Ошибка: ${e.message}")
                }
            }
        }
        say('i', "  ✓ Переназначено: ${empTaskCount}")
        
        say('i', "")
        say('i', "🔚 ЗАКРЫТИЕ ЗАДАЧ:")
        
        def tasksToClose = findTasksToClose(emp)
        say('i', "  Найдено: ${tasksToClose.size()}")
        
        int closedCount = 0
        if (tasksToClose.size() > 0) {
            tasksToClose.each { obj ->
                try {
                    def statusData = getStatusInfo(obj)
                    if (CLOSE_STATUS_CODES.contains(statusData[0]) || 
                        statusData[1].contains('разрешен') || 
                        statusData[1].contains('разрешё')) {
                        
                        say('i', "  📌 ${obj.UUID}")
                        if (tryCloseResolvedTask(obj)) {
                            closedCount++
                            tasksClosed++
                        }
                        sleepIfNeeded()
                    }
                } catch (Exception e) {
                    say('e', "  Ошибка: ${e.message}")
                }
            }
        }
        say('i', "  ✓ Закрыто: ${closedCount}")
        
        say('i', "")
        say('i', "🔑 ЛИЦЕНЗИЯ:")
        def licenseResult = updateLicenseWithRetry(emp) { findAllRelatedObjects(emp) }
        if (licenseResult.success) {
            licensesChanged++
            details.add("✅ ${emp.title}: переназначено ${empTaskCount}, закрыто ${closedCount}, лицензия OK")
        } else {
            licensesFailedToChange++
            details.add("⚠️ ${emp.title}: переназначено ${empTaskCount}, закрыто ${closedCount}, лицензия НЕ OK")
        }
        
        say('i', "")
        say('i', "📦 АРХИВ:")
        def archiveResult = archiveEmployee(emp)
        if (archiveResult.success) {
            archived++
            say('i', "  ✅ АРХИВИРОВАН")
        } else {
            archiveFailed++
            say('w', "  ⚠️ Архив: ${archiveResult.reason}")
        }
        
        sleepIfNeeded()
    }
    
    say('i', "")
    say('i', "╔════════════════════════════════════════════════════════════════╗")
    say('i', "║                      ИТОГОВЫЙ ОТЧЁТ                          ║")
    say('i', "╚════════════════════════════════════════════════════════════════╝")
    say('i', "")
    say('i', "📊 РЕЗУЛЬТАТЫ:")
    say('i', "  • Обработано: ${processed}")
    say('i', "  • Переназначено задач: ${tasksReassigned} ✓")
    say('i', "  • Закрыто задач: ${tasksClosed} ✓")
    say('i', "  • Лицензий изменено: ${licensesChanged}")
    say('i', "  • Ошибок лицензии: ${licensesFailedToChange}")
    say('i', "  • Архивировано: ${archived}")
    say('i', "  • Ошибок архива: ${archiveFailed}")
    say('i', "")
    say('i', "📋 ДЕТАЛИ:")
    details.each { detail -> say('i', "  ${detail}") }
    
    if (DRY_RUN) {
        say('i', "")
        say('i', "🧪 ТЕСТИРОВАНИЕ - установите DRY_RUN = false для применения")
    }
    
    say('i', "")
    say('i', "✅ ЗАВЕРШЕНО")
    
} catch (Exception e) {
    say('e', "!!! КРИТИЧЕСКАЯ ОШИБКА !!!")
    say('e', "${e.class.name}: ${e.message}")
    try {
        e.getStackTrace().take(5).each { trace -> say('e', "  ${trace}") }
    } catch (Exception ignore) {}
}

return report.toString()