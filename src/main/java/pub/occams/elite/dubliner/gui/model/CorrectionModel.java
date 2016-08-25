package pub.occams.elite.dubliner.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CorrectionModel {

    private final StringProperty from = new SimpleStringProperty();
    private final StringProperty to = new SimpleStringProperty();

    public CorrectionModel(final String from, final String to) {
        setFrom(from);
        setTo(to);
    }

    public String getFrom() {
        return from.get();
    }

    public StringProperty fromProperty() {
        return from;
    }

    public void setFrom(String from) {
        this.from.set(from);
    }

    public String getTo() {
        return to.get();
    }

    public StringProperty toProperty() {
        return to;
    }

    public void setTo(String to) {
        this.to.set(to);
    }
}
