package dinhnguyen.filza.file.manager.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileTypeUtils {
    
    private static final Set<String> PDF_EXTENSIONS = new HashSet<>(Arrays.asList(".pdf"));
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    private static final Set<String> AUDIO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp3", ".wav", ".ogg", ".m4a", ".flac", ".aac"
    ));
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp4", ".mkv", ".3gp", ".avi", ".mov", ".wmv", ".flv", ".webm"
    ));
    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".txt", ".log", ".md", ".json", ".xml", ".csv"
    ));
    
    public static boolean isPdfFile(String fileName) {
        return hasExtension(fileName, PDF_EXTENSIONS);
    }
    
    public static boolean isImageFile(String fileName) {
        return hasExtension(fileName, IMAGE_EXTENSIONS);
    }
    
    public static boolean isAudioFile(String fileName) {
        return hasExtension(fileName, AUDIO_EXTENSIONS);
    }
    
    public static boolean isVideoFile(String fileName) {
        return hasExtension(fileName, VIDEO_EXTENSIONS);
    }
    
    public static boolean isTextFile(String fileName) {
        return hasExtension(fileName, TEXT_EXTENSIONS);
    }
    
    public static boolean isPdfFile(File file) {
        return file != null && file.exists() && isPdfFile(file.getName());
    }
    
    public static boolean isImageFile(File file) {
        return file != null && file.exists() && isImageFile(file.getName());
    }
    
    public static boolean isAudioFile(File file) {
        return file != null && file.exists() && isAudioFile(file.getName());
    }
    
    public static boolean isVideoFile(File file) {
        return file != null && file.exists() && isVideoFile(file.getName());
    }
    
    public static boolean isTextFile(File file) {
        return file != null && file.exists() && isTextFile(file.getName());
    }
    
    private static boolean hasExtension(String fileName, Set<String> extensions) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return extensions.stream().anyMatch(lowerFileName::endsWith);
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