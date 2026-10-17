# Angstrom Line Simulator

Stands in for the PropshaftLine1 assembly line so PlantWatch can be exercised without the
plant. It plays every socket device in `../pwDevice.yaml` (barcode scanners and cameras) and
drops the per-station CSV files PlantWatch's ReadFromFile devices pick up.

## How it fits PlantWatch

* PlantWatch's **Socket device is a TCP client**. For each device the simulator listens on a
  port; PlantWatch connects to `<simulator host>:<port>` and the simulator pushes one framed
  message per event: `prefix + payload + suffix`, ISO-8859-1, e.g. `<1>PS261017-00042</1?>`.
  The prefix and suffix must match `searchMethodPrefix` / `searchMethodSuffix` on the device.
* CSV files are written to `<outputDirectory>\<Kind>\<Kind>_<serial>_<yyyyMMdd_HHmmss>.csv`,
  first to a `.tmp` name and then renamed, so a watcher on `*.csv` never reads a partial file.

## Line model

| Station          | Role            | Emits                                   |
|------------------|-----------------|-----------------------------------------|
| PrototypeTable1/2| PrototypeEntry  | scan on ScannerP1 / ScannerP2           |
| ProductionTable1/2| ProductionEntry| scan on ScannerProd1 / ScannerProd2     |
| Welder           | Process         | WeldCam event                           |
| MarryUp          | Process         | MaryUpCam event + `Assembly` CSV        |
| Balance          | Process         | BalanceCam event + `Balance` CSV        |
| Paint            | Process         | nothing, time only                      |
| Final QA         | Process         | `Inspection` CSV                        |

Prototype parts enter at a prototype table, are scanned again at a production table, then
follow the same path as production parts. Which table is picked is random. Many parts are on
the line at once; each runs on its own thread with the cycle times from `simulator.yaml`.

CSV columns:

* **Assembly**: SerialNumber, WIPBatchNumber, AssemblyStation, AssemblyDate, AssemblyTime, TorqueValue, Result
* **Balance**: SerialNumber, BalanceDate, BalanceTime, UnbalanceAmount, CorrectionWeight, CorrectionAngle, Result
* **Inspection**: SerialNumber, InspectionDate, InspectionTime, VisionResult, MeasurementData, PassFail

Dates are `yyyy-MM-dd`, times `HH:mm:ss`, TorqueValue / UnbalanceAmount / CorrectionWeight have
two decimals, MeasurementData three, CorrectionAngle is a whole number, Result and PassFail are
`PASS` or `FAIL`.

## Build and run

Requires JDK 21. Maven is not required: the wrapper downloads it.

```
mvnw.cmd package
java -jar target\line-simulator.jar
```

Options:

```
--config, -c <file>   simulator.yaml to load (default: .\simulator.yaml)
--out,    -o <dir>    override outputDirectory
--parts,  -n <count>  stop after this many parts (0 = run until Ctrl+C)
--rate,   -r <ppm>    parts per minute
```

In IntelliJ: File > Open the `Simulator` folder, let it import the Maven project, then run
`angstrom.simulator.Main` with the working directory set to the `Simulator` folder.

## Configuration

Everything lives in `simulator.yaml`: output folder, framing, line rates, the device ports and
the station list. To move the Assembly CSV from MarryUp to Welder, move the `csv: Assembly`
line. To change what a scanner sends, edit that device's `payload` template.

## Matching PlantWatch config

`../pwDevice.yaml` must point each Socket device at the simulator host with the same port as
`simulator.yaml`, and have one ReadFromFile device per CSV folder, for example:

```yaml
- fileSpec: C:\PlantWatchShare\PropshaftLine1\Assembly\*.csv
  scanRate: Continuous
  deleteFileAfterRead: true
  name: MarryUpCsv
  deviceType: ReadFromFile
  station: MarryUp
  disabled: false
```

## Tests

`mvnw.cmd test` runs unit tests for framing, CSV output, config validation and routing, plus an
end-to-end run of the shipped configuration against fake PlantWatch clients.
