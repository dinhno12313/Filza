package dinhnguyen.filza.file.manager.examples;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.manager.BulkOperationsManager;

/**
 * Example demonstrating how to use bulk operations in the file manager
 * 
 * This example shows how to:
 * 1. Initialize the BulkOperationsManager
 * 2. Show the bulk operations menu
 * 3. Handle different bulk operations (copy, move, compress, delete)
 * 
 * Usage:
 * 1. In your activity, initialize the BulkOperationsManager
 * 2. When files are selected, call showBulkOperationsMenu()
 * 3. The manager will handle all the UI and operations automatically
 */
public class BulkOperationsExample {
    
    private final Context context;
    private final FragmentActivity activity;
    private final BulkOperationsManager bulkOperationsManager;
    
    public BulkOperationsExample(Context context, FragmentActivity activity) {
        this.context = context;
        this.activity = activity;
        this.bulkOperationsManager = new BulkOperationsManager(context, activity);
    }
    
    /**
     * Example: Add bulk operations to menu
     * Call this from your activity's onCreateOptionsMenu
     */
    public void addBulkOperationsToMenu(Menu menu, List<File> selectedFiles) {
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Add bulk operations menu item
            MenuItem bulkMenuItem = menu.add(Menu.NONE, R.id.action_bulk_operations, Menu.NONE, "Bulk Operations");
            bulkMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            bulkMenuItem.setIcon(R.drawable.ic_more_vert);
            
            // Add individual operation items
            menu.add(Menu.NONE, R.id.action_copy, Menu.NONE, "Copy (" + selectedFiles.size() + ")");
            menu.add(Menu.NONE, R.id.action_move, Menu.NONE, "Move (" + selectedFiles.size() + ")");
            menu.add(Menu.NONE, R.id.action_compress, Menu.NONE, "Compress (" + selectedFiles.size() + ")");
            menu.add(Menu.NONE, R.id.action_delete, Menu.NONE, "Delete (" + selectedFiles.size() + ")");
        }
    }
    
    /**
     * Example: Handle menu item clicks
     * Call this from your activity's onOptionsItemSelected
     */
    public boolean handleMenuClick(MenuItem item, List<File> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        int id = item.getItemId();
        if (id == R.id.action_bulk_operations) {
            showBulkOperationsMenu(selectedFiles);
            return true;
        } else if (id == R.id.action_copy) {
            performCopyOperation(selectedFiles);
            return true;
        } else if (id == R.id.action_move) {
            performMoveOperation(selectedFiles);
            return true;
        } else if (id == R.id.action_compress) {
            performCompressOperation(selectedFiles);
            return true;
        } else if (id == R.id.action_delete) {
            performDeleteOperation(selectedFiles);
            return true;
        }
        
        return false;
    }
    
    /**
     * Example: Show bulk operations menu
     */
    public void showBulkOperationsMenu(List<File> selectedFiles) {
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Perform copy operation
     */
    public void performCopyOperation(List<File> selectedFiles) {
        if (!bulkOperationsManager.canOperateOnFiles(selectedFiles)) {
            Toast.makeText(context, "Cannot operate on selected files", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String totalSize = bulkOperationsManager.getFormattedTotalSize(selectedFiles);
        Toast.makeText(context, "Copying " + selectedFiles.size() + " files (" + totalSize + ")", Toast.LENGTH_SHORT).show();
        
        // The actual operation is handled by BulkOperationsManager
        // This is just a wrapper for direct copy operation
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Perform move operation
     */
    public void performMoveOperation(List<File> selectedFiles) {
        if (!bulkOperationsManager.canOperateOnFiles(selectedFiles)) {
            Toast.makeText(context, "Cannot operate on selected files", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String totalSize = bulkOperationsManager.getFormattedTotalSize(selectedFiles);
        Toast.makeText(context, "Moving " + selectedFiles.size() + " files (" + totalSize + ")", Toast.LENGTH_SHORT).show();
        
        // The actual operation is handled by BulkOperationsManager
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Perform compress operation
     */
    public void performCompressOperation(List<File> selectedFiles) {
        if (!bulkOperationsManager.canOperateOnFiles(selectedFiles)) {
            Toast.makeText(context, "Cannot operate on selected files", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String totalSize = bulkOperationsManager.getFormattedTotalSize(selectedFiles);
        Toast.makeText(context, "Compressing " + selectedFiles.size() + " files (" + totalSize + ")", Toast.LENGTH_SHORT).show();
        
        // The actual operation is handled by BulkOperationsManager
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Perform delete operation
     */
    public void performDeleteOperation(List<File> selectedFiles) {
        if (!bulkOperationsManager.canOperateOnFiles(selectedFiles)) {
            Toast.makeText(context, "Cannot operate on selected files", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String totalSize = bulkOperationsManager.getFormattedTotalSize(selectedFiles);
        Toast.makeText(context, "Deleting " + selectedFiles.size() + " files (" + totalSize + ")", Toast.LENGTH_SHORT).show();
        
        // The actual operation is handled by BulkOperationsManager
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Add bulk operations to toolbar
     * Call this from your activity's onCreate
     */
    public void setupBulkOperationsToolbar() {
        // This would be called from your activity to set up the toolbar
        // with bulk operations functionality
    }
    
    /**
     * Example: Handle multi-select mode
     * Call this when entering/exiting multi-select mode
     */
    public void onMultiSelectModeChanged(boolean isMultiSelectMode, List<File> selectedFiles) {
        if (isMultiSelectMode && selectedFiles != null && !selectedFiles.isEmpty()) {
            // Show bulk operations UI
            showBulkOperationsInfo(selectedFiles);
        }
    }
    
    /**
     * Example: Show bulk operations info
     */
    private void showBulkOperationsInfo(List<File> selectedFiles) {
        String totalSize = bulkOperationsManager.getFormattedTotalSize(selectedFiles);
        String info = selectedFiles.size() + " items selected (" + totalSize + ")";
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Example: Refresh after bulk operations
     * Call this after bulk operations complete to refresh the file list
     */
    public void refreshAfterBulkOperation() {
        // This would typically call your activity's refresh method
        // For example: activity.refreshFileList();
        Toast.makeText(context, "File list refreshed", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Example: Get operation statistics
     */
    public String getOperationStatistics(List<File> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return "No files selected";
        }
        
        int fileCount = 0;
        int directoryCount = 0;
        long totalSize = 0;
        
        for (File file : selectedFiles) {
            if (file.isDirectory()) {
                directoryCount++;
            } else {
                fileCount++;
            }
            totalSize += file.length();
        }
        
        return String.format("%d files, %d folders, %s total", 
            fileCount, directoryCount, 
            android.text.format.Formatter.formatFileSize(context, totalSize));
    }
    
    /**
     * Example: Show bulk operations menu for selected files
     */
    public void showBulkOperationsForSelectedFiles(List<File> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // This will show a dialog with options: Copy, Move, Compress, Delete
        bulkOperationsManager.showBulkOperationsMenu(selectedFiles);
    }
    
    /**
     * Example: Create sample files for testing bulk operations
     */
    public List<File> createSampleFilesForTesting() {
        List<File> sampleFiles = new ArrayList<>();
        
        // Create some sample files in the app's external files directory
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            try {
                // Create sample text files
                for (int i = 1; i <= 3; i++) {
                    File sampleFile = new File(externalDir, "sample_file_" + i + ".txt");
                    if (!sampleFile.exists()) {
                        sampleFile.createNewFile();
                    }
                    sampleFiles.add(sampleFile);
                }
                
                // Create sample directories
                for (int i = 1; i <= 2; i++) {
                    File sampleDir = new File(externalDir, "sample_folder_" + i);
                    if (!sampleDir.exists()) {
                        sampleDir.mkdirs();
                    }
                    sampleFiles.add(sampleDir);
                }
                
                Toast.makeText(context, "Created " + sampleFiles.size() + " sample files", Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(context, "Error creating sample files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        return sampleFiles;
    }
    
    /**
     * Example: Demonstrate different bulk operations
     */
    public void demonstrateBulkOperations() {
        // Step 1: Create sample files
        List<File> sampleFiles = createSampleFilesForTesting();
        
        // Step 2: Show bulk operations menu
        if (!sampleFiles.isEmpty()) {
            showBulkOperationsForSelectedFiles(sampleFiles);
        }
    }
    
    /**
     * Example: Check if files can be operated on
     */
    public boolean canOperateOnFiles(List<File> files) {
        return bulkOperationsManager.canOperateOnFiles(files);
    }
    
    /**
     * Example: Get formatted total size of files
     */
    public String getFormattedTotalSize(List<File> files) {
        return bulkOperationsManager.getFormattedTotalSize(files);
    }
    
    /**
     * Example: Show file information before bulk operations
     */
    public void showFileInformation(List<File> files) {
        if (files == null || files.isEmpty()) {
            Toast.makeText(context, "No files to show information for", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Selected ").append(files.size()).append(" item(s)\n");
        info.append("Total size: ").append(getFormattedTotalSize(files)).append("\n");
        info.append("Can operate: ").append(canOperateOnFiles(files) ? "Yes" : "No");
        
        Toast.makeText(context, info.toString(), Toast.LENGTH_LONG).show();
    }
} 