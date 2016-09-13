package pub.occams.elite.dubliner.gui.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.controlsfx.control.PopOver;
import pub.occams.elite.dubliner.correct.Corrector;
import pub.occams.elite.dubliner.domain.powerplay.Power;

public class OcrDataTextField extends TextField {

    private final TextArea popoverContent;
    private final PopOver popover;
    private final BooleanProperty click = new SimpleBooleanProperty(false);

    public OcrDataTextField() {
        this(null);
    }

    public OcrDataTextField(final String text) {
        super(text);
        popoverContent = new TextArea();
        popover = new PopOver(popoverContent);
        init();
    }

    private void init() {


        textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (null == newValue || newValue.isEmpty() || "null".equals(newValue) || "-1".equals(newValue) ||
                            Power.UNKNOWN.name.equals(newValue) || Corrector.UNKNOWN_SYSTEM.equals(newValue)) {
                        setStyle("-fx-background-color: #FFA4A4");
                    } else {
                        setStyle("-fx-background-color: #FFFFFF");
                    }
                }
        );

        setOnMouseClicked(
                event -> click.set(!click.get())
        );

        click.addListener(
                (observable, oldValue, clickSet) -> {
                    if (clickSet) {
                        popover.show(this);
                    } else {
                        popover.hide();
                    }
                }
        );
        popoverContent.setMaxHeight(150);
        popoverContent.setMaxWidth(350);
        popover.setArrowLocation(PopOver.ArrowLocation.RIGHT_TOP);
    }

    public void setText(final String text, final String tooltip) {
        setText(text);
        setTooltip(tooltip);
    }

    public void setTooltip(final String tooltip) {
        this.popoverContent.setText(tooltip);
    }
}
