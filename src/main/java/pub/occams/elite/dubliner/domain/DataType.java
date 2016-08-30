package pub.occams.elite.dubliner.domain;

public enum DataType {
    NUMERIC("Numeric"),
    ALPHA("Alpha"),
    ALPHA_NUMERIC("Alpha-numeric");

    private final String name;

    DataType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
