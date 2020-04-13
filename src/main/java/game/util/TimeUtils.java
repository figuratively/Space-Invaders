package game.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    public static String getReadableTime(long timeMillis) {
        long timeSeconds = timeMillis / 1000;
        long minutes = timeSeconds / 60;
        long seconds = timeSeconds % 60;
        return (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static long getMillisTime(String readableTime) throws InvalidDateFormatException {
        Pattern timePattern = Pattern.compile("[0-5][0-9]:[0-5][0-9]");
        Matcher timeMatcher = timePattern.matcher(readableTime);
        if(timeMatcher.matches()) {
            String matchedReadableTime = timeMatcher.group();
            try {
                String[] splitReadableTime = matchedReadableTime.split(":");
                int minutes = Integer.parseInt(splitReadableTime[0]);
                int seconds = Integer.parseInt(splitReadableTime[1]);
                return (minutes * 60 + seconds) * 1000;
            } catch (Exception e) {
                throw new InvalidDateFormatException();
            }
        } else {
            throw new InvalidDateFormatException();
        }
    }
}
