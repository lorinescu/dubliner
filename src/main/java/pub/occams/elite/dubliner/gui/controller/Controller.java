package pub.occams.elite.dubliner.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import pub.occams.elite.dubliner.application.ImageApi;

public abstract class Controller<VIEW extends Pane> {

    @FXML
    private VIEW view;

    public VIEW getView() {
        return view;
    }

    public void setView(VIEW view) {
        this.view = view;
    }

}
