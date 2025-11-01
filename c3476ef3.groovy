import com.naumen.core.shared.dto.IDto
import com.naumen.core.server.query.QueryPool

boolean DRY_RUN = true

String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович'''

long TIMEOUT_MS = 4 * 60 * 1000
long startTime = System.currentTimeMillis()

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
    boolean timeout = false
    List<String> notArchivedNames = []
    List<String> licenseFailedNames = []
    List<String> errorNames = []
    
    String getSummary() {
        return """
Найдено сотрудников: $found
Не найдено: $notfound
Закрыто разрешенных задач: $closed
Переназначено открытых задач: $reassigned
Изменено лицензий: $licensesChanged
Не удалось снять лицензию: $licensesFailed
Архивировано: $archived
Не архивировано: $notArchived
Ошибок: $errors
${timeout ? 'TIMEOUT: скрипт прерван по времени' : ''}
${licenseFailedNames.size() > 0 ? 'Не удалось снять лицензию: ' + licenseFailedNames.join(', ') : ''}
${notArchivedNames.size() > 0 ? 'Не попали в архив: ' + notArchivedNames.join(', ') : ''}
${errorNames.size() > 0 ? 'Ошибки при обработке: ' + errorNames.join(', ') : ''}
"""
    }
}

Report report = new Report()

boolean isTimeout() {
    return (System.currentTimeMillis() - startTime) >= TIMEOUT_MS
}

IDto findEmployee(String name) {
    if (!name?.trim() || isTimeout()) return null
    
    String norm = name.trim()
        .replace('ё', 'е')
        .replace('Ё', 'Е')
        .replace('-', ' ')
        .toLowerCase()
    
    try {
        def result = QueryPool.query("name LIKE '%${norm}%'")
            .setMaxResults(1)
            .execute()
        
        if (result?.list()?.size() > 0) {
            return result.list()[0]
        }
    } catch (Exception e) {
        report.errors++
    }
    
    return null
}

List<IDto> getResolvedTasks(IDto emp) {
    if (!emp?.UUID || isTimeout()) return []
    
    try {
        String uuid = emp.UUID.toString()
        String q = """(executor = '${uuid}' OR responsible = '${uuid}' OR secondaryResponsible = '${uuid}')
            AND (status = 'resolved' OR status = 'разрешен' OR status = 'razreshyonnye')"""
        
        def result = QueryPool.query(q)
            .setMaxResults(100)
            .execute()
        
        return result?.list() ?: []
    } catch (Exception e) {
        return []
    }
}

List<IDto> getOpenTasks(IDto emp) {
    if (!emp?.UUID || isTimeout()) return []
    
    try {
        String uuid = emp.UUID.toString()
        String q = """(executor = '${uuid}' OR responsible = '${uuid}' OR secondaryResponsible = '${uuid}')
            AND status != 'closed'
            AND status != 'completed'
            AND status != 'resolved'
            AND status != 'разрешен'
            AND status != 'razreshyonnye'"""
        
        def result = QueryPool.query(q)
            .setMaxResults(100)
            .execute()
        
        return result?.list() ?: []
    } catch (Exception e) {
        return []
    }
}

void closeResolvedTasks(List<IDto> tasks) {
    if (isTimeout()) return
    
    tasks.each { task ->
        if (isTimeout()) return
        
        try {
            if (!DRY_RUN) {
                task.status = 'closed'
                task.save()
            }
            report.closed++
        } catch (Exception e) {
            report.errors++
        } finally {
            task = null
        }
    }
}

void reassignOpenTasks(List<IDto> tasks, IDto ou) {
    if (isTimeout()) return
    
    tasks.each { task ->
        if (isTimeout()) return
        
        try {
            if (!DRY_RUN) {
                if (task.metaClass.hasProperty(task, 'executor')) {
                    task.executor = ou
                }
                if (task.metaClass.hasProperty(task, 'responsible')) {
                    task.responsible = ou
                }
                if (task.metaClass.hasProperty(task, 'secondaryResponsible')) {
                    task.secondaryResponsible = ou
                }
                task.save()
            }
            report.reassigned++
        } catch (Exception e) {
            report.errors++
        } finally {
            task = null
        }
    }
}

void processEmployee(String name) {
    if (isTimeout()) return
    
    IDto emp = findEmployee(name)
    if (!emp) {
        report.notfound++
        report.errorNames.add(name)
        return
    }
    
    report.found++
    
    IDto ou = emp.parent?.deref()
    if (!ou) {
        report.errors++
        report.errorNames.add(name)
        return
    }
    
    List<IDto> resolvedTasks = getResolvedTasks(emp)
    if (resolvedTasks.size() > 0) {
        closeResolvedTasks(resolvedTasks)
    }
    resolvedTasks.clear()
    
    if (isTimeout()) return
    
    List<IDto> openTasks = getOpenTasks(emp)
    if (openTasks.size() > 0) {
        reassignOpenTasks(openTasks, ou)
    }
    openTasks.clear()
    
    if (isTimeout()) return
    
    boolean licenseChanged = false
    try {
        if (!DRY_RUN) {
            emp.licenseName = 'notLicensed'
            emp.save()
        }
        licenseChanged = true
        report.licensesChanged++
    } catch (Exception e) {
        report.licensesFailed++
        report.licenseFailedNames.add(name)
        report.errors++
    }
    
    if (!licenseChanged) return
    
    try {
        if (!DRY_RUN) {
            emp.removed = true
            emp.save()
        }
        report.archived++
    } catch (Exception e) {
        report.notArchived++
        report.notArchivedNames.add(name)
    } finally {
        emp = null
        ou = null
    }
}

try {
    List<String> employees = CSV_TEXT
        .split('\n')
        .collect { it.trim() }
        .findAll { it && !it.startsWith('#') }
    
    for (String emp : employees) {
        if (isTimeout()) {
            report.timeout = true
            break
        }
        
        try {
            processEmployee(emp)
        } catch (Exception e) {
            report.errors++
            report.errorNames.add(emp)
        }
    }
    
    employees.clear()
    
} catch (Exception e) {
    report.errors++
} finally {
    System.gc()
}

report.getSummary()
