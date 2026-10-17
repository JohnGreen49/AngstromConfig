package angstrom.simulator.config;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decodes the escape sequences PlantWatch allows in delimiters: \r \n \t \0 \\ and \xHH.
 * Output is bytes in ISO-8859-1, the same encoding PlantWatch's SocketDevice uses on the wire.
 */
public final class Escapes {
    private Escapes() {}

    public static byte[] decode(String text) {
        if (text == null || text.isEmpty()) return new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != '\\' || i + 1 >= text.length()) {
                out.writeBytes(String.valueOf(c).getBytes(StandardCharsets.ISO_8859_1));
                i++;
                continue;
            }
            char e = text.charAt(i + 1);
            switch (e) {
                case 'r' -> { out.write('\r'); i += 2; }
                case 'n' -> { out.write('\n'); i += 2; }
                case 't' -> { out.write('\t'); i += 2; }
                case '0' -> { out.write(0); i += 2; }
                case '\\' -> { out.write('\\'); i += 2; }
                case 'x' -> {
                    if (i + 3 >= text.length())
                        throw new IllegalArgumentException("incomplete \\x escape in '" + text + "'");
                    String hex = text.substring(i + 2, i + 4);
                    try {
                        out.write(Integer.parseInt(hex, 16));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("bad hex escape \\x" + hex + " in '" + text + "'");
                    }
                    i += 4;
                }
                default -> throw new IllegalArgumentException("unknown escape \\" + e + " in '" + text + "'");
            }
        }
        return out.toByteArray();
    }
}
