import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.HttpsURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class ExcelToExecPost {

    // === ВАШИ НАСТРОЙКИ ===
    static String EXCEL_FILE = "C:/data/equipment.xlsx";

    // База из curl:
    static String BASE_EXEC_POST = "https://172.16.3.107:8080/sd/services/rest/exec-post";
    static String ACCESS_KEY     = "PASTE_YOUR_ACCESS_KEY"; // то, что стоит после accessKey=
    static String FUNC           = "modules.apiMigrationITop.migration";

    // Нагрузка
    static int  BATCH_SIZE  = 100;   // регулируйте под сервер
    static long PAUSE_MS    = 800;   // пауза между батчами
    static int  URL_LIMIT   = 6000;  // защитный лимит длины URL (можно 4000–8000; зависит от прокси/серверов)

    // SSL как curl -k
    static boolean TRUST_ALL_SSL = true;

    static final Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            if (TRUST_ALL_SSL) trustAllSsl();

            // 1) читаем Excel
            List<Map<String, String>> rows = readXlsxAsMaps(EXCEL_FILE);
            if (rows.isEmpty()) {
                System.out.println("Excel пуст или не прочитан.");
                return;
            }
            System.out.println("Строк прочитано: " + rows.size());

            // 2) батчинг с авто-уменьшением, если URL слишком длинный
            List<List<Map<String, String>>> batches = makeBatchesByUrlLimit(rows, BATCH_SIZE, URL_LIMIT);

            // 3) отправка
            for (int i = 0; i < batches.size(); i++) {
                List<Map<String, String>> batch = batches.get(i);
                String json = GSON.toJson(batch);             // это тот самый %json_text%
                String url  = buildExecPostUrl(json);         // ...&params='%json_text%'

                System.out.printf("POST batch %d/%d (size=%d) urlLen=%d%n",
                        i + 1, batches.size(), batch.size(), url.length());

                int code = postEmptyBody(url); // тело пустое — всё в query, как в curl
                System.out.println("HTTP " + code);
                if (i < batches.size() - 1) Thread.sleep(PAUSE_MS);
            }

            System.out.println("Готово.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====== Excel чтение: всё строками, первая строка — заголовки ======
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
                    String v = fmt.formatCellValue(row.getCell(c)).trim();
                    if (!v.isEmpty()) allEmpty = false;
                    m.put(names[c], v);
                }
                if (!allEmpty) out.add(m);
            }
        }
        return out;
    }

    // ====== Построение URL под ваш exec-post ======
    // Итог: https://.../exec-post?accessKey=...&func=...&params='%JSON...%'
    static String buildExecPostUrl(String jsonText) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(BASE_EXEC_POST);
        char join = BASE_EXEC_POST.contains("?") ? '&' : '?';
        sb.append(join).append("accessKey=").append(encode(ACCESS_KEY));
        sb.append("&func=").append(encode(FUNC));

        // важный момент: модулю нужен формат params='%json_text%'
        // т.е. value = "'" + json + "'"
        String paramsValue = "'" + jsonText + "'";
        sb.append("&params=").append(encode(paramsValue));
        return sb.toString();
    }

    static String encode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
    }

    // ====== Отправка POST с пустым телом (как curl -X POST без --data) ======
    static int postEmptyBody(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        // как в curl: -H "Content-Type: application/json"
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // тело пустое (zero-length), но это всё ещё POST:
        c.setDoOutput(true);
        try (OutputStream os = c.getOutputStream()) {
            // ничего не пишем намеренно
        }

        int code = c.getResponseCode();
        try (InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream()) {
            if (is != null) {
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!body.isBlank()) System.out.println("Response: " + (body.length() > 800 ? body.substring(0, 800) + "..." : body));
            }
        }
        c.disconnect();
        return code;
    }

    // ====== Автоподбор батча под ограничение длины URL ======
    static List<List<Map<String, String>>> makeBatchesByUrlLimit(
            List<Map<String, String>> rows, int startBatch, int urlLimit) throws UnsupportedEncodingException {

        List<List<Map<String, String>>> res = new ArrayList<>();
        int i = 0;
        while (i < rows.size()) {
            int size = Math.min(startBatch, rows.size() - i);
            // уменьшаем, пока URL не влезает
            while (size > 0) {
                String json = GSON.toJson(rows.subList(i, i + size));
                String url  = buildExecPostUrl(json);
                if (url.length() <= urlLimit) break;
                size = size / 2; // делим батч пополам
            }
            if (size == 0) {
                // даже одна строка не влезла — это редкость; сообщим и попробуем отправить её отдельно
                size = 1;
                String url = buildExecPostUrl(GSON.toJson(rows.subList(i, i + 1)));
                System.err.println("WARNING: URL длинее лимита (" + url.length() + " > " + urlLimit + ") даже для 1 записи.");
                System.err.println("Рассмотрите уменьшение набора полей/короткие значения или другой способ передачи (тело POST).");
            }
            res.add(rows.subList(i, i + size));
            i += size;
        }
        return res;
    }

    // ====== Эквивалент curl -k (использовать ТОЛЬКО в доверенной сети) ======
    static void trustAllSsl() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{ new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] xcs, String a) {}
            public void checkServerTrusted(X509Certificate[] xcs, String a) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
    }
}