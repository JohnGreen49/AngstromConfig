package angstrom.simulator.csv;

import angstrom.simulator.model.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWriterTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 17);
    private static final LocalTime CLOCK = LocalTime.of(9, 5, 7);

    @Test
    void writesHeaderAndRowAtomically(@TempDir Path dir) throws Exception {
        CsvWriter writer = new CsvWriter(dir);
        AssemblyRecord row = new AssemblyRecord("PS261017-00001", "WIP-20261017-001", "MarryUp", DAY, CLOCK, 45.5, Result.PASS);

        Path written = writer.write(row, LocalDateTime.of(DAY, CLOCK));

        assertEquals(dir.resolve("Assembly").resolve("Assembly_PS261017-00001_20261017_090507.csv"), written);
        String content = Files.readString(written, StandardCharsets.UTF_8);
        assertEquals("SerialNumber,WIPBatchNumber,AssemblyStation,AssemblyDate,AssemblyTime,TorqueValue,Result\r\n"
                + "PS261017-00001,WIP-20261017-001,MarryUp,2026-10-17,09:05:07,45.50,PASS\r\n", content);

        try (Stream<Path> files = Files.list(dir.resolve("Assembly"))) {
            assertTrue(files.noneMatch(p -> p.toString().endsWith(".tmp")), "no temp file left behind");
        }
    }

    @Test
    void balanceRowMatchesSpecifiedColumns() {
        BalanceRecord row = new BalanceRecord("PS1", DAY, CLOCK, 3.25, 2.6, 270, Result.FAIL);
        assertEquals(CsvKind.Balance.headers().size(), row.values().size());
        assertEquals(List.of("PS1", "2026-10-17", "09:05:07", "3.25", "2.60", "270", "FAIL"), row.values());
    }

    @Test
    void inspectionRowMatchesSpecifiedColumns() {
        InspectionRecord row = new InspectionRecord("PS1", DAY, CLOCK, "OK", 1250.123, Result.PASS);
        assertEquals(CsvKind.Inspection.headers().size(), row.values().size());
        assertEquals(List.of("PS1", "2026-10-17", "09:05:07", "OK", "1250.123", "PASS"), row.values());
    }

    @Test
    void cellsWithCommasAreQuoted() {
        assertEquals("a,\"b,c\",\"say \"\"hi\"\"\"\r\n", CsvWriter.line(List.of("a", "b,c", "say \"hi\"")));
    }
}
