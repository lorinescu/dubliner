package pub.occams.elite.dubliner.correct;

import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.dto.settings.CorrectionsDto;

import java.util.Map;
import java.util.Optional;

public class Corrector {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    public final static String TOTAL = "TOTAL";
    public final static String TRIGGER = "TRIGGER";
    public final static String REACHED = "REACHED";
    public static final String PREPARATION = "PREPARATION";
    public static final String EXPANSION = "EXPANSION";
    public static final String CONTROL = "CONTROL";

    private final CorrectionsDto corrections;

    public Corrector(final CorrectionsDto corrections) {
        this.corrections = corrections;
    }

    public String cleanSystemName(final String str) {
        final String nameWithoutTurmoil = str.replaceAll("(\\(|\\[).*", "").trim();
        if (corrections.systemName.containsKey(nameWithoutTurmoil)) {
            return corrections.systemName.get(nameWithoutTurmoil);
        } else {
            return nameWithoutTurmoil;
        }
    }

    public Optional<Power> powerFromString(final String str) {

        final String powerString = str.trim().toUpperCase();

        final Map<String, String> corr = corrections.powerName;

        String correctedPowerString = powerString;
        for (final String badName : corr.keySet()) {
            if (powerString.contains(badName)) {
                correctedPowerString = corr.get(badName);
                break;
            }
        }

        for (final Power p : Power.values()) {
            if (correctedPowerString.contains(p.getName())) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    public Integer cleanPositiveInteger(final String str) {
        try {
            return Integer.parseInt(
                    str
                            .trim()
                            .replace("CC", "")
                            .replace("O", "0")
                            .replace("'", "")
                            .replace(" ", "")
                            .replace("-", "")
                            .replace("B", "8")
                            .replace("D", "0")
            );
        } catch (final NumberFormatException | NullPointerException ignored) {
            LOGGER.error("could not parse positive integer from: [" + str + "]");
        }
        return -1;
    }

    public String correctGlobalOcrErrors(final String str) {
        return str
                .trim()
                .toUpperCase()
                .replace("\n\n", "\n")
                .replace("T0TAL", "TOTAL")
                .replace("TDTAL", "TOTAL");
    }
}
