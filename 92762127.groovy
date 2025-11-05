String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович'''

def stats = [
    found: 0,
    notfound: 0,
    withLicense: 0,
    withoutLicense: 0,
    withLicenseList: [],
    withoutLicenseList: [],
    startTime: System.currentTimeMillis()
]

def findEmployee(name) {
    if (!name?.trim()) return null
    
    def original = name.trim()
    def normalized = original.replace('ё', 'е').replace('Ё', 'Е').replace('-', ' ').toLowerCase()
    def parts = original.split('\\s+')
    def lastName = parts.size() > 0 ? parts[0] : ''
    def firstName = parts.size() > 1 ? parts[1] : ''
    
    try {
        def q1 = QueryPool.query("name = \"$original\"").setMaxResults(1).execute()
        if (q1?.list()?.size() > 0) return q1.list()[0]
        
        def q2 = QueryPool.query("name LIKE \"%$original%\"").setMaxResults(1).execute()
        if (q2?.list()?.size() > 0) return q2.list()[0]
        
        if (lastName && firstName) {
            def q3 = QueryPool.query("lastName = \"$lastName\" AND firstName = \"$firstName\"").setMaxResults(1).execute()
            if (q3?.list()?.size() > 0) return q3.list()[0]
        }
        
        def q4 = QueryPool.query("name = \"$normalized\"").setMaxResults(1).execute()
        if (q4?.list()?.size() > 0) return q4.list()[0]
        
        def q5 = QueryPool.query("name LIKE \"%$normalized%\"").setMaxResults(1).execute()
        if (q5?.list()?.size() > 0) return q5.list()[0]
        
        if (lastName && firstName) {
            def lastNameNorm = lastName.replace('ё', 'е').replace('Ё', 'Е')
            def firstNameNorm = firstName.replace('ё', 'е').replace('Ё', 'Е')
            def q6 = QueryPool.query("lastName = \"$lastNameNorm\" AND firstName = \"$firstNameNorm\"").setMaxResults(1).execute()
            if (q6?.list()?.size() > 0) return q6.list()[0]
        }
    } catch (Exception e) {
    }
    
    return null
}

CSV_TEXT.split('\n').each { empName ->
    empName = empName.trim()
    if (!empName || empName.startsWith('#')) return
    
    def emp = findEmployee(empName)
    
    if (!emp) {
        stats.notfound++
        return
    }
    
    stats.found++
    
    try {
        def license = emp.licenseName
        def licenseStr = license?.toString()?.toLowerCase()?.trim()
        
        if (!license || 
            licenseStr == 'notlicensed' || 
            licenseStr == 'not licensed' || 
            licenseStr == 'none' ||
            licenseStr == 'нет' ||
            licenseStr == 'отсутствует' ||
            licenseStr == '') {
            
            stats.withoutLicense++
            stats.withoutLicenseList.add(empName)
        } else {
            stats.withLicense++
            stats.withLicenseList.add(empName)
        }
    } catch (Exception e) {
    }
}

def elapsed = (System.currentTimeMillis() - stats.startTime) / 1000

"""
Обработано сотрудников: ${stats.found + stats.notfound}
Найдено: ${stats.found}
Не найдено: ${stats.notfound}
Время выполнения: ${elapsed}сек

РЕЗУЛЬТАТЫ ПО ЛИЦЕНЗИЯМ:
С лицензией: ${stats.withLicense}
Без лицензии: ${stats.withoutLicense}

С ЛИЦЕНЗИЕЙ (${stats.withLicense}):
${stats.withLicenseList.isEmpty() ? 'Нет' : stats.withLicenseList.collect { "  - $it" }.join('\n')}

БЕЗ ЛИЦЕНЗИИ (${stats.withoutLicense}):
${stats.withoutLicenseList.isEmpty() ? 'Нет' : stats.withoutLicenseList.collect { "  - $it" }.join('\n')}
"""
