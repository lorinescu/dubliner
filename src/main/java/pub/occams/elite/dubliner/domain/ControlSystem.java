package pub.occams.elite.dubliner.domain;

public class ControlSystem {

    private final ControlSystemRectangles controlSystemRectangles;
    private final Power power;
    private final String systemName;
    private final Integer upkeepFromLastCycle;
    private final Integer costIfFortified;
    private final Integer costIfUndermined;
    private final Integer baseIncome;
    private final Integer fortifyTotal;
    private final Integer fortifyTrigger;
    private final Integer underminingTotal;
    private final Integer underminingTrigger;


    public ControlSystem(final ControlSystemRectangles controlSystemRectangles, final Power power,
                         final String systemName,
                         final Integer upkeepFromLastCycle,
                         final Integer costIfFortified, final Integer costIfUndermined, final Integer baseIncome,
                         final Integer fortifyTotal, final Integer fortifyTrigger,
                         final Integer underminingTotal, final Integer underminingTrigger) {
        this.controlSystemRectangles = controlSystemRectangles;
        this.power = power;
        this.systemName = systemName;
        this.upkeepFromLastCycle = upkeepFromLastCycle;
        this.costIfFortified = costIfFortified;
        this.costIfUndermined = costIfUndermined;
        this.baseIncome = baseIncome;
        this.fortifyTotal = fortifyTotal;
        this.fortifyTrigger = fortifyTrigger;
        this.underminingTotal = underminingTotal;
        this.underminingTrigger = underminingTrigger;
    }

    public ControlSystemRectangles getControlSystemRectangles() {
        return controlSystemRectangles;
    }

    public Power getPower() {
        return power;
    }

    public String getSystemName() {
        return systemName;
    }

    public Integer getUpkeepFromLastCycle() {
        return upkeepFromLastCycle;
    }

    public Integer getCostIfFortified() {
        return costIfFortified;
    }

    public Integer getCostIfUndermined() {
        return costIfUndermined;
    }

    public Integer getBaseIncome() {
        return baseIncome;
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
        return power +
                "," + systemName +
                "," + upkeepFromLastCycle +
                "," + costIfFortified +
                "," + costIfUndermined +
                "," + baseIncome +
                "," + fortifyTotal +
                "," + fortifyTrigger +
                "," + underminingTotal +
                "," + underminingTrigger;
    }
}
