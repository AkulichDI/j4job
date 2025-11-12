import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class ExcelBatchPosterSimple {

    // ==== настройка ====
    static String EXCEL_FILE   = "C:/data/equipment.xlsx";
    static String ENDPOINT_URL = "https://host/path/to/module?param=..."; // <-- ваш URL из curl
    static int    BATCH_SIZE   = 100;      // регулируйте под сервер
    static long   PAUSE_MS     = 800;      // пауза между батчами
    static boolean TRUST_ALL_SSL = true;   // аналог curl -k (НЕБЕЗОПАСНО, только если нужно)

    // Если нужен токен/другие заголовки — добавьте сюда:
    static Map<String,String> EXTRA_HEADERS = Map.of(
        "Content-Type", "application/json"
        // ,"Authorization", "Bearer YOUR_TOKEN"
    );

    static final Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            if (TRUST_ALL_SSL) trustAllSsl();   // эквивалент curl -k

            List<Map<String, String>> rows = readXlsxAsMaps(EXCEL_FILE);
            List<List<Map<String, String>>> batches = slice(rows, BATCH_SIZE);

            for (int i = 0; i < batches.size(); i++) {
                String payload = GSON.toJson(batches.get(i));   // массив записей
                int code = postJson(ENDPOINT_URL, payload, EXTRA_HEADERS);
                System.out.printf("Batch %d/%d -> HTTP %d%n", i+1, batches.size(), code);
                if (i < batches.size() - 1) Thread.sleep(PAUSE_MS);
            }
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Excel (.xlsx): первая строка — заголовки =====
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
                if (!allEmpty) out.add(m);
            }
        }
        return out;
    }

    // ===== POST JSON =====
    static int postJson(String url, String json, Map<String,String> headers) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);

        // заголовки из curl
        for (var e : headers.entrySet()) {
            c.setRequestProperty(e.getKey(), e.getValue());
        }

        try (OutputStream os = c.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        int code = c.getResponseCode();

        try (InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream()) {
            if (is != null) {
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!body.isBlank()) System.out.println("Response: " + body.substring(0, Math.min(600, body.length())));
            }
        }
        c.disconnect();
        return code;
    }

    // ===== batching =====
    static <T> List<List<T>> slice(List<T> src, int size) {
        List<List<T>> res = new ArrayList<>();
        for (int i = 0; i < src.size(); i += size) res.add(src.subList(i, Math.min(i + size, src.size())));
        return res;
    }

    // ===== curl -k (доверять всем сертификатам) — используйте только в доверенной сети! =====
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