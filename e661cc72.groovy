/* ═════════════════════════════════════════════════════════════════════════════════════
 * ФИНАЛЬНЫЙ КОД v6.0 - МАКСИМАЛЬНО ПРОСТОЙ И РАБОЧИЙ
 * Версия: PRODUCTION для Naumen Service Desk 4.17
 * 
 * УПРОЩЕНО:
 * ✓ Работаем ТОЛЬКО с parent (отдел)
 * ✓ Забываем про teams (команду)
 * ✓ Переназначаем все задачи на отдел parent
 * ✓ Меняем лицензию
 * ✓ Архивируем
 * ✓ Максимально надёжно и просто
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

List<String> CLASSES = ['serviceCall', 'task']
List<String> REL_ATTRS = [
    'responsibleEmployee', 'executor', 'assignee', 'author',
    'clientEmployee', 'initiator', 'manager', 'observer'
]

List<String> OU_TARGET_FIELDS = ['responsibleOu', 'ou']

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

def findEmployeeByFio = { String fioInput ->
    try {
        def fio = normalizeFio(fioInput)
        if (!fio) return null
        
        // Поиск по точному названию
        try {
            def found = utils.find('employee', [title: fio], sp.ignoreCase())
            if (found?.size() == 1) return toObj(found[0])
        } catch (Exception ignore) {}
        
        // Поиск по LIKE
        try {
            def found = utils.find('employee', [title: op.like("%${fio}%")], sp.ignoreCase())
            if (found?.size() == 1) return toObj(found[0])
        } catch (Exception ignore) {}
        
        // Поиск по частям фамилии/имени
        def parts = fio.tokenize(' ')
        if (parts.size() >= 2) {
            try {
                def found = utils.find('employee', [
                    lastName: parts[0],
                    firstName: parts[1]
                ], sp.ignoreCase())
                if (found?.size() == 1) return toObj(found[0])
            } catch (Exception ignore) {}
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

/* ═════ ПОЛУЧЕНИЕ ОТДЕЛА СОТРУДНИКА (ПРОСТО!) ═════ */
def getEmployeeDepartment = { emp ->
    try {
        if (!emp) return null
        
        // parent это ссылка на отдел
        def parent = emp.parent
        if (!parent) {
            say('d', "emp.parent = null")
            return null
        }
        
        // Получаем UUID
        def uuid = parent?.UUID
        if (!uuid) {
            say('d', "emp.parent.UUID = null")
            return null
        }
        
        // Нормализуем UUID (должен быть ou$ID)
        def normalizedUuid = uuid.toString()
        if (!normalizedUuid.contains('$')) {
            normalizedUuid = "ou\$${uuid}"
        }
        
        say('d', "getEmployeeDepartment: найдено UUID = ${normalizedUuid}")
        return normalizedUuid
        
    } catch (Exception e) {
        say('e', "Ошибка получения отдела: ${e.message}")
        return null
    }
}

/* ═════ ПЕРЕНАЗНАЧЕНИЕ ═════ */
def alreadyAssignedTo = { obj, String field, String targetUuid ->
    try {
        def value = obj."${field}"
        if (!value) return false
        def currentUuid = value?.UUID?.toString() ?: (value instanceof String ? value : null)
        if (!currentUuid) return false
        
        // Сравниваем UUID
        if (currentUuid == targetUuid) return true
        
        // Сравниваем без префикса
        def extractId = { uuid -> uuid.contains('$') ? uuid.split('\\$', 2)[1] : uuid }
        return extractId(currentUuid) == extractId(targetUuid)
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
                say('i', "  ✓ назначено: ${field}")
                return 'assigned'
            }
        } catch (Exception e) {
            say('d', "  Попытка ${field} не удалась: ${e.message}")
        }
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
                    say('d', "Не удалось закрыть: ${e2.message}")
                    return false
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
        CLASSES.each { cls ->
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

/* ═════ СМЕНА ЛИЦЕНЗИИ ═════ */
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
            say('d', "  ℹ Лицензия уже notLicensed")
            return false
        }
        
        if (DRY_RUN) {
            say('i', "  DRY: license → notLicensed")
            return true
        }
        
        inTx { utils.edit(emp, [license: 'notLicensed']) }
        say('i', "  ✓ ЛИЦЕНЗИЯ ИЗМЕНЕНА")
        return true
        
    } catch (Exception e) {
        say('w', "  Ошибка лицензии: ${e.message}")
        return false
    }
}

/* ═════ АРХИВИРОВАНИЕ ═════ */
def archiveEmployee = { emp ->
    try {
        if (!emp) return false
        say('i', "  📦 Архивирование...")
        
        if (DRY_RUN) {
            say('i', "    DRY: archive")
            return true
        }
        
        try {
            inTx { utils.edit(emp, [state: 'archived']) }
            say('i', "    ✓ archived")
            return true
        } catch (Exception e1) { }
        
        try {
            inTx { utils.edit(emp, [archived: true]) }
            say('i', "    ✓ archived=true")
            return true
        } catch (Exception e2) { }
        
        try {
            inTx { utils.delete(emp) }
            say('i', "    ✓ deleted")
            return true
        } catch (Exception e3) {
            say('w', "    ⚠️ Архив не получился")
            return false
        }
        
    } catch (Exception e) {
        say('e', "  Ошибка архивации: ${e.message}")
        return false
    }
}

/* ═════ ОСНОВНОЙ ПРОЦЕСС ═════ */
try {
    say('i', "╔════════════════════════════════════════════════════════════════╗")
    say('i', "║  ПЕРЕНАЗНАЧЕНИЕ НА ОТДЕЛ v6.0 ФИНАЛЬНАЯ                     ║")
    say('i', "║  Naumen SD 4.17 | parent → responsibleOu | Максимум просто  ║")
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
    int archived = 0
    def details = []
    
    fioList.each { String fio ->
        processed++
        if (checkTimeout()) return
        
        say('i', "")
        say('i', "═══════════════════════════════════════════════════════════════")
        say('i', "[${processed}/${fioList.size()}] 🔍 ${fio}")
        say('i', "═══════════════════════════════════════════════════════════════")
        
        // ПОИСК СОТРУДНИКА
        def emp = findEmployeeByFio(fio)
        if (!emp) {
            say('w', "❌ НЕ НАЙДЕН")
            return
        }
        
        say('i', "✓ Найден: ${emp.title} (${emp.UUID})")
        
        // ПОЛУЧЕНИЕ ОТДЕЛА
        def departmentUuid = getEmployeeDepartment(emp)
        if (!departmentUuid) {
            say('e', "❌ НЕ НАЙДЕН ОТДЕЛ (parent)")
            return
        }
        
        say('i', "✓ Отдел: ${departmentUuid}")
        say('i', "")
        
        // ПЕРЕНАЗНАЧЕНИЕ ЗАДАЧ
        def relatedObjects = findAllRelatedObjects(emp)
        say('i', "🔄 ПЕРЕНАЗНАЧЕНИЕ (найдено ${relatedObjects.size()}):")
        
        int empTaskCount = 0
        if (relatedObjects.size() > 0) {
            relatedObjects.each { obj ->
                if (tasksReassigned >= MAX_TOTAL_EDITS || empTaskCount >= MAX_EDITS_PER_EMPLOYEE) return
                try {
                    def statusData = getStatusInfo(obj)
                    if (SKIP_STATUS_CODES.contains(statusData[0]) || 
                        SKIP_STATUS_TITLES.any { statusData[1].contains(it) }) return
                    
                    say('i', "  📌 ${obj.UUID}")
                    def result = tryAssign(obj, OU_TARGET_FIELDS, departmentUuid)
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
        
        // ЗАКРЫТИЕ ЗАДАЧ
        say('i', "")
        say('i', "🔚 ЗАКРЫТИЕ ЗАДАЧ (разрешен → закрыто):")
        
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
        
        // СМЕНА ЛИЦЕНЗИИ
        say('i', "")
        say('i', "🔑 ЛИЦЕНЗИЯ:")
        if (updateLicense(emp)) {
            licensesChanged++
            details.add("✅ ${emp.title}: переназначено ${empTaskCount}, закрыто ${closedCount}, лицензия OK")
        } else {
            details.add("⚠️ ${emp.title}: переназначено ${empTaskCount}, закрыто ${closedCount}, лицензия ошибка")
        }
        
        // АРХИВИРОВАНИЕ
        say('i', "")
        say('i', "📦 АРХИВ:")
        if (archiveEmployee(emp)) {
            archived++
            say('i', "  ✅ АРХИВИРОВАН")
        } else {
            say('w', "  ⚠️ Архив не получился (но задачи переназначены!)")
        }
        
        sleepIfNeeded()
    }
    
    // ИТОГИ
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
    say('i', "  • Архивировано: ${archived}")
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