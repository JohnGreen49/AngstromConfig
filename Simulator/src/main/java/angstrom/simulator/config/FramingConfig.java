package angstrom.simulator.config;

/**
 * Message delimiters, matching PlantWatch's searchMethodPrefix / searchMethodSuffix on the
 * Socket device. Escapes such as \r, \n and \x02 are accepted, as PlantWatch accepts them.
 */
public class FramingConfig {
    private String prefix = "<1>";
    private String suffix = "</1?>";

    public String getPrefix() { return prefix == null ? "" : prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix == null ? "" : suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public byte[] prefixBytes() { return Escapes.decode(getPrefix()); }
    public byte[] suffixBytes() { return Escapes.decode(getSuffix()); }
}
