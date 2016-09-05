package pub.occams.elite.dubliner.domain.geometry;

public class Rectangle {
    public final int x0;
    public final int y0;
    public final int x1;
    public final int y1;

    public Rectangle(final int x0, final int y0, final int x1, final int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public String toString() {
        return "x0=" + x0 + ", y0=" + y0 + ", x1=" + x1 + ", y1=" + y1;
    }
}
