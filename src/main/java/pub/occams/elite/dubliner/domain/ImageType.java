package pub.occams.elite.dubliner.domain;

public enum ImageType {
    PP_PREPARATION("power-play/preparation"),
    PP_EXPANSION("power-play/expansion"),
    PP_CONTROL("power-play/control"),
    PP_TURMOIL("power-play/turmoil"),
    UNKNOWN("power-play/unknown");

    private final String name;

    ImageType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
