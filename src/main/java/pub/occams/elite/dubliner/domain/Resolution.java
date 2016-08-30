package pub.occams.elite.dubliner.domain;

public enum Resolution {

    R_1920x1200(1920, 1200),
    R_1920x10180(1920, 1080);

    private final int width;
    private final int height;

    Resolution(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
