package pub.occams.elite.dubliner.domain.image;

public enum ImageType {
    PP_PREPARATION("preparation"),
    PP_EXPANSION("expansion"),
    PP_CONTROL("control"),
    NEWS_BOUNTY_HUNTER("bountyHunter"),
    NEWS_CRIME("crime"),
    NEWS_TRAFFIC("traffic"),
    NEWS_BOUNTIES("bounties"),
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
