package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dinhnguyen.filza.file.manager.constants.FileConstants;
import dinhnguyen.filza.file.manager.utils.PermissionManager;

public class DirectoryManager {
    
    private final Context context;
    
    public DirectoryManager(Context context) {
        this.context = context;
    }
    
    public List<File> getFilesInDirectory(File directory) {
        // Check permissions first
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            showToast("File access permission required");
            return new ArrayList<>();
        }
        
        // Check if external storage is available
        if (!PermissionManager.isExternalStorageAvailable()) {
            showToast("External storage not available");
            return new ArrayList<>();
        }
        
        // Check if we can access this directory
        if (!PermissionManager.canAccessDirectory(context, directory.getAbsolutePath())) {
            showToast("Cannot access this directory");
            return new ArrayList<>();
        }
        
        File[] files = directory.listFiles();
        List<File> fileList = new ArrayList<>();
        
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });
            fileList.addAll(Arrays.asList(files));
        }
        
        return fileList;
    }
    
    public boolean createFolder(File parentDirectory, String folderName) {
        // Check permissions first
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            showToast("File access permission required");
            return false;
        }
        
        if (folderName == null || folderName.trim().isEmpty()) {
            showToast(FileConstants.ERROR_EMPTY_FOLDER_NAME);
            return false;
        }
        
        // Check if we can access the parent directory
        if (!PermissionManager.canAccessDirectory(context, parentDirectory.getAbsolutePath())) {
            showToast("Cannot access parent directory");
            return false;
        }
        
        File newDir = new File(parentDirectory, folderName.trim());
        if (newDir.exists()) {
            showToast(FileConstants.ERROR_FOLDER_EXISTS);
            return false;
        }
        
        boolean success = newDir.mkdir();
        if (!success) {
            showToast(FileConstants.ERROR_CREATE_FOLDER);
        }
        
        return success;
    }
    
    public File getParentDirectory(File currentDirectory) {
        // Check permissions first
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            return null;
        }
        
        File parent = currentDirectory.getParentFile();
        if (parent == null || !parent.canRead()) {
            return null;
        }
        
        // Check if we can access the parent directory
        if (!PermissionManager.canAccessDirectory(context, parent.getAbsolutePath())) {
            return null;
        }
        
        return parent;
    }
    
    public boolean canNavigateToParent(File currentDirectory) {
        File parent = getParentDirectory(currentDirectory);
        return parent != null;
    }
    
    public boolean deleteFile(File file) {
        // Check permissions first
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            showToast("File access permission required");
            return false;
        }
        
        if (!file.exists()) {
            showToast("File does not exist");
            return false;
        }
        
        // Check if we can access the file's directory
        File parentDir = file.getParentFile();
        if (parentDir != null && !PermissionManager.canAccessDirectory(context, parentDir.getAbsolutePath())) {
            showToast("Cannot access file directory");
            return false;
        }
        
        boolean success = file.delete();
        if (!success) {
            showToast("Failed to delete file");
        }
        
        return success;
    }
    
    public boolean renameFile(File file, String newName) {
        // Check permissions first
        if (!PermissionManager.hasFileAccessPermissions(context)) {
            showToast("File access permission required");
            return false;
        }
        
        if (!file.exists()) {
            showToast("File does not exist");
            return false;
        }
        
        if (newName == null || newName.trim().isEmpty()) {
            showToast("Invalid file name");
            return false;
        }
        
        // Check if we can access the file's directory
        File parentDir = file.getParentFile();
        if (parentDir != null && !PermissionManager.canAccessDirectory(context, parentDir.getAbsolutePath())) {
            showToast("Cannot access file directory");
            return false;
        }
        
        File newFile = new File(parentDir, newName.trim());
        if (newFile.exists()) {
            showToast("File with this name already exists");
            return false;
        }
        
        boolean success = file.renameTo(newFile);
        if (!success) {
            showToast("Failed to rename file");
        }
        
        return success;
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 