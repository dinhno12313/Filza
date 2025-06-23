package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog;
import dinhnguyen.filza.file.manager.utils.StorageUtils;

/**
 * Manager class for handling folder picker operations
 * Provides convenient methods to show the folder picker dialog
 * and handle the selected destination folder
 */
public class FolderPickerManager {
    
    private final Context context;
    private final FragmentActivity activity;
    
    public FolderPickerManager(Context context, FragmentActivity activity) {
        this.context = context;
        this.activity = activity;
    }
    
    /**
     * Show folder picker for copying files
     */
    public void showFolderPickerForCopy(List<File> filesToCopy) {
        showFolderPicker("Select Destination for Copy", selectedFolder -> {
            if (selectedFolder != null && StorageUtils.isDirectoryWritable(selectedFolder)) {
                copyFilesToDestination(filesToCopy, selectedFolder);
            } else {
                Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show folder picker for moving files
     */
    public void showFolderPickerForMove(List<File> filesToMove) {
        showFolderPicker("Select Destination for Move", selectedFolder -> {
            if (selectedFolder != null && StorageUtils.isDirectoryWritable(selectedFolder)) {
                moveFilesToDestination(filesToMove, selectedFolder);
            } else {
                Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show folder picker for creating a new folder
     */
    public void showFolderPickerForNewFolder() {
        showFolderPicker("Select Parent Folder", selectedFolder -> {
            if (selectedFolder != null && StorageUtils.isDirectoryWritable(selectedFolder)) {
                createNewFolder(selectedFolder);
            } else {
                Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show folder picker with custom title and callback
     */
    public void showFolderPicker(String title, FolderPickerDialog.FolderSelectCallback callback) {
        File initialFolder = StorageUtils.getSafeInitialDirectory(context);
        
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(initialFolder, title, callback);
        dialog.show(activity.getSupportFragmentManager(), "FolderPickerDialog");
    }
    
    /**
     * Show folder picker starting from a specific directory
     */
    public void showFolderPickerFromDirectory(File startDirectory, String title, 
                                            FolderPickerDialog.FolderSelectCallback callback) {
        if (startDirectory == null || !startDirectory.exists() || !startDirectory.canRead()) {
            startDirectory = StorageUtils.getSafeInitialDirectory(context);
        }
        
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(startDirectory, title, callback);
        dialog.show(activity.getSupportFragmentManager(), "FolderPickerDialog");
    }
    
    /**
     * Copy files to the selected destination
     */
    private void copyFilesToDestination(List<File> files, File destinationFolder) {
        // Check available space
        long totalSize = calculateTotalSize(files);
        long availableSpace = StorageUtils.getAvailableSpace(destinationFolder);
        
        if (totalSize > availableSpace) {
            Toast.makeText(context, "Not enough space available", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Perform copy operation
        int successCount = 0;
        int totalCount = files.size();
        
        for (File sourceFile : files) {
            try {
                File destFile = new File(destinationFolder, sourceFile.getName());
                
                // Handle duplicate names
                int counter = 1;
                while (destFile.exists()) {
                    String name = sourceFile.getName();
                    String baseName = name;
                    String extension = "";
                    
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0) {
                        baseName = name.substring(0, dotIndex);
                        extension = name.substring(dotIndex);
                    }
                    
                    destFile = new File(destinationFolder, baseName + " (" + counter + ")" + extension);
                    counter++;
                }
                
                if (sourceFile.isDirectory()) {
                    copyDirectory(sourceFile, destFile);
                } else {
                    copyFile(sourceFile, destFile);
                }
                successCount++;
                
            } catch (IOException e) {
                Toast.makeText(context, "Failed to copy " + sourceFile.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        String message = "Copied " + successCount + " of " + totalCount + " files successfully";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Move files to the selected destination
     */
    private void moveFilesToDestination(List<File> files, File destinationFolder) {
        // Check available space
        long totalSize = calculateTotalSize(files);
        long availableSpace = StorageUtils.getAvailableSpace(destinationFolder);
        
        if (totalSize > availableSpace) {
            Toast.makeText(context, "Not enough space available", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Perform move operation
        int successCount = 0;
        int totalCount = files.size();
        
        for (File sourceFile : files) {
            try {
                File destFile = new File(destinationFolder, sourceFile.getName());
                
                // Handle duplicate names
                int counter = 1;
                while (destFile.exists()) {
                    String name = sourceFile.getName();
                    String baseName = name;
                    String extension = "";
                    
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0) {
                        baseName = name.substring(0, dotIndex);
                        extension = name.substring(dotIndex);
                    }
                    
                    destFile = new File(destinationFolder, baseName + " (" + counter + ")" + extension);
                    counter++;
                }
                
                // Try to move first, fallback to copy+delete
                if (sourceFile.renameTo(destFile)) {
                    successCount++;
                } else {
                    // Fallback: copy then delete
                    if (sourceFile.isDirectory()) {
                        copyDirectory(sourceFile, destFile);
                        deleteRecursively(sourceFile);
                    } else {
                        copyFile(sourceFile, destFile);
                        sourceFile.delete();
                    }
                    successCount++;
                }
                
            } catch (IOException e) {
                Toast.makeText(context, "Failed to move " + sourceFile.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        String message = "Moved " + successCount + " of " + totalCount + " files successfully";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Create a new folder in the selected destination
     */
    private void createNewFolder(File parentFolder) {
        // This would typically show a dialog to enter the folder name
        // For now, we'll create a folder with a default name
        String folderName = "New Folder";
        File newFolder = new File(parentFolder, folderName);
        
        // Find a unique name if the folder already exists
        int counter = 1;
        while (newFolder.exists()) {
            newFolder = new File(parentFolder, folderName + " (" + counter + ")");
            counter++;
        }
        
        if (StorageUtils.createDirectoryIfNotExists(newFolder)) {
            Toast.makeText(context, "Folder created: " + newFolder.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Calculate total size of files
     */
    private long calculateTotalSize(List<File> files) {
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
    private long calculateDirectorySize(File directory) {
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
    
    /**
     * Copy a single file
     */
    private void copyFile(File source, File dest) throws IOException {
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
    private void copyDirectory(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }
    
    /**
     * Delete a file or directory recursively
     */
    private boolean deleteRecursively(File fileOrDirectory) {
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
     * Get available storage locations
     */
    public List<File> getAvailableStorages() {
        return StorageUtils.getAvailableStorages(context);
    }
    
    /**
     * Check if a directory is writable
     */
    public boolean isDirectoryWritable(File directory) {
        return StorageUtils.isDirectoryWritable(directory);
    }
    
    /**
     * Get available space in a directory
     */
    public long getAvailableSpace(File directory) {
        return StorageUtils.getAvailableSpace(directory);
    }
    
    /**
     * Format file size for display
     */
    public String formatFileSize(long size) {
        return StorageUtils.formatFileSize(size);
    }
} 