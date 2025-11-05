String CSV_TEXT = '''Иванов Иван Иванович
Петров Петр Петрович
Сидоров Сидор Сидорович'''

def startTime = System.currentTimeMillis()

def stats = [
    found: 0,
    notfound: 0,
    withLicense: 0,
    withoutLicense: 0,
    withLicenseList: [],
    withoutLicenseList: []
]

CSV_TEXT.split('\n').each { fullName ->
    fullName = fullName.trim()
    if (!fullName || fullName.startsWith('#')) return
    
    def emp = null
    
    try {
        def q = QueryPool.query("fullName = \"$fullName\"").setMaxResults(1).execute()
        if (q?.list()?.size() > 0) {
            emp = q.list()[0]
        }
    } catch (Exception e) {
    }
    
    if (!emp) {
        try {
            def q = QueryPool.query("fullName LIKE \"%$fullName%\"").setMaxResults(1).execute()
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
                def q = QueryPool.query("lastName = \"${parts[0]}\" AND firstName = \"${parts[1]}\"").setMaxResults(1).execute()
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
        
        if (!license || license.toString().toLowerCase().contains('notlicensed') || license.toString().toLowerCase() == 'none' || license.toString().trim() == '') {
            stats.withoutLicense++
            stats.withoutLicenseList.add(fullName)
        } else {
            stats.withLicense++
            stats.withLicenseList.add(fullName)
        }
    } catch (Exception e) {
    }
}

def elapsed = (System.currentTimeMillis() - startTime) / 1000

"""
Обработано: ${stats.found + stats.notfound}
Найдено: ${stats.found}
Не найдено: ${stats.notfound}
Время: ${elapsed}сек

РЕЗУЛЬТАТ ПО ЛИЦЕНЗИЯМ:
С лицензией: ${stats.withLicense}
Без лицензии: ${stats.withoutLicense}

С ЛИЦЕНЗИЕЙ (${stats.withLicense}):
${stats.withLicenseList.isEmpty() ? 'Нет' : stats.withLicenseList.collect { "  $it" }.join('\n')}

БЕЗ ЛИЦЕНЗИИ (${stats.withoutLicense}):
${stats.withoutLicenseList.isEmpty() ? 'Нет' : stats.withoutLicenseList.collect { "  $it" }.join('\n')}
"""
