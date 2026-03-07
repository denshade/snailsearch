package search.store;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-backed index store (data/{hostname}.csv). Columns: url,etag,text.
 * Text may contain commas; fields are quoted when needed.
 */
public class CsvIndexDAO implements IndexDAO {

    private static final String HEADER = "url,etag,text";
    private final Path file;
    private final List<Row> rows = new ArrayList<>();

    public CsvIndexDAO(String dataDir, String hostname) {
        this.file = Path.of(dataDir, hostname + ".csv");
    }

    @Override
    public void init() {
        rows.clear();
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                try (var reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    if (line != null && HEADER.equals(line.trim())) {
                        while ((line = reader.readLine()) != null) {
                            Row r = parseCsvLine(line);
                            if (r != null) {
                                rows.add(r);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPage(String url, String etag, String text) {
        rows.removeIf(r -> r.url.equals(url));
        rows.add(new Row(url, etag != null ? etag : "", text != null ? text : ""));
        writeAll();
    }

    @Override
    public boolean isEtagInIndex(String url, String etag) {
        if (etag == null || etag.isEmpty()) {
            return false;
        }
        for (Row r : rows) {
            if (r.url.equals(url) && r.etag.equals(etag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getUrls() {
        return rows.stream().map(r -> r.url).collect(Collectors.toList());
    }

    private void writeAll() {
        try {
            try (var out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8))) {
                out.write(HEADER);
                out.newLine();
                for (Row r : rows) {
                    out.write(quoteCsvField(r.url) + "," + quoteCsvField(r.etag) + "," + quoteCsvField(r.text));
                    out.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String quoteCsvField(String s) {
        if (s == null) return "";
        if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && !s.contains("\n")) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static Row parseCsvLine(String line) {
        if (line == null || line.isEmpty()) return null;
        List<String> fields = parseCsvFields(line);
        if (fields.size() < 3) return null;
        return new Row(fields.get(0), fields.get(1), fields.get(2));
    }

    private static List<String> parseCsvFields(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result;
    }

    private static final class Row {
        final String url, etag, text;

        Row(String url, String etag, String text) {
            this.url = url;
            this.etag = etag;
            this.text = text;
        }
    }
}
