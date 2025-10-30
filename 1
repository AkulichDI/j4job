// 1) Вставьте CSV сюда (первая колонка — полное ФИО, далее любые колонки)
def CSV_TEXT = """
Иванов Иван Иванович,ivanov@example.com,АДиТ,Сектор 1
Петров Петр Петрович,petrov@example.com,АДиТ,Сектор 2
Сидорова Анна Сергеевна,anna.sidorova@example.com,Разработка,Группа А
""".trim()

// 2) Получаем массив строк (каждый элемент — одна строка CSV)
def lines = CSV_TEXT.readLines()

// 3) Парсер CSV строки с учётом кавычек ("," внутри кавычек не режем)
List<String> parseCsvLine(String line, char delim = ',') {
  def out = []
  def sb = new StringBuilder()
  boolean inQuotes = false
  for (int i = 0; i < line.size(); i++) {
    char ch = line.charAt(i)
    if (ch == '"') {
      // удвоенные кавычки внутри поля -> одна кавычка
      if (inQuotes && i + 1 < line.size() && line.charAt(i + 1) == '"') {
        sb.append('"'); i++    // пропускаем вторую кавычку
      } else {
        inQuotes = !inQuotes
      }
      continue
    }
    if (ch == delim && !inQuotes) { out << sb.toString().trim(); sb.setLength(0) }
    else { sb.append(ch) }
  }
  out << sb.toString().trim()
  return out
}

// 4) Превращаем строки в объекты и достаём ФИО (первые 3 слова из первой колонки)
def rows = lines
  .findAll { it?.trim() && !it.trim().startsWith('#') && !it.trim().startsWith('//') } // фильтр «мусора»
  .collect { line ->
    def cols = parseCsvLine(line, ',')        // если у вас ; — замените на ';'
    def fioField = (cols[0] ?: "").trim()
    def fioParts = fioField.tokenize(/\s+/)   // делим по пробелам
    def fio3 = fioParts.take(3).join(' ')     // первые 3 слова как ФИО
    [
      raw : line,      // исходная строка
      cols: cols,      // массив колонок
      fio : fio3,      // «Фамилия Имя Отчество»
      fioParts: fioParts
    ]
  }

// 5) Пример: распечатаем массив ФИО
println rows*.fio
// Пример: доступ к колонкам остальных атрибутов
rows.each { r ->
  def email = r.cols.size() > 1 ? r.cols[1] : null
  println "ФИО=${r.fio}  email=${email}"
}