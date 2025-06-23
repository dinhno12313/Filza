package dinhnguyen.filza.file.manager.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {
    
    /**
     * Get all available storage directories
     */
    public static List<File> getAvailableStorages(Context context) {
        List<File> storages = new ArrayList<>();
        
        // Add internal storage
        File internalStorage = context.getFilesDir();
        if (internalStorage != null && internalStorage.exists()) {
            storages.add(internalStorage);
        }
        
        // Add external storage
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null && externalStorage.exists() && externalStorage.canRead()) {
            storages.add(externalStorage);
        }
        
        // Add SD card if available
        File[] externalFilesDirs = context.getExternalFilesDirs(null);
        if (externalFilesDirs != null) {
            for (File dir : externalFilesDirs) {
                if (dir != null && dir.exists() && dir.canRead()) {
                    File storage = getStorageRoot(dir);
                    if (storage != null && !storages.contains(storage)) {
                        storages.add(storage);
                    }
                }
            }
        }
        
        return storages;
    }
    
    /**
     * Get the root storage directory from a path
     */
    private static File getStorageRoot(File path) {
        if (path == null) return null;
        
        File current = path;
        while (current.getParentFile() != null) {
            File parent = current.getParentFile();
            if (parent.getPath().equals("/storage") || parent.getPath().equals("/mnt")) {
                return current;
            }
            current = parent;
        }
        
        return current;
    }
    
    /**
     * Check if a directory is writable
     */
    public static boolean isDirectoryWritable(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return false;
        }
        
        // Check if we can write to the directory
        File testFile = new File(directory, ".test_write_" + System.currentTimeMillis());
        try {
            if (testFile.createNewFile()) {
                testFile.delete();
                return true;
            }
        } catch (Exception e) {
            // Ignore exceptions
        }
        
        return false;
    }
    
    /**
     * Get available space in a directory
     */
    public static long getAvailableSpace(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        
        try {
            StatFs statFs = new StatFs(directory.getPath());
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get total space in a directory
     */
    public static long getTotalSpace(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        
        try {
            StatFs statFs = new StatFs(directory.getPath());
            return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Format file size in human readable format
     */
    public static String formatFileSize(long size) {
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
    
    /**
     * Get a safe initial directory for the folder picker
     */
    public static File getSafeInitialDirectory(Context context) {
        // Try external storage first
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null && externalStorage.exists() && externalStorage.canRead()) {
            return externalStorage;
        }
        
        // Fall back to internal storage
        return context.getFilesDir();
    }
    
    /**
     * Check if a path is within the app's private directory
     */
    public static boolean isPrivateDirectory(Context context, File file) {
        if (file == null) return false;
        
        try {
            String filePath = file.getCanonicalPath();
            String appPath = context.getFilesDir().getCanonicalPath();
            return filePath.startsWith(appPath);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Create a directory if it doesn't exist
     */
    public static boolean createDirectoryIfNotExists(File directory) {
        if (directory == null) return false;
        
        if (directory.exists()) {
            return directory.isDirectory();
        }
        
        return directory.mkdirs();
    }
} 