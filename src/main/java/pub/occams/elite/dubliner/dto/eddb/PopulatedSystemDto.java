package pub.occams.elite.dubliner.dto.eddb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PopulatedSystemDto {
    @JsonProperty("allegiance")
    public String allegiance;
    @JsonProperty("edsm_id")
    public Long edsmId;
    @JsonProperty("faction")
    public String faction;
    @JsonProperty("government")
    public String government;
    @JsonProperty("id")
    public Long id;
    @JsonProperty("is_populated")
    public Integer isPopulated;
    @JsonProperty("name")
    public String name;
    @JsonProperty("needs_permit")
    public Integer needsPermit;
    @JsonProperty("population")
    public Long population;
    @JsonProperty("power")
    public Object power;
    @JsonProperty("power_state")
    public Object powerState;
    @JsonProperty("primary_economy")
    public String primaryEconomy;
    @JsonProperty("reserve_type")
    public String reserveType;
    @JsonProperty("security")
    public String security;
    @JsonProperty("simbad_ref")
    public String simbadRef;
    @JsonProperty("state")
    public String state;
    @JsonProperty("updated_at")
    public Integer updatedAt;
    @JsonProperty("x")
    public Double x;
    @JsonProperty("y")
    public Double y;
    @JsonProperty("z")
    public Double z;
}
