package pub.occams.elite.dubliner.domain;

public enum Power {
    AISLING_DUVAL("aaaaaaaaaaaaa"),
    ARCHON_DELAINE("aaaaaaaaaaaaa"),
    ARISSA_LAVIGNY_DUVAL("A. LAVIGNY-DUVAL"),
    DENTON_PATREUS("aaaaaaaaaaaaa"),
    EDMUND_MAHON("EDMUND MAHON"),
    FELICIA_WINTERS("aaaaaaaaaaaaa"),
    LI_YONG_RUI("aaaaaaaaaaaaa"),
    PRANAV_ANTAL("aaaaaaaaaaaaa"),
    ZACHARY_HUDSON("aaaaaaaaaaaaa"),
    ZEMINA_TORVAL("aaaaaaaaaaaaa");

    private final String name;

    Power(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
