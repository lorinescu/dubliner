package pub.occams.elite.dubliner.domain.image;

import java.awt.image.BufferedImage;

public class ControlSystemRectangles {

    private ClassifiedImage input;
    private BufferedImage systemName;
    private BufferedImage upkeepFromLastCycle;
    private BufferedImage defaultUpkeepCost;
    private BufferedImage costIfFortified;
    private BufferedImage costIfUndermined;
    private BufferedImage baseIncome;
    private BufferedImage fortificationTotal;
    private BufferedImage fortificationTrigger;
    private BufferedImage underminingTotal;
    private BufferedImage underminingTrigger;

    public ControlSystemRectangles(final ClassifiedImage input,
                                   final BufferedImage systemName,
                                   final BufferedImage upkeepFromLastCycle, final BufferedImage defaultUpkeepCost,
                                   final BufferedImage costIfFortified, final BufferedImage costIfUndermined,
                                   final BufferedImage baseIncome,
                                   final BufferedImage fortificationTotal, final BufferedImage fortificationTrigger,
                                   final BufferedImage underminingTotal, final BufferedImage underminingTrigger) {
        this.input = input;
        this.systemName = systemName;
        this.upkeepFromLastCycle = upkeepFromLastCycle;
        this.defaultUpkeepCost = defaultUpkeepCost;
        this.costIfFortified = costIfFortified;
        this.costIfUndermined = costIfUndermined;
        this.baseIncome = baseIncome;
        this.fortificationTotal = fortificationTotal;
        this.fortificationTrigger = fortificationTrigger;
        this.underminingTotal = underminingTotal;
        this.underminingTrigger = underminingTrigger;
    }

    public ClassifiedImage getInput() {
        return input;
    }

    public BufferedImage getSystemName() {
        return systemName;
    }

    public BufferedImage getUpkeepFromLastCycle() {
        return upkeepFromLastCycle;
    }

    public BufferedImage getDefaultUpkeepCost() {
        return defaultUpkeepCost;
    }

    public BufferedImage getCostIfFortified() {
        return costIfFortified;
    }

    public BufferedImage getCostIfUndermined() {
        return costIfUndermined;
    }

    public BufferedImage getBaseIncome() {
        return baseIncome;
    }

    public BufferedImage getFortificationTotal() {
        return fortificationTotal;
    }

    public BufferedImage getFortificationTrigger() {
        return fortificationTrigger;
    }

    public BufferedImage getUnderminingTotal() {
        return underminingTotal;
    }

    public BufferedImage getUnderminingTrigger() {
        return underminingTrigger;
    }
}
