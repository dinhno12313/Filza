package dinhnguyen.filza.file.manager.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import dinhnguyen.filza.file.manager.R;

import java.io.File;

public class FileIconManager {
    
    public static Drawable getFileIcon(Context context, File file) {
        if (file.isDirectory()) {
            return ContextCompat.getDrawable(context, R.drawable.ic_folder);
        }
        
        String fileName = file.getName().toLowerCase();
        
        // Document files
        if (FileTypeDetector.isPdfFile(fileName)) {
            return ContextCompat.getDrawable(context, R.drawable.ic_pdf);
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_word);
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_excel);
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_powerpoint);
        } else if (FileTypeDetector.isTextFile(fileName)) {
            return ContextCompat.getDrawable(context, R.drawable.ic_text);
        }
        
        // Media files
        if (FileTypeDetector.isImageFile(fileName)) {
            return ContextCompat.getDrawable(context, R.drawable.ic_image);
        } else if (FileTypeDetector.isVideoFile(fileName)) {
            return ContextCompat.getDrawable(context, R.drawable.ic_video);
        } else if (FileTypeDetector.isAudioFile(fileName)) {
            return ContextCompat.getDrawable(context, R.drawable.ic_audio);
        }
        
        // Archive files
        if (fileName.endsWith(".zip") || fileName.endsWith(".rar") || 
            fileName.endsWith(".7z") || fileName.endsWith(".tar") ||
            fileName.endsWith(".gz")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_archive);
        }
        
        // Default file icon
        return ContextCompat.getDrawable(context, R.drawable.ic_file);
    }
    
    public static String getFileInfo(File file) {
        if (file.isDirectory()) {
            return "Folder";
        }
        
        long size = file.length();
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static int getIconResForMimeType(String mimeType) {
        if (mimeType == null) return R.drawable.ic_file;
        if (mimeType.equals("application/pdf")) return R.drawable.ic_pdf;
        if (mimeType.equals("application/msword") || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return R.drawable.ic_word;
        if (mimeType.equals("application/vnd.ms-excel") || mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return R.drawable.ic_excel;
        if (mimeType.equals("application/vnd.ms-powerpoint") || mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) return R.drawable.ic_powerpoint;
        if (mimeType.startsWith("image/")) return R.drawable.ic_image;
        if (mimeType.startsWith("video/")) return R.drawable.ic_video;
        if (mimeType.startsWith("audio/")) return R.drawable.ic_audio;
        if (mimeType.equals("application/zip") || mimeType.equals("application/x-rar-compressed") || mimeType.equals("application/x-7z-compressed") || mimeType.equals("application/x-tar") || mimeType.equals("application/gzip") || mimeType.equals("application/x-bzip2")) return R.drawable.ic_archive;
        return R.drawable.ic_file;
    }
} 