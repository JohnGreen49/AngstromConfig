package angstrom.simulator.csv;

import java.util.List;

/** The three CSV layouts Angstrom specified. The enum name is also the output sub-folder. */
public enum CsvKind {
    Assembly(List.of("SerialNumber", "WIPBatchNumber", "AssemblyStation", "AssemblyDate", "AssemblyTime", "TorqueValue", "Result")),
    Balance(List.of("SerialNumber", "BalanceDate", "BalanceTime", "UnbalanceAmount", "CorrectionWeight", "CorrectionAngle", "Result")),
    Inspection(List.of("SerialNumber", "InspectionDate", "InspectionTime", "VisionResult", "MeasurementData", "PassFail"));

    private final List<String> headers;

    CsvKind(List<String> headers) {
        this.headers = headers;
    }

    public List<String> headers() {
        return headers;
    }
}
