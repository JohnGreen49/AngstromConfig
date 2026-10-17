package angstrom.simulator.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hands out serial numbers and WIP batch numbers. Serials look like PS261017-00042 and
 * batches like WIP-20261017-002, rolling over every {@code batchSize} parts.
 */
public final class SerialNumbers {
    private final String prefix;
    private final int batchSize;
    private final String serialDay;
    private final String batchDay;
    private final AtomicInteger sequence = new AtomicInteger();

    public SerialNumbers(String prefix, int batchSize) {
        this(prefix, batchSize, LocalDate.now());
    }

    public SerialNumbers(String prefix, int batchSize, LocalDate day) {
        if (batchSize < 1) throw new IllegalArgumentException("batchSize must be >= 1");
        this.prefix = prefix == null ? "" : prefix;
        this.batchSize = batchSize;
        this.serialDay = day.format(DateTimeFormatter.ofPattern("yyMMdd"));
        this.batchDay = day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public Part next(boolean prototype) {
        int n = sequence.incrementAndGet();
        String serial = String.format("%s%s-%05d", prefix, serialDay, n);
        int batchNumber = (n - 1) / batchSize + 1;
        String batch = String.format("WIP-%s-%03d", batchDay, batchNumber);
        return new Part(n, serial, batch, prototype);
    }

    public int issued() {
        return sequence.get();
    }
}
