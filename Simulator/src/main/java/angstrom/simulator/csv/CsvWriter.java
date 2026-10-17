package angstrom.simulator.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes one file per row under {@code root/<Kind>/<Kind>_<serial>_<yyyyMMdd_HHmmss>.csv}.
 *
 * The file is written to a .tmp name and then renamed, so a PlantWatch ReadFromFile device
 * watching {@code *.csv} never sees a half-written file.
 */
public final class CsvWriter {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String EOL = "\r\n";

    private final Path root;

    public CsvWriter(Path root) {
        this.root = root;
    }

    public Path root() {
        return root;
    }

    public Path folderFor(CsvKind kind) {
        return root.resolve(kind.name());
    }

    public Path write(CsvRow row, LocalDateTime when) throws IOException {
        List<String> headers = row.kind().headers();
        List<String> values = row.values();
        if (values.size() != headers.size()) {
            throw new IllegalStateException(row.kind() + " row has " + values.size() + " values for " + headers.size() + " headers");
        }

        Path dir = folderFor(row.kind());
        Files.createDirectories(dir);
        String name = row.kind().name() + "_" + row.serialNumber() + "_" + when.format(STAMP) + ".csv";
        Path target = dir.resolve(name);
        Path tmp = dir.resolve(name + ".tmp");

        String content = line(headers) + line(values);
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    static String line(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quote(cells.get(i)));
        }
        return sb.append(EOL).toString();
    }

    private static String quote(String cell) {
        if (cell == null) return "";
        boolean needs = cell.indexOf(',') >= 0 || cell.indexOf('"') >= 0 || cell.indexOf('\n') >= 0 || cell.indexOf('\r') >= 0;
        return needs ? "\"" + cell.replace("\"", "\"\"") + "\"" : cell;
    }
}
