package dinhnguyen.filza.file.manager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.listener.FileActionListener;
import dinhnguyen.filza.file.manager.utils.FileIconManager;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private List<File> files;
    private final FileActionListener actionListener;
    private final OnFileClickListener clickListener;
    private final Context context;
    
    // Multi-select functionality
    private boolean isMultiSelectMode = false;
    private final Set<File> selectedFiles = new HashSet<>();
    private final OnMultiSelectListener multiSelectListener;
    
    // View mode
    private ViewMode currentViewMode = ViewMode.LIST;
    
    // Sorting
    private SortMode currentSortMode = SortMode.NAME;

    public enum ViewMode {
        LIST, GRID
    }
    
    public enum SortMode {
        NAME, DATE, SIZE
    }

    public interface OnFileClickListener {
        void onClick(File file);
    }
    
    public interface OnMultiSelectListener {
        void onSelectionChanged(int selectedCount);
        void onMultiSelectModeChanged(boolean isMultiSelectMode);
    }

    public FileAdapter(Context context, List<File> files, FileActionListener actionListener, 
                      OnFileClickListener clickListener, OnMultiSelectListener multiSelectListener) {
        this.context = context;
        this.files = files != null ? files : new ArrayList<>();
        this.actionListener = actionListener;
        this.clickListener = clickListener;
        this.multiSelectListener = multiSelectListener;
    }

    @Override
    public int getItemViewType(int position) {
        return currentViewMode == ViewMode.GRID ? 1 : 0;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) { // Grid view
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file_grid, parent, false);
        } else { // List view
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file, parent, false);
        }
        return new FileViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void updateFiles(List<File> newFiles) {
        this.files.clear();
        this.files.addAll(newFiles);
        sortFiles();
        notifyDataSetChanged();
    }

    public void setFiles(List<File> list) {
        this.files = list != null ? list : new ArrayList<>();
        sortFiles();
        notifyDataSetChanged();
    }
    
    // Multi-select functionality
    public void setMultiSelectMode(boolean enabled) {
        this.isMultiSelectMode = enabled;
        if (!enabled) {
            selectedFiles.clear();
        }
        notifyDataSetChanged();
        if (multiSelectListener != null) {
            multiSelectListener.onMultiSelectModeChanged(enabled);
            multiSelectListener.onSelectionChanged(selectedFiles.size());
        }
    }
    
    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }
    
    public Set<File> getSelectedFiles() {
        return new HashSet<>(selectedFiles);
    }
    
    public void clearSelection() {
        selectedFiles.clear();
        notifyDataSetChanged();
        if (multiSelectListener != null) {
            multiSelectListener.onSelectionChanged(0);
        }
    }
    
    // View mode functionality
    public void setViewMode(ViewMode viewMode) {
        this.currentViewMode = viewMode;
        notifyDataSetChanged();
    }
    
    public ViewMode getCurrentViewMode() {
        return currentViewMode;
    }
    
    // Sorting functionality
    public void setSortMode(SortMode sortMode) {
        this.currentSortMode = sortMode;
        sortFiles();
        notifyDataSetChanged();
    }
    
    public SortMode getCurrentSortMode() {
        return currentSortMode;
    }
    
    private void sortFiles() {
        if (files == null || files.isEmpty()) return;
        
        switch (currentSortMode) {
            case NAME:
                Collections.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
                break;
            case DATE:
                Collections.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return Long.compare(f2.lastModified(), f1.lastModified()); // Newest first
                });
                break;
            case SIZE:
                Collections.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return Long.compare(f2.length(), f1.length()); // Largest first
                });
                break;
        }
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imageFileIcon;
        private final TextView textFileName;
        private final TextView textFileInfo;
        private final TextView textFileDate;
        private final MaterialButton buttonOptions;
        private final MaterialCardView cardView;
        private final int viewType;

        public FileViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            imageFileIcon = itemView.findViewById(R.id.imageFileIcon);
            textFileName = itemView.findViewById(R.id.textFileName);
            textFileInfo = itemView.findViewById(R.id.textFileInfo);
            textFileDate = itemView.findViewById(R.id.textFileDate);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
            cardView = (MaterialCardView) itemView;
        }

        public void bind(File file) {
            textFileName.setText(file.getName());
            textFileInfo.setText(FileIconManager.getFileInfo(file));
            // Set last modified date with different format for grid vs list
            String formattedDate = formatDate(file.lastModified());
            textFileDate.setText(formattedDate);
            
            // Set appropriate icon for the file
            imageFileIcon.setImageDrawable(FileIconManager.getFileIcon(context, file));
            
            // Handle multi-select mode
            if (isMultiSelectMode) {
                updateSelectionUI(file);
                itemView.setOnClickListener(v -> toggleSelection(file));
                buttonOptions.setVisibility(View.GONE);
            } else {
                // Normal mode
                selectedFiles.remove(file);
                cardView.setStrokeWidth(0);
                cardView.setStrokeColor(context.getColor(android.R.color.transparent));
                
                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onClick(file);
                    }
                });
                buttonOptions.setVisibility(View.VISIBLE);
                buttonOptions.setOnClickListener(v -> showOptionsMenu(v, file));
            }
        }
        
        private void updateSelectionUI(File file) {
            if (selectedFiles.contains(file)) {
                cardView.setStrokeWidth(4);
                cardView.setStrokeColor(context.getColor(R.color.purple_500));
            } else {
                cardView.setStrokeWidth(0);
                cardView.setStrokeColor(context.getColor(android.R.color.transparent));
            }
        }
        
        private void toggleSelection(File file) {
            if (selectedFiles.contains(file)) {
                selectedFiles.remove(file);
            } else {
                selectedFiles.add(file);
            }
            updateSelectionUI(file);
            if (multiSelectListener != null) {
                multiSelectListener.onSelectionChanged(selectedFiles.size());
            }
        }

        private void showOptionsMenu(View view, File file) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.file_item_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_rename) {
                    actionListener.onRename(file);
                    return true;
                } else if (itemId == R.id.action_move) {
                    actionListener.onMove(file);
                    return true;
                } else if (itemId == R.id.action_copy) {
                    actionListener.onCopy(file);
                    return true;
                } else if (itemId == R.id.action_zip) {
                    actionListener.onZip(file);
                    return true;
                } else if (itemId == R.id.action_unzip) {
                    actionListener.onUnzip(file);
                    return true;
                } else if (itemId == R.id.action_duplicate) {
                    actionListener.onDuplicate(file);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    actionListener.onDelete(file);
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private String formatDate(long millis) {
            SimpleDateFormat sdf;
            if (viewType == 1) { // Grid view - shorter format
                sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
            } else { // List view - full format
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            }
            return sdf.format(new Date(millis));
        }
    }
}