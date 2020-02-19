package watch.poe.app.utility;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ChangeIdUtility {

    private static final Pattern changeIdPattern = Pattern.compile("\\d+(-\\d+){4}");

    public static int comparator(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }

        return isNewerThan(a, b) ? 1 : 0;
    }

    public static boolean isNewerThan(String a, String b) {
        if (!isChangeId(a) || !isChangeId(b)) {
            return false;
        }

        return sumChangeId(a) > sumChangeId(b);
    }

    public static boolean isChangeId(String changeId) {
        if (changeId == null) {
            return false;
        }

        return changeIdPattern.matcher(changeId).find();
    }

    public static List<Long> splitChangeId(String changeId) {
        return Arrays.stream(changeId.split("-")).mapToLong(Long::parseLong).boxed().collect(Collectors.toList());
    }

    public static long sumChangeId(String changeId) {
        return Arrays.stream(changeId.split("-")).mapToLong(Long::parseLong).sum();
    }

    public static String find(String haystack) {
        Matcher matcher = changeIdPattern.matcher(haystack);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group();
    }
}
