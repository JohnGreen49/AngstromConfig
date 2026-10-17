package angstrom.simulator.csv;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** One data row destined for a CSV file. Implementations are the per-station records. */
public interface CsvRow {
    DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    CsvKind kind();

    String serialNumber();

    /** Values in header order. Must be the same length as {@code kind().headers()}. */
    List<String> values();

    static String date(LocalDate d) {
        return d.format(DATE);
    }

    static String time(LocalTime t) {
        return t.format(TIME);
    }
}
