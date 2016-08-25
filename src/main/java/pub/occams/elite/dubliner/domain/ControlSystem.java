package pub.occams.elite.dubliner.domain;

public class ControlSystem {

    private ControlSystemSegments controlSystemSegments;
    private String systemName;
    private Integer upkeepCost;
    private Integer defaultUpkeepCost;
    private Integer costIfFortified;
    private Integer costIfUndermined;
    private Integer fortifyTotal;
    private Integer fortifyTrigger;
    private Integer underminingTotal;
    private Integer underminingTrigger;

    public ControlSystem(final ControlSystemSegments controlSystemSegments, String systemName, Integer upkeepCost,
                         Integer defaultUpkeepCost, Integer costIfFortified, Integer costIfUndermined, Integer fortifyTotal, Integer fortifyTrigger, Integer underminingTotal, Integer underminingTrigger) {
        this.controlSystemSegments = controlSystemSegments;
        this.systemName = systemName;
        this.upkeepCost = upkeepCost;
        this.defaultUpkeepCost = defaultUpkeepCost;
        this.costIfFortified = costIfFortified;
        this.costIfUndermined = costIfUndermined;
        this.fortifyTotal = fortifyTotal;
        this.fortifyTrigger = fortifyTrigger;
        this.underminingTotal = underminingTotal;
        this.underminingTrigger = underminingTrigger;
    }

    public ControlSystemSegments getControlSystemSegments() {
        return controlSystemSegments;
    }

    public String getSystemName() {
        return systemName;
    }

    public Integer getUpkeepCost() {
        return upkeepCost;
    }

    public Integer getDefaultUpkeepCost() {
        return defaultUpkeepCost;
    }

    public Integer getCostIfFortified() {
        return costIfFortified;
    }

    public Integer getCostIfUndermined() {
        return costIfUndermined;
    }

    public Integer getFortifyTotal() {
        return fortifyTotal;
    }

    public Integer getFortifyTrigger() {
        return fortifyTrigger;
    }

    public Integer getUnderminingTotal() {
        return underminingTotal;
    }

    public Integer getUnderminingTrigger() {
        return underminingTrigger;
    }

    @Override
    public String toString() {
        return systemName
                + "," + upkeepCost
                + "," + defaultUpkeepCost
                + ","+ costIfFortified
                + "," + costIfUndermined
                + "," + fortifyTotal
                + "," + fortifyTrigger
                + "," + underminingTotal
                + "," + underminingTrigger;
    }
}
