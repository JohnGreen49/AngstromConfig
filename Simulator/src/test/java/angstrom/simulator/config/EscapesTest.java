package angstrom.simulator.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EscapesTest {

    @Test
    void plainTextPassesThrough() {
        assertArrayEquals("<1>".getBytes(StandardCharsets.ISO_8859_1), Escapes.decode("<1>"));
        assertArrayEquals("</1?>".getBytes(StandardCharsets.ISO_8859_1), Escapes.decode("</1?>"));
    }

    @Test
    void decodesControlAndHexEscapes() {
        assertArrayEquals(new byte[] { 0x02, 'A', 'B', 0x0D, 0x0A, 0, '\\', '\t' }, Escapes.decode("\\x02AB\\r\\n\\0\\\\\\t"));
    }

    @Test
    void nullAndEmptyGiveEmpty() {
        assertEquals(0, Escapes.decode(null).length);
        assertEquals(0, Escapes.decode("").length);
    }

    @Test
    void rejectsBadEscapes() {
        assertThrows(IllegalArgumentException.class, () -> Escapes.decode("\\x0"));
        assertThrows(IllegalArgumentException.class, () -> Escapes.decode("\\xZZ"));
        assertThrows(IllegalArgumentException.class, () -> Escapes.decode("\\q"));
    }
}
