package angstrom.simulator.csv;

import angstrom.simulator.model.Measurements;
import angstrom.simulator.model.Result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BalanceRecord(
        String serialNumber,
        LocalDate balanceDate,
        LocalTime balanceTime,
        double unbalanceAmount,
        double correctionWeight,
        int correctionAngle,
        Result result) implements CsvRow {

    @Override
    public CsvKind kind() {
        return CsvKind.Balance;
    }

    @Override
    public List<String> values() {
        return List.of(
                serialNumber,
                CsvRow.date(balanceDate),
                CsvRow.time(balanceTime),
                Measurements.format(unbalanceAmount, 2),
                Measurements.format(correctionWeight, 2),
                Integer.toString(correctionAngle),
                result.name());
    }
}
