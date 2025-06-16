package dinhnguyen.filza.file.manager.utils;

import java.util.Locale;

public class TimeFormatter {
    
    public static String formatTime(int milliseconds) {
        if (milliseconds < 0) return "00:00";
        
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = (milliseconds / (1000 * 60 * 60)) % 24;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
    
    public static String formatTimeShort(int milliseconds) {
        if (milliseconds < 0) return "0:00";
        
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
    
    public static int millisecondsToSeconds(int milliseconds) {
        return milliseconds / 1000;
    }
    
    public static int secondsToMilliseconds(int seconds) {
        return seconds * 1000;
    }
} 