package pub.occams.elite.dubliner.domain;

public enum ImageType {
    PP_PREPARATION("preparation"),
    PP_EXPANSION("expansion"),
    PP_CONTROL("control"),
    UNKNOWN("unknown");

    private final String name;

    ImageType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
