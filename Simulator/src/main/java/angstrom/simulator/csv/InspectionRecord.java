package angstrom.simulator.csv;

import angstrom.simulator.model.Measurements;
import angstrom.simulator.model.Result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record InspectionRecord(
        String serialNumber,
        LocalDate inspectionDate,
        LocalTime inspectionTime,
        String visionResult,
        double measurementData,
        Result passFail) implements CsvRow {

    @Override
    public CsvKind kind() {
        return CsvKind.Inspection;
    }

    @Override
    public List<String> values() {
        return List.of(
                serialNumber,
                CsvRow.date(inspectionDate),
                CsvRow.time(inspectionTime),
                visionResult,
                Measurements.format(measurementData, 3),
                passFail.name());
    }
}
