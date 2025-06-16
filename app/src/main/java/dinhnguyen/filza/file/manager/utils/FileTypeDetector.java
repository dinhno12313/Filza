package dinhnguyen.filza.file.manager.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileTypeDetector {
    
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
    
    private static final Set<String> ZIP_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"
    ));
    
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
    
    public static boolean isZipFile(String fileName) {
        return hasExtension(fileName, ZIP_EXTENSIONS);
    }
    
    public static boolean isPdfFile(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }
    
    private static boolean hasExtension(String fileName, Set<String> extensions) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return extensions.stream().anyMatch(lowerFileName::endsWith);
    }
    
    public static String getMimeType(String fileName) {
        if (fileName == null) return "*/*";
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "txt": return "text/plain";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "mp3": return "audio/mpeg";
            case "mp4": return "video/mp4";
            case "zip": return "application/zip";
            case "rar": return "application/x-rar-compressed";
            case "7z": return "application/x-7z-compressed";
            case "tar": return "application/x-tar";
            case "gz": return "application/gzip";
            case "bz2": return "application/x-bzip2";
            default: return "*/*";
        }
    }
} 