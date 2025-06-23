package dinhnguyen.filza.file.manager.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for bulk file operations
 * Handles copy, move, delete, and zip operations asynchronously
 */
public class FileUtils {
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    public interface OperationCallback {
        void onProgress(int current, int total, String currentFile);
        void onSuccess(String message);
        void onError(String error);
        void onComplete();
    }
    
    public interface ZipCallback {
        void onProgress(int current, int total, String currentFile);
        void onSuccess(File zipFile);
        void onError(String error);
        void onComplete();
    }
    
    /**
     * Copy multiple files to a destination folder
     */
    public static void copyFiles(Context context, List<File> files, File destinationFolder, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int totalFiles = calculateTotalFileCount(files);
                int currentFile = 0;
                int successCount = 0;
                
                for (File sourceFile : files) {
                    try {
                        currentFile++;
                        notifyProgress(callback, currentFile, totalFiles, "Copying " + sourceFile.getName());
                        
                        File destFile = getUniqueDestinationFile(sourceFile, destinationFolder);
                        
                        if (sourceFile.isDirectory()) {
                            copyDirectoryRecursively(sourceFile, destFile);
                        } else {
                            copyFile(sourceFile, destFile);
                        }
                        successCount++;
                        
                    } catch (IOException e) {
                        notifyError(callback, "Failed to copy " + sourceFile.getName() + ": " + e.getMessage());
                    }
                }
                
                String message = "Copied " + successCount + " of " + files.size() + " items successfully";
                notifySuccess(callback, message);
                
            } catch (Exception e) {
                notifyError(callback, "Copy operation failed: " + e.getMessage());
            } finally {
                notifyComplete(callback);
            }
        });
    }
    
    /**
     * Move multiple files to a destination folder
     */
    public static void moveFiles(Context context, List<File> files, File destinationFolder, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int totalFiles = calculateTotalFileCount(files);
                int currentFile = 0;
                int successCount = 0;
                
                for (File sourceFile : files) {
                    try {
                        currentFile++;
                        notifyProgress(callback, currentFile, totalFiles, "Moving " + sourceFile.getName());
                        
                        File destFile = getUniqueDestinationFile(sourceFile, destinationFolder);
                        
                        // Try to move first, fallback to copy+delete
                        if (sourceFile.renameTo(destFile)) {
                            successCount++;
                        } else {
                            // Fallback: copy then delete
                            if (sourceFile.isDirectory()) {
                                copyDirectoryRecursively(sourceFile, destFile);
                                deleteRecursively(sourceFile);
                            } else {
                                copyFile(sourceFile, destFile);
                                sourceFile.delete();
                            }
                            successCount++;
                        }
                        
                    } catch (IOException e) {
                        notifyError(callback, "Failed to move " + sourceFile.getName() + ": " + e.getMessage());
                    }
                }
                
                String message = "Moved " + successCount + " of " + files.size() + " items successfully";
                notifySuccess(callback, message);
                
            } catch (Exception e) {
                notifyError(callback, "Move operation failed: " + e.getMessage());
            } finally {
                notifyComplete(callback);
            }
        });
    }
    
    /**
     * Delete multiple files
     */
    public static void deleteFiles(Context context, List<File> files, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int totalFiles = calculateTotalFileCount(files);
                int currentFile = 0;
                int successCount = 0;
                
                for (File file : files) {
                    try {
                        currentFile++;
                        notifyProgress(callback, currentFile, totalFiles, "Deleting " + file.getName());
                        
                        if (deleteRecursively(file)) {
                            successCount++;
                        } else {
                            notifyError(callback, "Failed to delete " + file.getName());
                        }
                        
                    } catch (Exception e) {
                        notifyError(callback, "Failed to delete " + file.getName() + ": " + e.getMessage());
                    }
                }
                
                String message = "Deleted " + successCount + " of " + files.size() + " items successfully";
                notifySuccess(callback, message);
                
            } catch (Exception e) {
                notifyError(callback, "Delete operation failed: " + e.getMessage());
            } finally {
                notifyComplete(callback);
            }
        });
    }
    
    /**
     * Create a ZIP file containing all selected files
     */
    public static void createZipFile(Context context, List<File> files, File destinationFolder, String zipFileName, ZipCallback callback) {
        executor.execute(() -> {
            try {
                // Ensure zip file has .zip extension
                final String finalZipFileName;
                if (!zipFileName.toLowerCase().endsWith(".zip")) {
                    finalZipFileName = zipFileName + ".zip";
                } else {
                    finalZipFileName = zipFileName;
                }
                
                File zipFile = new File(destinationFolder, finalZipFileName);
                
                // Handle duplicate zip file names
                int counter = 1;
                while (zipFile.exists()) {
                    String baseName = finalZipFileName.substring(0, finalZipFileName.lastIndexOf('.'));
                    zipFile = new File(destinationFolder, baseName + " (" + counter + ").zip");
                    counter++;
                }
                
                int totalFiles = calculateTotalFileCount(files);
                int currentFile = 0;
                
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                    zos.setLevel(6); // Good compression level
                    
                    for (File file : files) {
                        currentFile++;
                        notifyZipProgress(callback, currentFile, totalFiles, "Adding " + file.getName());
                        
                        if (file.isDirectory()) {
                            addDirectoryToZip(file, file.getName(), zos);
                        } else {
                            addFileToZip(file, file.getName(), zos);
                        }
                    }
                }
                
                notifyZipSuccess(callback, zipFile);
                
            } catch (Exception e) {
                notifyZipError(callback, "Failed to create ZIP file: " + e.getMessage());
            } finally {
                notifyZipComplete(callback);
            }
        });
    }
    
    /**
     * Calculate total number of files (including files in subdirectories)
     */
    private static int calculateTotalFileCount(List<File> files) {
        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFilesInDirectory(file);
            } else {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Count files in a directory recursively
     */
    private static int countFilesInDirectory(File directory) {
        int count = 1; // Count the directory itself
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFilesInDirectory(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Get a unique destination file name
     */
    private static File getUniqueDestinationFile(File sourceFile, File destinationFolder) {
        File destFile = new File(destinationFolder, sourceFile.getName());
        
        if (!destFile.exists()) {
            return destFile;
        }
        
        // Handle duplicate names
        String name = sourceFile.getName();
        String baseName = name;
        String extension = "";
        
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }
        
        int counter = 1;
        while (destFile.exists()) {
            destFile = new File(destinationFolder, baseName + " (" + counter + ")" + extension);
            counter++;
        }
        
        return destFile;
    }
    
    /**
     * Copy a single file
     */
    private static void copyFile(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.createNewFile();
        }
        
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }
    
    /**
     * Copy a directory recursively
     */
    private static void copyDirectoryRecursively(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyDirectoryRecursively(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }
    
    /**
     * Delete a file or directory recursively
     */
    private static boolean deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                }
            }
        }
        return fileOrDirectory.delete();
    }
    
    /**
     * Add a file to ZIP
     */
    private static void addFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zos.putNextEntry(zipEntry);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        
        zos.closeEntry();
    }
    
    /**
     * Add a directory to ZIP recursively
     */
    private static void addDirectoryToZip(File directory, String entryName, ZipOutputStream zos) throws IOException {
        // Add the directory entry
        ZipEntry zipEntry = new ZipEntry(entryName + "/");
        zos.putNextEntry(zipEntry);
        zos.closeEntry();
        
        // Add all files in the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String newEntryName = entryName + "/" + file.getName();
                if (file.isDirectory()) {
                    addDirectoryToZip(file, newEntryName, zos);
                } else {
                    addFileToZip(file, newEntryName, zos);
                }
            }
        }
    }
    
    /**
     * Check if there's enough space for the operation
     */
    public static boolean hasEnoughSpace(List<File> files, File destination) {
        long requiredSpace = calculateTotalSize(files);
        long availableSpace = StorageUtils.getAvailableSpace(destination);
        return requiredSpace <= availableSpace;
    }
    
    /**
     * Calculate total size of files
     */
    public static long calculateTotalSize(List<File> files) {
        long totalSize = 0;
        for (File file : files) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    totalSize += calculateDirectorySize(file);
                } else {
                    totalSize += file.length();
                }
            }
        }
        return totalSize;
    }
    
    /**
     * Calculate directory size recursively
     */
    private static long calculateDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }
    
    // Helper methods for callbacks
    private static void notifyProgress(OperationCallback callback, int current, int total, String currentFile) {
        if (callback != null) {
            mainHandler.post(() -> callback.onProgress(current, total, currentFile));
        }
    }
    
    private static void notifySuccess(OperationCallback callback, String message) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(message));
        }
    }
    
    private static void notifyError(OperationCallback callback, String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    private static void notifyComplete(OperationCallback callback) {
        if (callback != null) {
            mainHandler.post(callback::onComplete);
        }
    }
    
    private static void notifyZipProgress(ZipCallback callback, int current, int total, String currentFile) {
        if (callback != null) {
            mainHandler.post(() -> callback.onProgress(current, total, currentFile));
        }
    }
    
    private static void notifyZipSuccess(ZipCallback callback, File zipFile) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(zipFile));
        }
    }
    
    private static void notifyZipError(ZipCallback callback, String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    private static void notifyZipComplete(ZipCallback callback) {
        if (callback != null) {
            mainHandler.post(callback::onComplete);
        }
    }
} 