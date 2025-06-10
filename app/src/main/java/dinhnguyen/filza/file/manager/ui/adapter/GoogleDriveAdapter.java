package dinhnguyen.filza.file.manager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.model.GoogleDriveFile;
import dinhnguyen.filza.file.manager.utils.FileIconManager;

public class GoogleDriveAdapter extends RecyclerView.Adapter<GoogleDriveAdapter.ViewHolder> {
    
    private final Context context;
    private List<GoogleDriveFile> files;
    private OnFileClickListener onFileClickListener;
    private OnFileLongClickListener onFileLongClickListener;
    private final SimpleDateFormat dateFormat;
    
    public interface OnFileClickListener {
        void onFileClick(GoogleDriveFile file);
    }
    
    public interface OnFileLongClickListener {
        void onFileLongClick(GoogleDriveFile file, View view);
    }
    
    public GoogleDriveAdapter(Context context) {
        this.context = context;
        this.files = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }
    
    public void setFiles(List<GoogleDriveFile> files) {
        this.files = files != null ? files : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnFileClickListener(OnFileClickListener listener) {
        this.onFileClickListener = listener;
    }
    
    public void setOnFileLongClickListener(OnFileLongClickListener listener) {
        this.onFileLongClickListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoogleDriveFile file = files.get(position);
        holder.bind(file);
    }
    
    @Override
    public int getItemCount() {
        return files.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imageFileIcon;
        private final TextView textFileName;
        private final TextView textFileInfo;
        private final TextView textFileDate;
        private final MaterialButton buttonOptions;
        private final MaterialCardView cardView;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFileIcon = itemView.findViewById(R.id.imageFileIcon);
            textFileName = itemView.findViewById(R.id.textFileName);
            textFileInfo = itemView.findViewById(R.id.textFileInfo);
            textFileDate = itemView.findViewById(R.id.textFileDate);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
            cardView = (MaterialCardView) itemView;
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFileClickListener != null) {
                    onFileClickListener.onFileClick(files.get(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFileLongClickListener != null) {
                    onFileLongClickListener.onFileLongClick(files.get(position), v);
                    return true;
                }
                return false;
            });
        }
        
        public void bind(GoogleDriveFile file) {
            textFileName.setText(file.getName());
            if (file.isFolder()) {
                imageFileIcon.setImageResource(R.drawable.ic_folder);
                textFileInfo.setText("Folder");
            } else {
                imageFileIcon.setImageResource(FileIconManager.getIconResForMimeType(file.getMimeType()));
                textFileInfo.setText(formatFileSize(file.getSize()));
            }
            // Set last modified date
            if (file.getModifiedTime() > 0) {
                textFileDate.setText(dateFormat.format(new Date(file.getModifiedTime())));
            } else {
                textFileDate.setText("Unknown");
            }
            buttonOptions.setVisibility(View.GONE); // Hide options for now
        }
        
        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            
            return String.format(Locale.getDefault(), "%.1f %s", 
                size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
} 