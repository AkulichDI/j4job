String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович'''

long SCRIPT_START_TIME = System.currentTimeMillis()
long TIMEOUT_MS = 4 * 60 * 1000

class Report {
    int found = 0
    int notfound = 0
    int withLicense = 0
    int withoutLicense = 0
    int errors = 0
    boolean timeout = false
    
    List<String> employeesWithLicense = []
    List<String> employeesWithoutLicense = []
    List<String> errorNames = []
    
    long executionTimeMs = 0
    
    String getSummary() {
        long seconds = executionTimeMs / 1000
        String timeInfo = "${seconds}сек"
        
        StringBuilder result = new StringBuilder()
        result.append("Обработано сотрудников: ${found + notfound}\n")
        result.append("Найдено: $found\n")
        result.append("Не найдено: $notfound\n")
        result.append("Ошибок: $errors\n")
        result.append("Время выполнения: $timeInfo\n")
        result.append("\n=== РЕЗУЛЬТАТЫ ПО ЛИЦЕНЗИЯМ ===\n")
        result.append("С лицензией: $withLicense\n")
        result.append("Без лицензии: $withoutLicense\n")
        
        if (employeesWithLicense.size() > 0) {
            result.append("\n=== С ЛИЦЕНЗИЕЙ (${employeesWithLicense.size()}) ===\n")
            employeesWithLicense.each { name ->
                result.append("  - $name\n")
            }
        }
        
        if (employeesWithoutLicense.size() > 0) {
            result.append("\n=== БЕЗ ЛИЦЕНЗИИ (${employeesWithoutLicense.size()}) ===\n")
            employeesWithoutLicense.each { name ->
                result.append("  - $name\n")
            }
        }
        
        if (errorNames.size() > 0) {
            result.append("\n=== НЕ НАЙДЕНЫ (${errorNames.size()}) ===\n")
            errorNames.each { name ->
                result.append("  - $name\n")
            }
        }
        
        if (timeout) {
            result.append("\n=== ТАЙМАУТ ===\n")
            result.append("Скрипт прерван по времени (4 минуты)\n")
        }
        
        return result.toString()
    }
}

Report report = new Report()

boolean checkTimeout() {
    long elapsed = System.currentTimeMillis() - SCRIPT_START_TIME
    return elapsed >= TIMEOUT_MS
}

def findEmployee(String name) {
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

boolean hasLicense(def emp) {
    if (!emp) return false
    
    try {
        def license = emp.licenseName
        if (license == null) {
            return false
        }
        
        String licenseStr = license.toString().trim().toLowerCase()
        
        if (licenseStr.isEmpty() || 
            licenseStr == 'notlicensed' || 
            licenseStr == 'not licensed' || 
            licenseStr == 'none' ||
            licenseStr == 'нет' ||
            licenseStr == 'отсутствует') {
            return false
        }
        
        return true
    } catch (Exception e) {
        return false
    }
}

void processEmployee(String name) {
    if (checkTimeout()) {
        report.timeout = true
        return
    }
    
    def emp = findEmployee(name)
    if (!emp) {
        report.notfound++
        report.errorNames.add(name)
        return
    }
    
    report.found++
    
    try {
        if (hasLicense(emp)) {
            report.withLicense++
            report.employeesWithLicense.add(name)
        } else {
            report.withoutLicense++
            report.employeesWithoutLicense.add(name)
        }
    } catch (Exception e) {
        report.errors++
        report.errorNames.add(name)
    }
    
    emp = null
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
