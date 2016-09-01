package pub.occams.elite.dubliner.domain.geometry;

public class DataRectangle<T> {
    public final T data;
    public final Rectangle rectangle;

    public DataRectangle(final T data, final Rectangle rectangle) {
        this.data = data;
        this.rectangle = rectangle;
    }

    public T getData() {
        return data;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
