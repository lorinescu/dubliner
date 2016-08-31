package pub.occams.elite.dubliner.domain.geometry;

public class LineSegment {

    public final int x0;
    public final int y0;
    public final int x1;
    public final int y1;

    public LineSegment(final int x0,final int y0, final int x1,final int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LineSegment that = (LineSegment) o;

        if (x0 != that.x0) return false;
        if (y0 != that.y0) return false;
        if (x1 != that.x1) return false;
        return y1 == that.y1;

    }

    @Override
    public int hashCode() {
        int result = x0;
        result = 31 * result + y0;
        result = 31 * result + x1;
        result = 31 * result + y1;
        return result;
    }
}
