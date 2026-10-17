package angstrom.simulator.csv;

import angstrom.simulator.model.Measurements;
import angstrom.simulator.model.Result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AssemblyRecord(
        String serialNumber,
        String wipBatchNumber,
        String assemblyStation,
        LocalDate assemblyDate,
        LocalTime assemblyTime,
        double torqueValue,
        Result result) implements CsvRow {

    @Override
    public CsvKind kind() {
        return CsvKind.Assembly;
    }

    @Override
    public List<String> values() {
        return List.of(
                serialNumber,
                wipBatchNumber,
                assemblyStation,
                CsvRow.date(assemblyDate),
                CsvRow.time(assemblyTime),
                Measurements.format(torqueValue, 2),
                result.name());
    }
}
