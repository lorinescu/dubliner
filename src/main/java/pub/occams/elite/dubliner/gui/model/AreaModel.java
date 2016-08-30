package pub.occams.elite.dubliner.gui.model;

import javafx.beans.property.*;
import pub.occams.elite.dubliner.domain.DataType;

public class AreaModel {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<DataType> type = new SimpleObjectProperty<>();
    private final IntegerProperty x0 = new SimpleIntegerProperty();
    private final IntegerProperty y0 = new SimpleIntegerProperty();
    private final IntegerProperty x1 = new SimpleIntegerProperty();
    private final IntegerProperty y1 = new SimpleIntegerProperty();

    public AreaModel(final String name, final DataType type, final int x0, final int y0, final int x1, final int y1) {
        setName(name);
        setType(type);
        setX0(x0);
        setY0(y0);
        setX1(x1);
        setY1(y1);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public DataType getType() {
        return type.get();
    }

    public ObjectProperty<DataType> typeProperty() {
        return type;
    }

    public void setType(DataType type) {
        this.type.set(type);
    }

    public int getX0() {
        return x0.get();
    }

    public IntegerProperty x0Property() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0.set(x0);
    }

    public int getY0() {
        return y0.get();
    }

    public IntegerProperty y0Property() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0.set(y0);
    }

    public int getX1() {
        return x1.get();
    }

    public IntegerProperty x1Property() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1.set(x1);
    }

    public int getY1() {
        return y1.get();
    }

    public IntegerProperty y1Property() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1.set(y1);
    }
}
