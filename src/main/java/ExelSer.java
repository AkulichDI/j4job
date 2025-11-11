package your.pkg;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Excel (.xlsx) -> JSON[] батчами -> POST на ваш endpoint.
 * Регулируемые BATCH_SIZE и PAUSE_MS, никакой бизнес-логики.
 */
public class ExcelBatchPoster {

    // ====== НАСТРОЙКИ (меняйте под себя) ======
    static String EXCEL_FILE   = "C:/data/equipment.xlsx";
    static String ENDPOINT_URL = "https://your-naumen/sd/services/rest/ingest";

    // Токен: в заголовке (Authorization: Bearer ...) или в query (?accessKey=...)
    static boolean TOKEN_IN_HEADER   = true;
    static String  TOKEN             = "PASTE_YOUR_TOKEN";
    static String  TOKEN_QUERY_PARAM = "accessKey";

    // Регулировка нагрузки
    static int  BATCH_SIZE = 100;  // сколько строк в одном POST
    static long PAUSE_MS   = 800;  // пауза между батчами (мс)

    // (опционально) лёгкий ретрай на 429/5xx
    static int  MAX_RETRIES     = 2;
    static long RETRY_BACKOFFMS = 600;

    static final Gson GSON = new Gson();

    public static void main(String[] args) {
        // Позволяем задавать часть настроек через аргументы/ENV (необязательно)
        // args: <excelPath> <endpointUrl> <batchSize> <pauseMs>
        if (args.length > 0) EXCEL_FILE = args[0];
        if (args.length > 1) ENDPOINT_URL = args[1];
        if (args.length > 2) BATCH_SIZE = Integer.parseInt(args[2]);
        if (args.length > 3) PAUSE_MS = Long.parseLong(args[3]);

        // ENV-переменные тоже можно использовать (если удобно)
        String envToken = System.getenv("NAUMEN_TOKEN");
        if (envToken != null && !envToken.isBlank()) TOKEN = envToken;

        try {
            // 1) читаем Excel
            List<Map<String, String>> rows = readXlsxAsMaps(EXCEL_FILE);
            System.out.println("Строк прочитано: " + rows.size());
            if (rows.isEmpty()) return;

            // 2) режем на батчи
            List<List<Map<String, String>>> batches = slice(rows, BATCH_SIZE);
            int total = batches.size();

            // 3) шлём по очереди
            for (int i = 0; i < total; i++) {
                List<Map<String, String>> batch = batches.get(i);
                String payload = GSON.toJson(batch);
                String url = appendTokenIfNeeded(ENDPOINT_URL);

                System.out.printf("POST batch %d/%d (size=%d)%n", i + 1, total, batch.size());
                boolean ok = postWithRetry(url, payload);
                if (!ok) {
                    System.err.println("Батч не доставлен после ретраев. Останавливаюсь.");
                    break; // или continue — по вашей политике
                }

                if (i < total - 1) {
                    try { Thread.sleep(PAUSE_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }

            System.out.println("Готово.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // ============ Excel ============

    // Первая строка — заголовки; дальше — данные; всё -> String
    static List<Map<String, String>> readXlsxAsMaps(String path) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        try (InputStream in = new FileInputStream(path);
             Workbook wb = new XSSFWorkbook(in)) {

            Sheet sh = wb.getSheetAt(0);
            if (sh == null) return out;

            Row header = sh.getRow(0);
            if (header == null) return out;

            int cols = header.getLastCellNum();
            String[] names = new String[cols];
            DataFormatter fmt = new DataFormatter();

            for (int c = 0; c < cols; c++) {
                names[c] = fmt.formatCellValue(header.getCell(c)).trim();
                if (names[c].isEmpty()) names[c] = "Column" + (c + 1);
            }

            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;

                Map<String, String> m = new LinkedHashMap<>();
                boolean allEmpty = true;

                for (int c = 0; c < cols; c++) {
                    String val = fmt.formatCellValue(row.getCell(c)).trim();
                    if (!val.isEmpty()) allEmpty = false;
                    m.put(names[c], val);
                }
                if (!allEmpty) out.add(m); // пропускаем полностью пустые строки
            }
        }
        return out;
    }

    // ============ batching utils ============
    static <T> List<List<T>> slice(List<T> src, int size) {
        List<List<T>> res = new ArrayList<>();
        for (int i = 0; i < src.size(); i += size) {
            res.add(src.subList(i, Math.min(i + size, src.size())));
        }
        return res;
    }

    // ============ HTTP ============

    static boolean postWithRetry(String url, String json) {
        int attempts = 0;
        long backoff = RETRY_BACKOFFMS;

        while (true) {
            attempts++;
            try {
                int code = postJson(url, json);
                if (code >= 200 && code < 300) return true;

                // Ретраим только 429/5xx
                if (code == 429 || (code >= 500 && code < 600)) {
                    if (attempts > MAX_RETRIES) return false;
                    System.err.println("HTTP " + code + ", retry " + attempts + " after " + backoff + " ms");
                    try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return false; }
                    backoff *= 2;
                    continue;
                }
                // Остальное — не ретраим
                System.err.println("HTTP " + code + ", без ретрая.");
                return false;

            } catch (IOException e) {
                if (attempts > MAX_RETRIES) {
                    System.err.println("IO error: " + e.getMessage());
                    return false;
                }
                System.err.println("IO error, retry " + attempts + " after " + backoff + " ms: " + e.getMessage());
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return false; }
                backoff *= 2;
            }
        }
    }

    static int postJson(String url, String json) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        if (TOKEN_IN_HEADER) {
            c.setRequestProperty("Authorization", "Bearer " + TOKEN);
        }

        try (OutputStream os = c.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = c.getResponseCode();
        try (InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream()) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                String body = new String(bytes, StandardCharsets.UTF_8);
                if (!body.isBlank()) {
                    System.out.println("Ответ: " + (body.length() > 500 ? body.substring(0, 500) + "..." : body));
                }
            }
        }
        c.disconnect();
        return code;
    }

    static String appendTokenIfNeeded(String url) {
        if (TOKEN_IN_HEADER) return url;
        String delim = url.contains("?") ? "&" : "?";
        return url + delim + TOKEN_QUERY_PARAM + "=" + encode(TOKEN);
    }

    static String encode(String s) {
        try { return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
}