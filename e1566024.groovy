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
    List<String> withLicenseList = []
    List<String> withoutLicenseList = []
    long executionTimeMs = 0
    
    String getSummary() {
        long seconds = executionTimeMs / 1000
        return """
Найдено: $found
Не найдено: $notfound
С лицензией: $withLicense
Без лицензии: $withoutLicense
Время: ${seconds}сек

С ЛИЦЕНЗИЕЙ:
${withLicenseList.isEmpty() ? 'Нет' : withLicenseList.join('\n')}

БЕЗ ЛИЦЕНЗИИ:
${withoutLicenseList.isEmpty() ? 'Нет' : withoutLicenseList.join('\n')}
"""
    }
}

Report report = new Report()

boolean checkTimeout() {
    return (System.currentTimeMillis() - SCRIPT_START_TIME) >= TIMEOUT_MS
}

def findEmployee(String name) {
    if (!name?.trim() || checkTimeout()) return null
    
    String original = name.trim()
    String normalized = original.replace('ё', 'е').replace('Ё', 'Е').replace('-', ' ').toLowerCase()
    List<String> parts = original.split('\\s+')
    String lastName = parts.size() > 0 ? parts[0] : ''
    String firstName = parts.size() > 1 ? parts[1] : ''
    
    try {
        def q1 = QueryPool.query("name = '${original}'").setMaxResults(1).execute()
        if (q1?.list()?.size() > 0) return q1.list()[0]
        
        def q2 = QueryPool.query("name LIKE '%${original}%'").setMaxResults(1).execute()
        if (q2?.list()?.size() > 0) return q2.list()[0]
        
        if (lastName && firstName) {
            def q3 = QueryPool.query("lastName = '${lastName}' AND firstName = '${firstName}'").setMaxResults(1).execute()
            if (q3?.list()?.size() > 0) return q3.list()[0]
        }
        
        def q4 = QueryPool.query("name = '${normalized}'").setMaxResults(1).execute()
        if (q4?.list()?.size() > 0) return q4.list()[0]
        
        def q5 = QueryPool.query("name LIKE '%${normalized}%'").setMaxResults(1).execute()
        if (q5?.list()?.size() > 0) return q5.list()[0]
        
        if (lastName && firstName) {
            String lastNameNorm = lastName.replace('ё', 'е').replace('Ё', 'Е')
            String firstNameNorm = firstName.replace('ё', 'е').replace('Ё', 'Е')
            def q6 = QueryPool.query("lastName = '${lastNameNorm}' AND firstName = '${firstNameNorm}'").setMaxResults(1).execute()
            if (q6?.list()?.size() > 0) return q6.list()[0]
        }
    } catch (Exception e) {
    }
    
    return null
}

CSV_TEXT.split('\n').each { empName ->
    if (checkTimeout()) return
    
    empName = empName.trim()
    if (!empName || empName.startsWith('#')) return
    
    def emp = findEmployee(empName)
    
    if (!emp) {
        report.notfound++
        return
    }
    
    report.found++
    
    try {
        def license = emp.licenseName
        String licenseStr = license?.toString()?.toLowerCase()?.trim()
        
        if (!license || 
            licenseStr == 'notlicensed' || 
            licenseStr == 'not licensed' || 
            licenseStr == 'none' ||
            licenseStr == 'нет' ||
            licenseStr == 'отсутствует') {
            
            report.withoutLicense++
            report.withoutLicenseList.add(empName)
        } else {
            report.withLicense++
            report.withLicenseList.add(empName)
        }
    } catch (Exception e) {
    }
}

report.executionTimeMs = System.currentTimeMillis() - SCRIPT_START_TIME
report.getSummary()
