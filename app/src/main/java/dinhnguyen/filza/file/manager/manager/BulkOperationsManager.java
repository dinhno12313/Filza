package dinhnguyen.filza.file.manager.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog;
import dinhnguyen.filza.file.manager.utils.FileUtils;
import dinhnguyen.filza.file.manager.utils.StorageUtils;

/**
 * Manager class for handling bulk file operations
 * Provides UI for copy, move, delete, and compress operations
 */
public class BulkOperationsManager {
    
    private final Context context;
    private final FragmentActivity activity;
    private AlertDialog progressDialog;
    private TextView textViewTitle;
    private TextView textViewCurrentFile;
    private ProgressBar progressBar;
    private TextView textViewProgress;
    private TextView textViewPercentage;
    
    public BulkOperationsManager(Context context, FragmentActivity activity) {
        this.context = context;
        this.activity = activity;
    }
    
    /**
     * Show bulk operations menu
     */
    public void showBulkOperationsMenu(List<File> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] operations = {"Copy Files", "Move Files", "Compress to ZIP", "Delete Files"};
        
        new AlertDialog.Builder(context)
            .setTitle("Bulk Operations (" + selectedFiles.size() + " items)")
            .setItems(operations, (dialog, which) -> {
                switch (which) {
                    case 0:
                        copyFiles(selectedFiles);
                        break;
                    case 1:
                        moveFiles(selectedFiles);
                        break;
                    case 2:
                        compressFiles(selectedFiles);
                        break;
                    case 3:
                        deleteFiles(selectedFiles);
                        break;
                }
            })
            .show();
    }
    
    /**
     * Copy selected files
     */
    private void copyFiles(List<File> selectedFiles) {
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(
            StorageUtils.getSafeInitialDirectory(context),
            "Select Destination for Copy",
            destinationFolder -> {
                if (destinationFolder != null) {
                    if (StorageUtils.isDirectoryWritable(destinationFolder)) {
                        if (FileUtils.hasEnoughSpace(selectedFiles, destinationFolder)) {
                            performCopyOperation(selectedFiles, destinationFolder);
                        } else {
                            Toast.makeText(context, "Not enough space available", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        dialog.show(activity.getSupportFragmentManager(), "CopyDestinationPicker");
    }
    
    /**
     * Move selected files
     */
    private void moveFiles(List<File> selectedFiles) {
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(
            StorageUtils.getSafeInitialDirectory(context),
            "Select Destination for Move",
            destinationFolder -> {
                if (destinationFolder != null) {
                    if (StorageUtils.isDirectoryWritable(destinationFolder)) {
                        if (FileUtils.hasEnoughSpace(selectedFiles, destinationFolder)) {
                            performMoveOperation(selectedFiles, destinationFolder);
                        } else {
                            Toast.makeText(context, "Not enough space available", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        dialog.show(activity.getSupportFragmentManager(), "MoveDestinationPicker");
    }
    
    /**
     * Compress selected files to ZIP
     */
    private void compressFiles(List<File> selectedFiles) {
        // First, show dialog to enter ZIP file name
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_zip_name, null);
        TextInputEditText editTextZipName = dialogView.findViewById(R.id.editTextZipName);
        
        // Set default name based on first file or "archive"
        String defaultName = "archive";
        if (!selectedFiles.isEmpty()) {
            File firstFile = selectedFiles.get(0);
            if (selectedFiles.size() == 1) {
                defaultName = firstFile.getName();
                if (defaultName.contains(".")) {
                    defaultName = defaultName.substring(0, defaultName.lastIndexOf('.'));
                }
            } else {
                defaultName = "archive";
            }
        }
        editTextZipName.setText(defaultName);
        
        new AlertDialog.Builder(context)
            .setTitle("Compress Files")
            .setView(dialogView)
            .setPositiveButton("Next", (dialog, which) -> {
                String zipName = editTextZipName.getText().toString().trim();
                if (zipName.isEmpty()) {
                    Toast.makeText(context, "Please enter a ZIP file name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Now show folder picker for destination
                FolderPickerDialog folderDialog = FolderPickerDialog.newInstance(
                    StorageUtils.getSafeInitialDirectory(context),
                    "Select ZIP Destination",
                    destinationFolder -> {
                        if (destinationFolder != null) {
                            if (StorageUtils.isDirectoryWritable(destinationFolder)) {
                                performCompressOperation(selectedFiles, destinationFolder, zipName);
                            } else {
                                Toast.makeText(context, "Selected folder is not writable", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                );
                folderDialog.show(activity.getSupportFragmentManager(), "ZipDestinationPicker");
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Delete selected files
     */
    private void deleteFiles(List<File> selectedFiles) {
        // Show confirmation dialog
        String message = "Are you sure you want to delete " + selectedFiles.size() + " item(s)?\n\nThis action cannot be undone.";
        
        new AlertDialog.Builder(context)
            .setTitle("Confirm Delete")
            .setMessage(message)
            .setPositiveButton("Delete", (dialog, which) -> {
                performDeleteOperation(selectedFiles);
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    /**
     * Perform copy operation with progress dialog
     */
    private void performCopyOperation(List<File> files, File destinationFolder) {
        showProgressDialog("Copying Files");
        
        FileUtils.copyFiles(context, files, destinationFolder, new FileUtils.OperationCallback() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                updateProgress(current, total, currentFile);
            }
            
            @Override
            public void onSuccess(String message) {
                hideProgressDialog();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onComplete() {
                // Operation completed
            }
        });
    }
    
    /**
     * Perform move operation with progress dialog
     */
    private void performMoveOperation(List<File> files, File destinationFolder) {
        showProgressDialog("Moving Files");
        
        FileUtils.moveFiles(context, files, destinationFolder, new FileUtils.OperationCallback() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                updateProgress(current, total, currentFile);
            }
            
            @Override
            public void onSuccess(String message) {
                hideProgressDialog();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onComplete() {
                // Operation completed
            }
        });
    }
    
    /**
     * Perform compress operation with progress dialog
     */
    private void performCompressOperation(List<File> files, File destinationFolder, String zipFileName) {
        showProgressDialog("Creating ZIP File");
        
        FileUtils.createZipFile(context, files, destinationFolder, zipFileName, new FileUtils.ZipCallback() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                updateProgress(current, total, currentFile);
            }
            
            @Override
            public void onSuccess(File zipFile) {
                hideProgressDialog();
                String message = "ZIP file created successfully: " + zipFile.getName();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onComplete() {
                // Operation completed
            }
        });
    }
    
    /**
     * Perform delete operation with progress dialog
     */
    private void performDeleteOperation(List<File> files) {
        showProgressDialog("Deleting Files");
        
        FileUtils.deleteFiles(context, files, new FileUtils.OperationCallback() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                updateProgress(current, total, currentFile);
            }
            
            @Override
            public void onSuccess(String message) {
                hideProgressDialog();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onComplete() {
                // Operation completed
            }
        });
    }
    
    /**
     * Show progress dialog
     */
    private void showProgressDialog(String title) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        
        textViewTitle = dialogView.findViewById(R.id.textViewTitle);
        textViewCurrentFile = dialogView.findViewById(R.id.textViewCurrentFile);
        progressBar = dialogView.findViewById(R.id.progressBar);
        textViewProgress = dialogView.findViewById(R.id.textViewProgress);
        textViewPercentage = dialogView.findViewById(R.id.textViewPercentage);
        
        textViewTitle.setText(title);
        
        progressDialog = new AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        progressDialog.show();
    }
    
    /**
     * Update progress dialog
     */
    private void updateProgress(int current, int total, String currentFile) {
        if (progressDialog != null && progressDialog.isShowing()) {
            activity.runOnUiThread(() -> {
                textViewCurrentFile.setText(currentFile);
                progressBar.setMax(total);
                progressBar.setProgress(current);
                textViewProgress.setText(current + "/" + total);
                
                int percentage = total > 0 ? (current * 100) / total : 0;
                textViewPercentage.setText(percentage + "%");
            });
        }
    }
    
    /**
     * Hide progress dialog
     */
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    /**
     * Get formatted file size for display
     */
    public String getFormattedTotalSize(List<File> files) {
        long totalSize = FileUtils.calculateTotalSize(files);
        return StorageUtils.formatFileSize(totalSize);
    }
    
    /**
     * Check if files can be operated on
     */
    public boolean canOperateOnFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        
        for (File file : files) {
            if (!file.exists()) {
                return false;
            }
        }
        
        return true;
    }
} 