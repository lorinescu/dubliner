package pub.occams.elite.dubliner.gui.control;

import javafx.scene.control.TextField;

public class ValidTextField extends TextField {

    public ValidTextField() {
        init();
    }

    public ValidTextField(String text) {
        super(text);
        init();
    }

    private void init() {
        this.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (null == newValue || newValue.isEmpty() || "null".equals(newValue) || "-1".equals(newValue)) {
                        setStyle("-fx-background-color: #FFA4A4");
                    } else {
                        setStyle("-fx-background-color: #FFFFFF");
                    }
                }
        );
    }
}
