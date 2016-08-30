package pub.occams.elite.dubliner.domain;

public class DataArea<T> {
    private final String name;
    private final DataType type;
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;

    public DataArea(final String name, final DataType type, final int x0, final int y0, final int x1, final int y1) {
        this.name = name;
        this.type = type;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }
}
