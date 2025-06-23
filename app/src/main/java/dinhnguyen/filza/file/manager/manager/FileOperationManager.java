package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dinhnguyen.filza.file.manager.utils.PermissionManager;

/**
 * Manages file operations like copy, move with progress tracking
 */
public class FileOperationManager {
    
    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public interface FileOperationCallback {
        void onProgress(int progress);
        void onSuccess(String message);
        void onError(String error);
    }
    
    public FileOperationManager(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Copy a file or directory to destination
     */
    public void copyFile(File source, File destination, FileOperationCallback callback) {
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            callback.onError("File access permission required");
            return;
        }
        
        if (!source.exists()) {
            callback.onError("Source file does not exist");
            return;
        }
        
        if (!PermissionManager.canAccessDirectory(context, destination.getParentFile().getAbsolutePath())) {
            callback.onError("Cannot access destination directory");
            return;
        }
        
        executorService.execute(() -> {
            try {
                if (source.isDirectory()) {
                    copyDirectory(source, destination, callback);
                } else {
                    copySingleFile(source, destination, callback);
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Copy failed: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Move a file or directory to destination
     */
    public void moveFile(File source, File destination, FileOperationCallback callback) {
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            callback.onError("File access permission required");
            return;
        }
        
        if (!source.exists()) {
            callback.onError("Source file does not exist");
            return;
        }
        
        if (!PermissionManager.canAccessDirectory(context, destination.getParentFile().getAbsolutePath())) {
            callback.onError("Cannot access destination directory");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // Try to move directly first (faster for same filesystem)
                if (source.renameTo(destination)) {
                    mainHandler.post(() -> callback.onSuccess("Moved successfully"));
                    return;
                }
                
                // If direct move fails, copy then delete
                if (source.isDirectory()) {
                    copyDirectory(source, destination, callback);
                } else {
                    copySingleFile(source, destination, callback);
                }
                
                // Delete source after successful copy
                if (deleteRecursively(source)) {
                    mainHandler.post(() -> callback.onSuccess("Moved successfully"));
                } else {
                    mainHandler.post(() -> callback.onError("Moved but failed to delete source"));
                }
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Move failed: " + e.getMessage()));
            }
        });
    }
    
    private void copySingleFile(File source, File destination, FileOperationCallback callback) throws IOException {
        long totalBytes = source.length();
        long copiedBytes = 0;
        
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(destination)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                copiedBytes += bytesRead;
                
                // Update progress
                final int progress = totalBytes > 0 ? (int) ((copiedBytes * 100) / totalBytes) : 0;
                mainHandler.post(() -> callback.onProgress(progress));
            }
            
            fos.getFD().sync();
        }
    }
    
    private void copyDirectory(File source, File destination, FileOperationCallback callback) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        
        int totalFiles = countFiles(source);
        int processedFiles = 0;
        
        for (File file : files) {
            File destFile = new File(destination, file.getName());
            
            if (file.isDirectory()) {
                copyDirectory(file, destFile, callback);
            } else {
                copySingleFile(file, destFile, callback);
            }
            
            processedFiles++;
            final int progress = (processedFiles * 100) / totalFiles;
            mainHandler.post(() -> callback.onProgress(progress));
        }
    }
    
    private int countFiles(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFiles(file);
                }
                count++;
            }
        }
        return count;
    }
    
    private boolean deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }
    
    /**
     * Get file size in human readable format
     */
    public static String getFileSizeString(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Check if destination file exists and handle conflict
     */
    public static boolean handleFileConflict(File destination, Context context) {
        if (destination.exists()) {
            // Show dialog to user about file conflict
            // For now, just return false to indicate conflict
            return false;
        }
        return true;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
} 