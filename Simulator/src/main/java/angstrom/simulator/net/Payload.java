package angstrom.simulator.net;

import java.util.Map;

/** Fills {placeholder} tokens in a device payload template. Unknown tokens are left as-is. */
public final class Payload {
    private Payload() {}

    public static String fill(String template, Map<String, String> fields) {
        String out = template;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
