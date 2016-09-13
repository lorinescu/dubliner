package pub.occams.elite.dubliner.domain.powerplay;

public enum Power {
    AISLING_DUVAL("AISLING DUVAL"),
    ARCHON_DELAINE("ARCHON DELAINE"),
    ARISSA_LAVIGNY_DUVAL("A. LAVIGNY-DUVAL"),
    DENTON_PATREUS("DENTON PATREUS"),
    EDMUND_MAHON("EDMUND MAHON"),
    FELICIA_WINTERS("FELICIA WINTERS"),
    LI_YONG_RUI("LI YONG-RUI"),
    PRANAV_ANTAL("PRANAV ANTAL"),
    ZACHARY_HUDSON("ZACHARY HUDSON"),
    ZEMINA_TORVAL("ZEMINA TORVAL"),
    UNKNOWN("unknown");

    public final String name;

    Power(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
