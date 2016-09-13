package pub.occams.elite.dubliner.correct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.powerplay.Power;
import pub.occams.elite.dubliner.dto.eddb.PopulatedSystemDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.util.*;
import java.util.stream.Collectors;

public class Corrector {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.LOGGER_NAME);

    public final static String TOTAL = "TOTAL";
    public final static String TRIGGER = "TRIGGER";
    public final static String REACHED = "REACHED";
    public static final String PREPARATION = "PREPARATION";
    public static final String EXPANSION = "EXPANSION";
    public static final String CONTROL = "CONTROL";
    public static final String UNKNOWN_SYSTEM = "UNKNOWN_SYSTEM";
    public static final String CC = "CC";
    public static final String PREP = "PREP";

    private final Map<String, String> systems;
    private final Map<String, String> powers;
    private final Set<String> knownSystemNames;

    private Corrector(final Map<String, String> systems, final Map<String, String> powers,
                      final Set<String> knownSystemNames) {
        this.systems = systems;
        this.powers = powers;
        this.knownSystemNames = knownSystemNames;
    }

    private static int editDistance(final String a, final String b) {

        int distance = 0;
        int aLen = a.length();
        int bLen = b.length();

        for (int i = 0; i < aLen && i < bLen; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                distance++;
            }
        }
        distance += Math.abs(aLen - bLen);

        if (a.contains("KAWIL0CIDI") && b.contains("KAWILOCIDI")) {
            System.out.println("DISTANCE = " + distance);
        }
        return distance;
    }

    public String cleanSystemName(final String str) {
        final String nameWithoutTurmoil = str.replaceAll("(\\(|\\[).*", "").trim().toUpperCase();
        if (systems.containsKey(nameWithoutTurmoil)) {
            return systems.get(nameWithoutTurmoil);
        }

        //TODO: evaluate a weighted option and a more relevant algo (ex: levensthein distance)
        //find a similar name in known systems
        final int minSimilarityByEditDistance = 4;
        final List<String> possibleNames =
                knownSystemNames
                        .stream()
                        .map(name -> name.trim().toUpperCase())
                        .filter(name -> editDistance(name, nameWithoutTurmoil) < minSimilarityByEditDistance)
                        .collect(Collectors.toList());
        //if we get more than 1 system -> unreliable result
        if (possibleNames.size() == 1) {
            return possibleNames.get(0);
        }

        //best effort
        return nameWithoutTurmoil;
    }

    public Optional<Power> powerFromString(final String str) {

        final String powerString = str.trim().toUpperCase();

        String correctedPowerString = powerString;
        for (final String badName : powers.keySet()) {
            if (powerString.contains(badName)) {
                correctedPowerString = powers.get(badName);
                break;
            }
        }

        for (final Power p : Power.values()) {
            if (correctedPowerString.contains(p.name)) {
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
                            .replace(CC, "")
                            .replace(PREP, "")
                            .replace("O", "0")
                            .replace("'", "")
                            .replace(" ", "")
                            .replace("-", "")
                            .replace("B", "8")
                            .replace("D", "0")
                            .replace("S", "9")
                            .replace("EI", "8")
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
                .replace("T0TAL", TOTAL)
                .replace("TDTAL", TOTAL)
                .replace("TRDGGER", TRIGGER);
    }

    public static Corrector buildCorrector(final SettingsDto settings, final List<PopulatedSystemDto> populatedSystems) {
        final Map<String, String> systems = new HashMap<>();
        settings.corrections.systemName.forEach(systems::put);

        final Map<String, String> powers = new HashMap<>();
        settings.corrections.powerName.forEach(powers::put);

        final Set<String> knownSystemNames = populatedSystems.
                stream()
                .filter(sys -> null != sys.name && !sys.name.isEmpty())
                .map(sys -> sys.name.trim().toUpperCase())
                .collect(Collectors.toSet());

        return new Corrector(systems, powers, knownSystemNames);
    }
}
