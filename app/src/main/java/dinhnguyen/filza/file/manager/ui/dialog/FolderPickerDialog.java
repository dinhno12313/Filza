package dinhnguyen.filza.file.manager.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dinhnguyen.filza.file.manager.R;

public class FolderPickerDialog extends DialogFragment {
    
    public interface FolderSelectCallback {
        void onFolderSelected(File folder);
    }

    private static final String ARG_INITIAL_PATH = "initial_path";
    private static final String ARG_TITLE = "title";
    
    private File currentFolder;
    private FolderSelectCallback callback;
    private FolderAdapter adapter;
    private TextView textViewCurrentPath;
    private LinearLayout breadcrumbContainer;
    private RecyclerView recyclerViewFolders;
    private MaterialButton buttonSelectCurrent;
    private MaterialButton buttonCancel;
    
    private List<File> folderHistory = new ArrayList<>();
    private int currentHistoryIndex = -1;

    public static FolderPickerDialog newInstance(File initialFolder, FolderSelectCallback callback) {
        return newInstance(initialFolder, "Select Destination Folder", callback);
    }

    public static FolderPickerDialog newInstance(File initialFolder, String title, FolderSelectCallback callback) {
        FolderPickerDialog dialog = new FolderPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_PATH, initialFolder.getAbsolutePath());
        args.putString(ARG_TITLE, title);
        dialog.setArguments(args);
        dialog.setCallback(callback);
        return dialog;
    }

    public void setCallback(FolderSelectCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_folder_picker, null);
        
        initializeViews(view);
        setupInitialFolder();
        setupRecyclerView();
        setupButtons();
        updateUI();
        
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE, "Select Destination Folder") : "Select Destination Folder";
        
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setCancelable(true)
                .create();
    }

    private void initializeViews(View view) {
        textViewCurrentPath = view.findViewById(R.id.textViewCurrentPath);
        breadcrumbContainer = view.findViewById(R.id.breadcrumbContainer);
        recyclerViewFolders = view.findViewById(R.id.recyclerViewFolders);
        buttonSelectCurrent = view.findViewById(R.id.buttonSelectCurrent);
        buttonCancel = view.findViewById(R.id.buttonCancel);
    }

    private void setupInitialFolder() {
        String initialPath = getArguments() != null ? getArguments().getString(ARG_INITIAL_PATH) : null;
        
        if (initialPath != null) {
            currentFolder = new File(initialPath);
        } else {
            // Start from external storage if available, otherwise internal storage
            File externalStorage = new File("/storage/emulated/0/");
            if (externalStorage.exists() && externalStorage.canRead()) {
                currentFolder = externalStorage;
            } else {
                currentFolder = requireContext().getFilesDir();
            }
        }
        
        // Ensure the folder exists and is readable
        if (!currentFolder.exists() || !currentFolder.canRead()) {
            currentFolder = requireContext().getFilesDir();
        }
        
        addToHistory(currentFolder);
    }

    private void setupRecyclerView() {
        adapter = new FolderAdapter();
        recyclerViewFolders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewFolders.setAdapter(adapter);
    }

    private void setupButtons() {
        buttonSelectCurrent.setOnClickListener(v -> {
            if (callback != null) {
                callback.onFolderSelected(currentFolder);
            }
            dismiss();
        });

        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void updateUI() {
        updateCurrentPath();
        updateBreadcrumb();
        updateFolderList();
    }

    private void updateCurrentPath() {
        if (textViewCurrentPath != null) {
            textViewCurrentPath.setText(currentFolder.getAbsolutePath());
        }
    }

    private void updateBreadcrumb() {
        if (breadcrumbContainer == null) return;
        
        breadcrumbContainer.removeAllViews();
        List<File> pathComponents = getPathComponents(currentFolder);
        
        for (int i = 0; i < pathComponents.size(); i++) {
            File component = pathComponents.get(i);
            View breadcrumbView = createBreadcrumbView(component, i == pathComponents.size() - 1);
            breadcrumbContainer.addView(breadcrumbView);
            
            // Add separator if not the last item
            if (i < pathComponents.size() - 1) {
                View separator = createSeparatorView();
                breadcrumbContainer.addView(separator);
            }
        }
    }

    private List<File> getPathComponents(File folder) {
        List<File> components = new ArrayList<>();
        File current = folder;
        
        while (current != null) {
            components.add(0, current);
            current = current.getParentFile();
        }
        
        return components;
    }

    private View createBreadcrumbView(File folder, boolean isLast) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_breadcrumb, breadcrumbContainer, false);
        TextView textView = view.findViewById(R.id.textViewBreadcrumb);
        ImageView separator = view.findViewById(R.id.imageViewSeparator);
        
        String displayName = folder.getName();
        if (displayName.isEmpty()) {
            displayName = "/";
        }
        
        textView.setText(displayName);
        
        if (isLast) {
            textView.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark));
            separator.setVisibility(View.GONE);
        } else {
            view.setOnClickListener(v -> navigateToFolder(folder));
        }
        
        return view;
    }

    private View createSeparatorView() {
        View view = new View(requireContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(16, 16));
        return view;
    }

    private void updateFolderList() {
        if (adapter == null) return;
        
        List<FolderItem> folderItems = new ArrayList<>();
        
        // Add parent folder if available
        File parent = currentFolder.getParentFile();
        if (parent != null && parent.canRead()) {
            folderItems.add(new FolderItem(parent, ".. (Up)", true));
        }
        
        // Add subfolders
        File[] subDirs = currentFolder.listFiles(File::isDirectory);
        if (subDirs != null) {
            Arrays.sort(subDirs, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (File subDir : subDirs) {
                if (subDir.canRead()) {
                    folderItems.add(new FolderItem(subDir, subDir.getName(), false));
                }
            }
        }
        
        adapter.setFolderItems(folderItems);
    }

    private void navigateToFolder(File folder) {
        if (folder != null && folder.exists() && folder.canRead()) {
            currentFolder = folder;
            addToHistory(folder);
            updateUI();
        } else {
            Toast.makeText(requireContext(), "Cannot access folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToHistory(File folder) {
        // Remove any history after current index
        while (folderHistory.size() > currentHistoryIndex + 1) {
            folderHistory.remove(folderHistory.size() - 1);
        }
        
        folderHistory.add(folder);
        currentHistoryIndex = folderHistory.size() - 1;
        
        // Limit history size
        if (folderHistory.size() > 50) {
            folderHistory.remove(0);
            currentHistoryIndex--;
        }
    }

    public boolean canGoBack() {
        return currentHistoryIndex > 0;
    }

    public boolean canGoForward() {
        return currentHistoryIndex < folderHistory.size() - 1;
    }

    public void goBack() {
        if (canGoBack()) {
            currentHistoryIndex--;
            currentFolder = folderHistory.get(currentHistoryIndex);
            updateUI();
        }
    }

    public void goForward() {
        if (canGoForward()) {
            currentHistoryIndex++;
            currentFolder = folderHistory.get(currentHistoryIndex);
            updateUI();
        }
    }

    private static class FolderItem {
        final File folder;
        final String displayName;
        final boolean isParent;

        FolderItem(File folder, String displayName, boolean isParent) {
            this.folder = folder;
            this.displayName = displayName;
            this.isParent = isParent;
        }
    }

    private class FolderAdapter extends RecyclerView.Adapter<FolderViewHolder> {
        private List<FolderItem> folderItems = new ArrayList<>();

        void setFolderItems(List<FolderItem> folderItems) {
            this.folderItems = folderItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
            FolderItem item = folderItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return folderItems.size();
        }
    }

    private class FolderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewFolderIcon;
        private final TextView textViewFolderName;
        private final ImageView imageViewArrow;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFolderIcon = itemView.findViewById(R.id.imageViewFolderIcon);
            textViewFolderName = itemView.findViewById(R.id.textViewFolderName);
            imageViewArrow = itemView.findViewById(R.id.imageViewArrow);
        }

        void bind(FolderItem item) {
            textViewFolderName.setText(item.displayName);
            
            if (item.isParent) {
                imageViewFolderIcon.setImageResource(R.drawable.ic_arrow_back);
                imageViewArrow.setVisibility(View.GONE);
            } else {
                imageViewFolderIcon.setImageResource(R.drawable.ic_folder);
                imageViewArrow.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> navigateToFolder(item.folder));
        }
    }
} 