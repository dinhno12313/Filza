package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dinhnguyen.filza.file.manager.constants.FileConstants;

public class DirectoryManager {
    
    private final Context context;
    
    public DirectoryManager(Context context) {
        this.context = context;
    }
    
    public List<File> getFilesInDirectory(File directory) {
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
        if (folderName == null || folderName.trim().isEmpty()) {
            showToast(FileConstants.ERROR_EMPTY_FOLDER_NAME);
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
        File parent = currentDirectory.getParentFile();
        return (parent != null && parent.canRead()) ? parent : null;
    }
    
    public boolean canNavigateToParent(File currentDirectory) {
        File parent = getParentDirectory(currentDirectory);
        return parent != null;
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 