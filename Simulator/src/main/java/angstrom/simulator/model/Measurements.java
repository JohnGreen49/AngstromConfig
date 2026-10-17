package angstrom.simulator.model;

import java.util.Locale;
import java.util.Random;

/** Random but plausible process values. All formatting goes through here so CSV and payloads agree. */
public final class Measurements {
    private final Random rng;
    private final int passRatePercent;

    public Measurements(Random rng, int passRatePercent) {
        this.rng = rng;
        this.passRatePercent = passRatePercent;
    }

    public Result result() {
        return rng.nextInt(100) < passRatePercent ? Result.PASS : Result.FAIL;
    }

    /** Fastener torque in Nm, nominal 45 with a little spread. */
    public double torqueValue() {
        return round(45.0 + rng.nextGaussian() * 1.2, 2);
    }

    /** Residual unbalance in g.mm before correction. */
    public double unbalanceAmount() {
        return round(0.5 + rng.nextDouble() * 7.5, 2);
    }

    /** Correction weight in grams, roughly proportional to the unbalance. */
    public double correctionWeight(double unbalance) {
        return round(unbalance * (0.7 + rng.nextDouble() * 0.3), 2);
    }

    /** Correction angle in whole degrees. */
    public int correctionAngle() {
        return rng.nextInt(360);
    }

    /** Overall length in mm, nominal 1250. */
    public double measurementData() {
        return round(1250.0 + rng.nextGaussian() * 0.15, 3);
    }

    public String visionResult(Result passFail) {
        if (passFail == Result.PASS) return "OK";
        String[] defects = { "WELD-POROSITY", "PAINT-RUN", "MISSING-CLIP", "SCRATCH" };
        return "DEFECT-" + defects[rng.nextInt(defects.length)];
    }

    /** Applies plus or minus jitterPercent to a nominal duration. */
    public double jitter(double seconds, int jitterPercent) {
        if (jitterPercent <= 0 || seconds <= 0) return seconds;
        double spread = seconds * jitterPercent / 100.0;
        return Math.max(0, seconds + (rng.nextDouble() * 2 - 1) * spread);
    }

    public boolean chance(double probability) {
        return rng.nextDouble() < probability;
    }

    public int pick(int bound) {
        return rng.nextInt(bound);
    }

    public static String format(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
