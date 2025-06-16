package dinhnguyen.filza.file.manager.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AudioFileValidator {
    
    private static final Set<String> AUDIO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp3", ".wav", ".ogg", ".m4a", ".flac", ".aac", ".wma", ".aiff"
    ));
    
    public static boolean isValidAudioFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        return AUDIO_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
    }
    
    public static boolean isValidAudioFile(File file) {
        return file != null && file.exists() && isValidAudioFile(file.getName());
    }
    
    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex).toLowerCase() : "";
    }
    
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
} 