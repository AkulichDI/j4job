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

CSV_TEXT.split('\n').each { fullName ->
    fullName = fullName.trim()
    if (!fullName || fullName.startsWith('#')) return
    
    def emp = null
    
    try {
        def q = QueryPool.query("name = \"$fullName\"")
            .setMaxResults(1)
            .setQueryTimeout(60000)
            .execute()
        if (q?.list()?.size() > 0) {
            emp = q.list()[0]
        }
    } catch (Exception e) {
    }
    
    if (!emp) {
        try {
            def q = QueryPool.query("name LIKE \"%$fullName%\"")
                .setMaxResults(1)
                .setQueryTimeout(60000)
                .execute()
            if (q?.list()?.size() > 0) {
                emp = q.list()[0]
            }
        } catch (Exception e) {
        }
    }
    
    if (!emp) {
        try {
            def parts = fullName.split('\\s+')
            if (parts.size() >= 2) {
                def lastName = parts[0]
                def firstName = parts[1]
                def q = QueryPool.query("lastName = \"$lastName\" AND firstName = \"$firstName\"")
                    .setMaxResults(1)
                    .setQueryTimeout(60000)
                    .execute()
                if (q?.list()?.size() > 0) {
                    emp = q.list()[0]
                }
            }
        } catch (Exception e) {
        }
    }
    
    def normalized = fullName.replace('ё', 'е').replace('Ё', 'Е').replace('-', ' ').toLowerCase()
    
    if (!emp) {
        try {
            def q = QueryPool.query("name = \"$normalized\"")
                .setMaxResults(1)
                .setQueryTimeout(60000)
                .execute()
            if (q?.list()?.size() > 0) {
                emp = q.list()[0]
            }
        } catch (Exception e) {
        }
    }
    
    if (!emp) {
        try {
            def q = QueryPool.query("name LIKE \"%$normalized%\"")
                .setMaxResults(1)
                .setQueryTimeout(60000)
                .execute()
            if (q?.list()?.size() > 0) {
                emp = q.list()[0]
            }
        } catch (Exception e) {
        }
    }
    
    if (!emp) {
        try {
            def parts = normalized.split('\\s+')
            if (parts.size() >= 2) {
                def lastNameNorm = parts[0]
                def firstNameNorm = parts[1]
                def q = QueryPool.query("lastName = \"$lastNameNorm\" AND firstName = \"$firstNameNorm\"")
                    .setMaxResults(1)
                    .setQueryTimeout(60000)
                    .execute()
                if (q?.list()?.size() > 0) {
                    emp = q.list()[0]
                }
            }
        } catch (Exception e) {
        }
    }
    
    if (!emp) {
        stats.notfound++
        return
    }
    
    stats.found++
    
    try {
        def license = emp.licenseName
        
        if (!license || 
            license.toString().toLowerCase().contains('notlicensed') || 
            license.toString().toLowerCase() == 'none' || 
            license.toString().toLowerCase() == 'нет' ||
            license.toString().trim() == '') {
            
            stats.withoutLicense++
            stats.withoutLicenseList.add(fullName)
        } else {
            stats.withLicense++
            stats.withLicenseList.add(fullName)
        }
    } catch (Exception e) {
    }
}

def elapsed = (System.currentTimeMillis() - stats.startTime) / 1000

"""
ПРОВЕРКА ЛИЦЕНЗИЙ СОТРУДНИКОВ
==============================
Обработано: ${stats.found + stats.notfound}
Найдено: ${stats.found}
Не найдено: ${stats.notfound}
Время: ${elapsed}сек

РЕЗУЛЬТАТЫ
==========
С лицензией: ${stats.withLicense}
Без лицензии: ${stats.withoutLicense}

С ЛИЦЕНЗИЕЙ (${stats.withLicense}):
${stats.withLicenseList.isEmpty() ? 'Нет' : stats.withLicenseList.collect { "  - $it" }.join('\n')}

БЕЗ ЛИЦЕНЗИИ (${stats.withoutLicense}):
${stats.withoutLicenseList.isEmpty() ? 'Нет' : stats.withoutLicenseList.collect { "  - $it" }.join('\n')}
"""
