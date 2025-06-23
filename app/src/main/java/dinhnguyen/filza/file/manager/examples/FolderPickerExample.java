package dinhnguyen.filza.file.manager.examples;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dinhnguyen.filza.file.manager.manager.FolderPickerManager;
import dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog;
import dinhnguyen.filza.file.manager.utils.StorageUtils;

/**
 * Comprehensive example showing how to use the FolderPickerDialog
 * This class demonstrates various use cases for the folder picker
 */
public class FolderPickerExample {
    
    private final Context context;
    private final FragmentActivity activity;
    private final FolderPickerManager folderPickerManager;
    
    public FolderPickerExample(Context context, FragmentActivity activity) {
        this.context = context;
        this.activity = activity;
        this.folderPickerManager = new FolderPickerManager(context, activity);
    }
    
    /**
     * Example 1: Basic folder picker usage
     */
    public void showBasicFolderPicker() {
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(
            StorageUtils.getSafeInitialDirectory(context),
            "Select Destination Folder",
            selectedFolder -> {
                if (selectedFolder != null) {
                    Toast.makeText(context, 
                        "Selected: " + selectedFolder.getAbsolutePath(), 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
        dialog.show(activity.getSupportFragmentManager(), "BasicFolderPicker");
    }
    
    /**
     * Example 2: Copy files with folder picker
     */
    public void copyFilesWithFolderPicker(List<File> filesToCopy) {
        folderPickerManager.showFolderPickerForCopy(filesToCopy);
    }
    
    /**
     * Example 3: Move files with folder picker
     */
    public void moveFilesWithFolderPicker(List<File> filesToMove) {
        folderPickerManager.showFolderPickerForMove(filesToMove);
    }
    
    /**
     * Example 4: Create new folder with folder picker
     */
    public void createNewFolderWithPicker() {
        folderPickerManager.showFolderPickerForNewFolder();
    }
    
    /**
     * Example 5: Custom folder picker with specific start directory
     */
    public void showCustomFolderPicker(File startDirectory, String title) {
        folderPickerManager.showFolderPickerFromDirectory(
            startDirectory, 
            title, 
            selectedFolder -> {
                if (selectedFolder != null) {
                    handleCustomFolderSelection(selectedFolder);
                }
            }
        );
    }
    
    /**
     * Example 6: Folder picker with validation
     */
    public void showFolderPickerWithValidation() {
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(
            StorageUtils.getSafeInitialDirectory(context),
            "Select Writable Folder",
            selectedFolder -> {
                if (selectedFolder != null) {
                    if (StorageUtils.isDirectoryWritable(selectedFolder)) {
                        long availableSpace = StorageUtils.getAvailableSpace(selectedFolder);
                        String spaceInfo = StorageUtils.formatFileSize(availableSpace);
                        Toast.makeText(context, 
                            "Selected: " + selectedFolder.getName() + 
                            "\nAvailable space: " + spaceInfo, 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, 
                            "Selected folder is not writable", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        dialog.show(activity.getSupportFragmentManager(), "ValidationFolderPicker");
    }
    
    /**
     * Example 7: Multiple file operations with folder picker
     */
    public void performMultipleOperations() {
        // Create sample files for demonstration
        List<File> sampleFiles = createSampleFiles();
        
        // Show options dialog
        String[] options = {"Copy Files", "Move Files", "Create Folder", "Basic Picker"};
        
        new android.app.AlertDialog.Builder(context)
            .setTitle("Choose Operation")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        copyFilesWithFolderPicker(sampleFiles);
                        break;
                    case 1:
                        moveFilesWithFolderPicker(sampleFiles);
                        break;
                    case 2:
                        createNewFolderWithPicker();
                        break;
                    case 3:
                        showBasicFolderPicker();
                        break;
                }
            })
            .show();
    }
    
    /**
     * Example 8: Folder picker with storage information
     */
    public void showFolderPickerWithStorageInfo() {
        List<File> storages = folderPickerManager.getAvailableStorages();
        
        if (storages.isEmpty()) {
            Toast.makeText(context, "No storage available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show storage selection first
        String[] storageNames = new String[storages.size()];
        for (int i = 0; i < storages.size(); i++) {
            File storage = storages.get(i);
            long totalSpace = StorageUtils.getTotalSpace(storage);
            long availableSpace = StorageUtils.getAvailableSpace(storage);
            storageNames[i] = storage.getName() + " (" + 
                StorageUtils.formatFileSize(availableSpace) + " free of " + 
                StorageUtils.formatFileSize(totalSpace) + ")";
        }
        
        new android.app.AlertDialog.Builder(context)
            .setTitle("Select Storage")
            .setItems(storageNames, (dialog, which) -> {
                File selectedStorage = storages.get(which);
                showCustomFolderPicker(selectedStorage, "Browse " + selectedStorage.getName());
            })
            .show();
    }
    
    /**
     * Example 9: Advanced folder picker with custom callback
     */
    public void showAdvancedFolderPicker() {
        FolderPickerDialog.FolderSelectCallback callback = new FolderPickerDialog.FolderSelectCallback() {
            @Override
            public void onFolderSelected(File folder) {
                if (folder != null) {
                    // Perform advanced validation
                    if (validateFolderForOperation(folder)) {
                        performAdvancedOperation(folder);
                    } else {
                        Toast.makeText(context, 
                            "Folder validation failed", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(
            StorageUtils.getSafeInitialDirectory(context),
            "Advanced Folder Selection",
            callback
        );
        dialog.show(activity.getSupportFragmentManager(), "AdvancedFolderPicker");
    }
    
    /**
     * Example 10: Batch operations with folder picker
     */
    public void performBatchOperations() {
        // Simulate batch file operations
        List<File> batchFiles = createBatchFiles();
        
        new android.app.AlertDialog.Builder(context)
            .setTitle("Batch Operations")
            .setMessage("Select operation for " + batchFiles.size() + " files")
            .setPositiveButton("Copy", (dialog, which) -> {
                copyFilesWithFolderPicker(batchFiles);
            })
            .setNegativeButton("Move", (dialog, which) -> {
                moveFilesWithFolderPicker(batchFiles);
            })
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    // Helper methods
    
    private void handleCustomFolderSelection(File selectedFolder) {
        Toast.makeText(context, 
            "Custom selection: " + selectedFolder.getAbsolutePath(), 
            Toast.LENGTH_SHORT).show();
    }
    
    private boolean validateFolderForOperation(File folder) {
        return folder != null && 
               folder.exists() && 
               folder.isDirectory() && 
               folder.canRead() && 
               folder.canWrite();
    }
    
    private void performAdvancedOperation(File folder) {
        // Implement your advanced operation here
        Toast.makeText(context, 
            "Advanced operation performed on: " + folder.getName(), 
            Toast.LENGTH_SHORT).show();
    }
    
    private List<File> createSampleFiles() {
        List<File> files = new ArrayList<>();
        File tempDir = context.getCacheDir();
        
        // Create some sample files for demonstration
        for (int i = 1; i <= 3; i++) {
            File sampleFile = new File(tempDir, "sample_file_" + i + ".txt");
            files.add(sampleFile);
        }
        
        return files;
    }
    
    private List<File> createBatchFiles() {
        List<File> files = new ArrayList<>();
        File tempDir = context.getCacheDir();
        
        // Create more sample files for batch operations
        for (int i = 1; i <= 10; i++) {
            File batchFile = new File(tempDir, "batch_file_" + i + ".txt");
            files.add(batchFile);
        }
        
        return files;
    }
    
    /**
     * Utility method to get storage information
     */
    public String getStorageInfo() {
        List<File> storages = folderPickerManager.getAvailableStorages();
        StringBuilder info = new StringBuilder("Available Storages:\n");
        
        for (File storage : storages) {
            long totalSpace = StorageUtils.getTotalSpace(storage);
            long availableSpace = StorageUtils.getAvailableSpace(storage);
            boolean isWritable = StorageUtils.isDirectoryWritable(storage);
            
            info.append(storage.getName())
                .append(": ")
                .append(StorageUtils.formatFileSize(availableSpace))
                .append(" free of ")
                .append(StorageUtils.formatFileSize(totalSpace))
                .append(" (")
                .append(isWritable ? "Writable" : "Read-only")
                .append(")\n");
        }
        
        return info.toString();
    }
} 