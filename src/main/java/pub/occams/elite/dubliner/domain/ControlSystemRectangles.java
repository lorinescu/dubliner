package pub.occams.elite.dubliner.domain;

import java.awt.image.BufferedImage;

public class ControlSystemRectangles {

    private InputImage inputImage;
    private BufferedImage systemName;
    private BufferedImage upkeepCost;
    private BufferedImage defaultUpkeepCost;
    private BufferedImage costIfFortified;
    private BufferedImage costIfUndermined;
    private BufferedImage fortificationTotal;
    private BufferedImage fortificationTrigger;
    private BufferedImage underminingTotal;
    private BufferedImage underminingTrigger;

    public ControlSystemRectangles(final InputImage inputImage,
                                   final BufferedImage systemName,
                                   final BufferedImage upkeepCost, final BufferedImage defaultUpkeepCost,
                                   final BufferedImage costIfFortified, final BufferedImage costIfUndermined,
                                   final BufferedImage fortificationTotal, final BufferedImage fortificationTrigger,
                                   final BufferedImage underminingTotal, final BufferedImage underminingTrigger) {
        this.inputImage = inputImage;
        this.systemName = systemName;
        this.upkeepCost = upkeepCost;
        this.defaultUpkeepCost = defaultUpkeepCost;
        this.costIfFortified = costIfFortified;
        this.costIfUndermined = costIfUndermined;
        this.fortificationTotal = fortificationTotal;
        this.fortificationTrigger = fortificationTrigger;
        this.underminingTotal = underminingTotal;
        this.underminingTrigger = underminingTrigger;
    }

    public InputImage getInputImage() {
        return inputImage;
    }

    public BufferedImage getSystemName() {
        return systemName;
    }

    public BufferedImage getUpkeepCost() {
        return upkeepCost;
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
