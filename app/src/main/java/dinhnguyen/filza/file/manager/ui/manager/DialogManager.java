package dinhnguyen.filza.file.manager.ui.manager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.constants.FileConstants;

public class DialogManager {
    
    private final Context context;
    
    public DialogManager(Context context) {
        this.context = context;
    }
    
    public void showCreateFolderDialog(OnFolderCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(FileConstants.DIALOG_CREATE_FOLDER_TITLE);
        builder.setView(R.layout.dialog_create_folder);
        
        builder.setPositiveButton(FileConstants.DIALOG_BUTTON_CREATE, (dialog, which) -> {
            AlertDialog alert = (AlertDialog) dialog;
            TextView input = alert.findViewById(R.id.inputFolderName);
            if (input != null) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    listener.onFolderCreated(name);
                }
            }
        });
        
        builder.setNegativeButton(FileConstants.DIALOG_BUTTON_CANCEL, null);
        builder.show();
    }
    
    public interface OnFolderCreatedListener {
        void onFolderCreated(String folderName);
    }
} 