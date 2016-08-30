package pub.occams.elite.dubliner.domain;

public class ControlSystem {

    private final ControlSystemRectangles controlSystemRectangles;
    private final String systemName;
    private final Integer upkeepCost;
    private final Integer defaultUpkeepCost;
    private final Integer costIfFortified;
    private final Integer costIfUndermined;
    private final Integer fortifyTotal;
    private final Integer fortifyTrigger;
    private final Integer underminingTotal;
    private final Integer underminingTrigger;

    public ControlSystem(final ControlSystemRectangles controlSystemRectangles, final String systemName,
                         final Integer upkeepCost, final Integer defaultUpkeepCost, final Integer costIfFortified,
                         final Integer costIfUndermined, final Integer fortifyTotal, final Integer fortifyTrigger,
                         final Integer underminingTotal, final Integer underminingTrigger) {
        this.controlSystemRectangles = controlSystemRectangles;
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

    public ControlSystemRectangles getControlSystemRectangles() {
        return controlSystemRectangles;
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
