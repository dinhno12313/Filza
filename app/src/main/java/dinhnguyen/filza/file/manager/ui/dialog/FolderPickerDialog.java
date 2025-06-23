package dinhnguyen.filza.file.manager.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.utils.PermissionManager;

/**
 * Dialog for picking a destination folder for copy/move operations
 */
public class FolderPickerDialog {
    
    private final Context context;
    private final FolderPickerCallback callback;
    private AlertDialog dialog;
    private File currentDirectory;
    private RecyclerView recyclerView;
    private TextView tvCurrentPath;
    private FolderAdapter adapter;
    private View dialogView;
    private File rootDirectory;
    
    public interface FolderPickerCallback {
        void onFolderSelected(File selectedFolder);
        void onCancelled();
    }
    
    public FolderPickerDialog(Context context, File initialDirectory, FolderPickerCallback callback) {
        this.context = context;
        this.currentDirectory = initialDirectory;
        this.rootDirectory = initialDirectory;
        this.callback = callback;
    }
    
    public void show() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_folder_picker, null);
        recyclerView = dialogView.findViewById(R.id.recyclerViewFolders);
        tvCurrentPath = dialogView.findViewById(R.id.tvCurrentPath);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new FolderAdapter(new ArrayList<>(), this::onFolderClicked, this::onFolderSelected);
        recyclerView.setAdapter(adapter);
        
        updateFolderList();
        
        dialog = new AlertDialog.Builder(context)
                .setTitle("Select Destination Folder")
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    if (callback != null) {
                        callback.onCancelled();
                    }
                })
                .setPositiveButton("Select", (dialogInterface, i) -> {
                    if (callback != null) {
                        callback.onFolderSelected(currentDirectory);
                    }
                })
                .setCancelable(true)
                .create();
        
        dialog.show();
    }
    
    private void updateFolderList() {
        tvCurrentPath.setText("Current: " + currentDirectory.getAbsolutePath());
        List<File> folders = getSubFolders(currentDirectory);
        adapter.setFolders(folders);
    }
    
    private List<File> getSubFolders(File directory) {
        List<File> folders = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.canRead()) {
                    folders.add(file);
                }
            }
        }
        // Nếu không phải root, thêm option ".." để quay lại cha
        if (!directory.equals(rootDirectory) && directory.getParentFile() != null) {
            folders.add(0, new File(".."));
        }
        return folders;
    }
    
    private void onFolderClicked(File folder) {
        if (folder.getName().equals("..")) {
            // Quay lại thư mục cha
            File parent = currentDirectory.getParentFile();
            if (parent != null && parent.canRead()) {
                currentDirectory = parent;
                updateFolderList();
            }
        } else {
            currentDirectory = folder;
            updateFolderList();
        }
    }
    
    private void onFolderSelected(File folder) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (callback != null) {
            callback.onFolderSelected(folder);
        }
    }
    
    /**
     * Adapter for displaying folders in the RecyclerView
     */
    private static class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
        
        private List<File> folders;
        private final OnFolderClickListener clickListener;
        private final OnFolderSelectListener selectListener;
        
        public interface OnFolderClickListener {
            void onFolderClick(File folder);
        }
        
        public interface OnFolderSelectListener {
            void onFolderSelect(File folder);
        }
        
        public FolderAdapter(List<File> folders, OnFolderClickListener clickListener, OnFolderSelectListener selectListener) {
            this.folders = folders;
            this.clickListener = clickListener;
            this.selectListener = selectListener;
        }
        
        public void setFolders(List<File> folders) {
            this.folders = folders;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new FolderViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
            File folder = folders.get(position);
            holder.bind(folder);
        }
        
        @Override
        public int getItemCount() {
            return folders.size();
        }
        
        class FolderViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            
            public FolderViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && clickListener != null) {
                        clickListener.onFolderClick(folders.get(position));
                    }
                });
                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && selectListener != null) {
                        selectListener.onFolderSelect(folders.get(position));
                        return true;
                    }
                    return false;
                });
            }
            
            public void bind(File folder) {
                if (folder.getName().equals("..")) {
                    textView.setText("⬆️ .. (Parent)");
                } else {
                    textView.setText("📁 " + folder.getName());
                }
            }
        }
    }
} 