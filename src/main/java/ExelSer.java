import org.apache.poi.ss.usermodel.*;        // Apache POI classes for workbook, sheet, row, cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;                 // Gson library for JSON serialization

public class NaumenExcelUploader {

    // Настройки / конфигурационные параметры
    private static String EXCEL_FILE_PATH = "C:/data/equipment.xlsx";   // Путь к Excel-файлу
    private static String SERVER_URL    = "http://your-naumen-server/sd/"; // Адрес Naumen SD (заканчивается на /sd/)
    private static String OBJECT_CLASS  = "equipment";    // Код класса создаваемого объекта (пример)
    private static String REST_METHOD   = "create";       // REST-метод для создания объекта
    private static String ACCESS_KEY    = "YOUR_ACCESS_TOKEN_HERE"; // Токен доступа (Access Key) для API
    private static int    BATCH_SIZE    = 100;    // Размер группы отправки (сколько записей отправлять за раз)
    private static int    PAUSE_MS      = 1000;   // Пауза (мс) между группами запросов

    public static void main(String[] args) {
        // 1. Чтение данных из Excel-файла
        List<Map<String, Object>> records;
        try {
            records = readExcelFile(EXCEL_FILE_PATH);
        } catch (IOException | InvalidFormatException e) {
            System.err.println("Ошибка чтения Excel-файла: " + e.getMessage());
            return;
        }

        // 2. Отправка данных на сервер Naumen в виде JSON через REST API
        sendRecordsToNaumen(records);
    }

    /**
     * Считывает Excel-файл и возвращает список записей.
     * Каждая запись представлена как Map "название атрибута -> значение".
     */
    private static List<Map<String, Object>> readExcelFile(String filePath) throws IOException, InvalidFormatException {
        List<Map<String, Object>> recordList = new ArrayList<>();

        // Открываем Excel-файл с помощью Apache POI
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);  // Берём первую страницу (лист) в книге

            if (sheet == null) {
                return recordList;  // пустой список, если лист не найден
            }

            // Получаем заголовки столбцов из первой строки (для имен полей)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return recordList;  // если файл пустой или без заголовка
            }
            int colCount = headerRow.getLastCellNum();
            String[] headers = new String[colCount];
            for (int c = 0; c < colCount; c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    headers[c] = cell.toString().trim();  // берём текстовое значение заголовка (обрезая пробелы)
                } else {
                    headers[c] = "Column" + (c + 1);      // имя по умолчанию, если ячейка пуста
                }
            }

            // Проходим по всем строкам ниже заголовка и считываем значения
            int lastRowNum = sheet.getLastRowNum();
            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue; // пропускаем пустые строки
                }
                Map<String, Object> record = new LinkedHashMap<>();  // одна запись (оборудование)
                // Считываем по каждому столбцу в этой строке
                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.getCell(c);
                    Object value;
                    if (cell == null) {
                        value = "";  // если ячейка пуста, сохраним пустую строку
                    } else {
                        // Определяем тип ячейки и получаем значение соответствующего типа
                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue().trim();
                                break;
                            case NUMERIC:
                                // Если число представляет дату, преобразуем в дату; иначе – в числовой тип
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    value = cell.getDateCellValue();  // Date объект (можно форматировать по необходимости)
                                } else {
                                    // По умолчанию считываем как Double. Если это целое число, можно привести к Long.
                                    double numericVal = cell.getNumericCellValue();
                                    // Проверка на целочисленность
                                    if (numericVal == (long) numericVal) {
                                        value = (long) numericVal;
                                    } else {
                                        value = numericVal;
                                    }
                                }
                                break;
                            case BOOLEAN:
                                value = cell.getBooleanCellValue();
                                break;
                            case FORMULA:
                                // Получаем результат формулы (как текст или число)
                                CellType formulaResultType = cell.getCachedFormulaResultType();
                                if (formulaResultType == CellType.NUMERIC) {
                                    value = cell.getNumericCellValue();
                                } else if (formulaResultType == CellType.STRING) {
                                    value = cell.getStringCellValue().trim();
                                } else {
                                    value = cell.toString();
                                }
                                break;
                            case BLANK:
                                value = "";
                                break;
                            default:
                                value = cell.toString().trim();
                        }
                    }
                    // Сохраняем значение в Map с ключом из заголовка
                    String fieldName = headers[c];
                    record.put(fieldName, value);
                }
                recordList.add(record);
            }
        }
        return recordList;
    }

    /**
     * Отправляет список записей (список Map-ов) в Naumen Service Desk через REST API.
     * Отправка выполняется пакетами (batch) с паузами между ними, чтобы снизить нагрузку на сервер.
     */
    private static void sendRecordsToNaumen(List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            System.out.println("Нет данных для отправки.");
            return;
        }

        Gson gson = new Gson();  // объект для преобразования в JSON
        int total = records.size();
        int sentCount = 0;
        for (Map<String, Object> record : records) {
            sentCount++;
            // Формируем JSON строку из записи
            String jsonData = gson.toJson(record);
            try {
                // Формируем URL запроса с указанием метода, класса объекта и токена доступа
                String requestUrl = SERVER_URL + "services/rest/" + REST_METHOD + "/" + OBJECT_CLASS 
                                     + "?accessKey=" + ACCESS_KEY;
                URL url = new URL(requestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Отправляем JSON-данные в тело POST-запроса
                byte[] out = jsonData.getBytes(StandardCharsets.UTF_8);
                conn.getOutputStream().write(out);
                // Получаем ответ (код HTTP)
                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    System.out.println("Запись " + sentCount + " из " + total + " успешно отправлена (HTTP " + responseCode + ").");
                } else {
                    System.err.println("Ошибка при отправке записи " + sentCount + ". Код ответа: " + responseCode);
                }
                conn.disconnect();  // закрываем соединение

            } catch (IOException e) {
                System.err.println("Исключение при отправке записи " + sentCount + ": " + e.getMessage());
            }

            // Если достигли размера батча, делаем паузу перед отправкой следующей группы
            if (sentCount % BATCH_SIZE == 0) {
                try {
                    System.out.println("Пауза " + PAUSE_MS + " мс после отправки " + sentCount + " записей...");
                    Thread.sleep(PAUSE_MS);
                } catch (InterruptedException ie) {
                    // Восстанавливаем прерванный статус потока и выходим, если поток прерван
                    Thread.currentThread().interrupt();
                    System.err.println("Выполнение прервано во время паузы.");
                    break;
                }
            }
        }
        System.out.println("Отправка завершена. Всего обработано записей: " + sentCount);
    }
}