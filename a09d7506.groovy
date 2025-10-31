import com.naumen.core.shared.dto.IDto
import com.naumen.core.server.query.QueryPool

boolean DRY_RUN = true

String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович'''

long SCRIPT_START_TIME = System.currentTimeMillis()
long TIMEOUT_MS = 4 * 60 * 1000

class Report {
    int found = 0
    int notfound = 0
    int closed = 0
    int reassigned = 0
    int licensesChanged = 0
    int licensesFailed = 0
    int archived = 0
    int notArchived = 0
    int errors = 0
    int processed = 0
    int skipped = 0
    boolean timeout = false
    
    List<String> notArchivedNames = []
    List<String> licenseFailedNames = []
    List<String> errorNames = []
    List<String> timeoutNames = []
    
    long executionTimeMs = 0
    
    String getSummary() {
        long seconds = executionTimeMs / 1000
        String timeInfo = "${seconds}сек"
        
        StringBuilder result = new StringBuilder()
        result.append("Обработано: $processed из ${processed + skipped}\n")
        result.append("Найдено: $found\n")
        result.append("Не найдено: $notfound\n")
        result.append("Закрыто разрешенных задач: $closed\n")
        result.append("Переназначено открытых задач: $reassigned\n")
        result.append("Ошибок: $errors\n")
        result.append("Время выполнения: $timeInfo\n")
        result.append("\n=== ЛИЦЕНЗИИ ===\n")
        result.append("Изменено успешно: $licensesChanged\n")
        result.append("Не удалось снять: $licensesFailed\n")
        if (licenseFailedNames.size() > 0) {
            licenseFailedNames.each { name ->
                result.append("  - $name\n")
            }
        }
        
        result.append("\n=== АРХИВИРОВАНИЕ ===\n")
        result.append("Архивировано успешно: $archived\n")
        result.append("Не архивировано: $notArchived\n")
        if (notArchivedNames.size() > 0) {
            notArchivedNames.each { name ->
                result.append("  - $name\n")
            }
        }
        
        if (timeout) {
            result.append("\n=== ТАЙМАУТ ===\n")
            result.append("Скрипт прерван по времени\n")
            result.append("Не обработано: $skipped\n")
            if (timeoutNames.size() > 0) {
                timeoutNames.each { name ->
                    result.append("  - $name\n")
                }
            }
        }
        
        if (errorNames.size() > 0) {
            result.append("\n=== ОШИБКИ ===\n")
            errorNames.each { name ->
                result.append("  - $name\n")
            }
        }
        
        return result.toString()
    }
}

Report report = new Report()

boolean checkTimeout() {
    long elapsed = System.currentTimeMillis() - SCRIPT_START_TIME
    return elapsed >= TIMEOUT_MS
}

IDto findEmployee(String name) {
    if (!name?.trim() || checkTimeout()) return null
    
    String original = name.trim()
    String normalized = original.replace('ё', 'е').replace('Ё', 'Е').replace('-', ' ').toLowerCase()
    
    List<String> parts = original.split('\\s+')
    String lastName = parts.size() > 0 ? parts[0] : ''
    String firstName = parts.size() > 1 ? parts[1] : ''
    
    try {
        def query1 = QueryPool.query("name = '${original}'").setMaxResults(1).execute()
        if (query1?.list()?.size() > 0) return query1.list()[0]
        
        def query2 = QueryPool.query("name LIKE '%${original}%'").setMaxResults(1).execute()
        if (query2?.list()?.size() > 0) return query2.list()[0]
        
        if (lastName && firstName) {
            def query3 = QueryPool.query("lastName = '${lastName}' AND firstName = '${firstName}'").setMaxResults(1).execute()
            if (query3?.list()?.size() > 0) return query3.list()[0]
        }
        
        def query4 = QueryPool.query("name = '${normalized}'").setMaxResults(1).execute()
        if (query4?.list()?.size() > 0) return query4.list()[0]
        
        def query5 = QueryPool.query("name LIKE '%${normalized}%'").setMaxResults(1).execute()
        if (query5?.list()?.size() > 0) return query5.list()[0]
        
        if (lastName && firstName) {
            String lastNameNorm = lastName.replace('ё', 'е').replace('Ё', 'Е')
            String firstNameNorm = firstName.replace('ё', 'е').replace('Ё', 'Е')
            def query6 = QueryPool.query("lastName = '${lastNameNorm}' AND firstName = '${firstNameNorm}'").setMaxResults(1).execute()
            if (query6?.list()?.size() > 0) return query6.list()[0]
        }
    } catch (Exception e) {
        report.errors++
    }
    
    return null
}

List<IDto> getResolvedTasks(IDto emp) {
    if (!emp?.UUID || checkTimeout()) return []
    
    try {
        String uuid = emp.UUID.toString()
        def result = QueryPool.query("""(executor = '${uuid}' OR responsible = '${uuid}' OR secondaryResponsible = '${uuid}')
            AND (status = 'resolved' OR status = 'разрешен' OR status = 'razreshyonnye')""")
            .setMaxResults(100)
            .execute()
        return result?.list() ?: []
    } catch (Exception e) {
        return []
    }
}

List<IDto> getOpenTasks(IDto emp) {
    if (!emp?.UUID || checkTimeout()) return []
    
    try {
        String uuid = emp.UUID.toString()
        def result = QueryPool.query("""(executor = '${uuid}' OR responsible = '${uuid}' OR secondaryResponsible = '${uuid}')
            AND status != 'closed'
            AND status != 'completed'
            AND status != 'resolved'
            AND status != 'разрешен'
            AND status != 'razreshyonnye'""")
            .setMaxResults(100)
            .execute()
        return result?.list() ?: []
    } catch (Exception e) {
        return []
    }
}

void closeResolvedTasks(List<IDto> tasks) {
    if (checkTimeout()) return
    
    tasks.each { task ->
        if (checkTimeout()) return
        
        try {
            if (!DRY_RUN) {
                task.status = 'closed'
                task.save()
            }
            report.closed++
        } catch (Exception e) {
            report.errors++
        }
    }
}

void reassignOpenTasks(List<IDto> tasks, IDto targetOU) {
    if (checkTimeout() || !targetOU) return
    
    tasks.each { task ->
        if (checkTimeout()) return
        
        try {
            if (!DRY_RUN) {
                if (task.metaClass.hasProperty(task, 'executor')) {
                    task.executor = targetOU
                }
                if (task.metaClass.hasProperty(task, 'responsible')) {
                    task.responsible = targetOU
                }
                if (task.metaClass.hasProperty(task, 'secondaryResponsible')) {
                    task.secondaryResponsible = targetOU
                }
                task.save()
            }
            report.reassigned++
        } catch (Exception e) {
            report.errors++
        }
    }
}

void processEmployee(String name) {
    if (checkTimeout()) {
        report.timeout = true
        report.timeoutNames.add(name)
        report.skipped++
        return
    }
    
    IDto emp = findEmployee(name)
    if (!emp) {
        report.notfound++
        report.errorNames.add(name)
        report.processed++
        return
    }
    
    report.found++
    
    IDto targetOU = null
    try {
        targetOU = emp.parent?.deref()
    } catch (Exception e) {
        report.errors++
        report.errorNames.add(name)
        report.processed++
        emp = null
        return
    }
    
    if (!targetOU) {
        report.errors++
        report.errorNames.add(name)
        report.processed++
        emp = null
        return
    }
    
    if (checkTimeout()) {
        report.timeout = true
        report.timeoutNames.add(name)
        report.skipped++
        emp = null
        targetOU = null
        return
    }
    
    List<IDto> resolvedTasks = getResolvedTasks(emp)
    if (resolvedTasks && resolvedTasks.size() > 0) {
        closeResolvedTasks(resolvedTasks)
    }
    resolvedTasks.clear()
    resolvedTasks = null
    
    if (checkTimeout()) {
        report.timeout = true
        report.timeoutNames.add(name)
        report.skipped++
        emp = null
        targetOU = null
        return
    }
    
    List<IDto> openTasks = getOpenTasks(emp)
    if (openTasks && openTasks.size() > 0) {
        reassignOpenTasks(openTasks, targetOU)
    }
    openTasks.clear()
    openTasks = null
    
    if (checkTimeout()) {
        report.timeout = true
        report.timeoutNames.add(name)
        report.skipped++
        emp = null
        targetOU = null
        return
    }
    
    boolean licenseOk = false
    try {
        if (!DRY_RUN) {
            emp.licenseName = 'notLicensed'
            emp.save()
        }
        report.licensesChanged++
        licenseOk = true
    } catch (Exception e) {
        report.licensesFailed++
        report.licenseFailedNames.add(name)
        report.errors++
        emp = null
        targetOU = null
        report.processed++
        return
    }
    
    if (!licenseOk) {
        emp = null
        targetOU = null
        report.processed++
        return
    }
    
    try {
        if (!DRY_RUN) {
            emp.removed = true
            emp.save()
        }
        report.archived++
    } catch (Exception e) {
        report.notArchived++
        report.notArchivedNames.add(name)
    }
    
    report.processed++
    emp = null
    targetOU = null
}

try {
    List<String> employees = CSV_TEXT
        .split('\n')
        .collect { it.trim() }
        .findAll { it && !it.startsWith('#') }
    
    for (String emp : employees) {
        if (checkTimeout()) {
            report.timeout = true
            break
        }
        
        try {
            processEmployee(emp)
        } catch (Exception e) {
            report.errors++
            report.errorNames.add(emp)
            report.processed++
        }
    }
    
    employees.clear()
    employees = null
    
} catch (Exception e) {
    report.errors++
} finally {
    System.gc()
}

report.executionTimeMs = System.currentTimeMillis() - SCRIPT_START_TIME

report.getSummary()
