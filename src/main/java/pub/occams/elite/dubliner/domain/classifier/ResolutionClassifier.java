package pub.occams.elite.dubliner.domain.classifier;

import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.domain.Resolution;

import java.util.Optional;

public class ResolutionClassifier {

    public Optional<Resolution> classify(final InputImage image) {
        final int w = image.getImage().getWidth();
        final int h = image.getImage().getHeight();
        for (final Resolution r : Resolution.values()) {
            if (r.getWidth() == w && r.getHeight() == h) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

}
