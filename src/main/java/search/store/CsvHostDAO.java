package search.store;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

/**
 * CSV-backed host store (data/hosts.csv). One column: url.
 */
public class CsvHostDAO implements HostDAO {

    private static final String HEADER = "url";
    private final Path file;
    private final Set<String> known = new HashSet<>();

    public CsvHostDAO(String dataDir) {
        this.file = Path.of(dataDir, "hosts.csv");
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                try (var reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    if (line != null && HEADER.equals(line.trim())) {
                        while ((line = reader.readLine()) != null) {
                            String url = unquoteCsvField(line.trim());
                            if (!url.isEmpty()) {
                                known.add(url);
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
    public void addHost(String url) {
        if (known.add(url)) {
            try {
                boolean writeHeader = !Files.exists(file);
                try (var out = new BufferedWriter(new OutputStreamWriter(
                        Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                        StandardCharsets.UTF_8))) {
                    if (writeHeader) {
                        out.write(HEADER);
                        out.newLine();
                    }
                    out.write(quoteCsvField(url));
                    out.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String quoteCsvField(String s) {
        if (s == null) return "";
        if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && !s.contains("\n")) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String unquoteCsvField(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}
