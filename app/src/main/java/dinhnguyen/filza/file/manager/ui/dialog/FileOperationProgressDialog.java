package dinhnguyen.filza.file.manager.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.File;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.manager.FileOperationManager;

/**
 * Dialog to show progress during file operations
 */
public class FileOperationProgressDialog {
    
    private final Context context;
    private final AlertDialog dialog;
    private final TextView tvOperationTitle;
    private final TextView tvFileName;
    private final TextView tvProgressText;
    private final TextView tvFileSize;
    private final ProgressBar progressBar;
    
    public FileOperationProgressDialog(Context context, String operation, File sourceFile) {
        this.context = context;
        
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_operation_progress, null);
        
        tvOperationTitle = dialogView.findViewById(R.id.tvOperationTitle);
        tvFileName = dialogView.findViewById(R.id.tvFileName);
        tvProgressText = dialogView.findViewById(R.id.tvProgressText);
        tvFileSize = dialogView.findViewById(R.id.tvFileSize);
        progressBar = dialogView.findViewById(R.id.progressBar);
        
        // Set initial values
        tvOperationTitle.setText(operation);
        tvFileName.setText(sourceFile.getName());
        progressBar.setProgress(0);
        tvProgressText.setText("0%");
        
        // Create non-cancelable dialog
        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();
    }
    
    public void show() {
        dialog.show();
    }
    
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public void updateProgress(int progress) {
        progressBar.setProgress(progress);
        tvProgressText.setText(progress + "%");
    }
    
    public void updateFileSize(long copiedBytes, long totalBytes) {
        String copiedStr = FileOperationManager.getFileSizeString(copiedBytes);
        String totalStr = FileOperationManager.getFileSizeString(totalBytes);
        tvFileSize.setText(copiedStr + " / " + totalStr);
    }
    
    public void setOperationTitle(String title) {
        tvOperationTitle.setText(title);
    }
    
    public void setFileName(String fileName) {
        tvFileName.setText(fileName);
    }
} 