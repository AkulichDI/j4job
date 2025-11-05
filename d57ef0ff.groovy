def CSV_TEXT = $/
Иванов Иван Иванович,
Петров "Пётр" Петрович,
O'Connor Connor
/$

// Разбиваем строки по запятым и удаляем лишние пробелы
def names = CSV_TEXT.split(',').collect{ it.trim() }.findAll{ it }

// Устанавливаем возможный таймаут (настройки Naumen SMP)
def timeoutMs = 60000
// (в зависимости от API, настройка таймаута может отличаться)

// Перебираем имена и выполняем поиск
for (String fullName : names) {
    if (!fullName) continue
    try {
        // Поиск сотрудника по полному ФИО
        def employee = utils.get('employee', ['fullName': fullName])
        if (!employee) {
            logger.warn("Сотрудник не найден: $fullName")
            continue
        }
        // Определение лицензии (пример)
        def license = utils.get('license', ['employee': employee])
        if (!license) {
            logger.info("Лицензия для $fullName не найдена")
        } else {
            logger.info("Лицензия для $fullName: ${license.code}")
        }
    } catch (Exception e) {
        logger.error("Ошибка при обработке $fullName: ${e.getMessage()}")
    }
}