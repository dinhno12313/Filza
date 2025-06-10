package dinhnguyen.filza.file.manager.ui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.listener.PageClickListener;
import dinhnguyen.filza.file.manager.ui.model.PdfPage;

public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PdfPageViewHolder> {
    
    private final List<PdfPage> pdfPages;
    private final List<PhotoView> photoViews;
    private PageClickListener pageClickListener;
    
    public PdfPageAdapter() {
        this.pdfPages = new ArrayList<>();
        this.photoViews = new ArrayList<>();
    }
    
    public void setPdfPages(List<PdfPage> pages) {
        this.pdfPages.clear();
        this.photoViews.clear();
        if (pages != null) {
            this.pdfPages.addAll(pages);
        }
        notifyDataSetChanged();
    }
    
    public void setPageClickListener(PageClickListener listener) {
        this.pageClickListener = listener;
    }
    
    @NonNull
    @Override
    public PdfPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_page, parent, false);
        return new PdfPageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PdfPageViewHolder holder, int position) {
        holder.bind(pdfPages.get(position), position);
        // Store reference to PhotoView for fit to screen functionality
        if (position < photoViews.size()) {
            photoViews.set(position, holder.photoView);
        } else {
            photoViews.add(holder.photoView);
        }
    }
    
    @Override
    public int getItemCount() {
        return pdfPages.size();
    }
    
    public void fitAllPagesToScreen() {
        for (PhotoView photoView : photoViews) {
            if (photoView != null) {
                photoView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                photoView.setScale(1.0f, false);
            }
        }
    }
    
    public void cleanup() {
        for (PdfPage page : pdfPages) {
            page.unloadPageBitmap();
        }
        photoViews.clear();
    }
    
    class PdfPageViewHolder extends RecyclerView.ViewHolder {
        private final PhotoView photoView;
        private final TextView textPageNumber;
        private final MaterialButton btnExport;
        private final MaterialButton btnHighlight;
        
        public PdfPageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
            textPageNumber = itemView.findViewById(R.id.textPageNumber);
            btnExport = itemView.findViewById(R.id.btnExport);
            btnHighlight = itemView.findViewById(R.id.btnHighlight);
        }
        
        public void bind(PdfPage pdfPage, int position) {
            textPageNumber.setText("Page " + (position + 1));
            
            // Load page bitmap if not already loaded
            if (!pdfPage.isLoaded()) {
                pdfPage.loadPageBitmap();
            }
            
            // Set bitmap to PhotoView
            Bitmap pageBitmap = pdfPage.getPageBitmap();
            if (pageBitmap != null) {
                photoView.setImageBitmap(pageBitmap);
            }
            
            // Set up click listeners
            itemView.setOnClickListener(v -> {
                if (pageClickListener != null) {
                    pageClickListener.onPageClicked(position);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (pageClickListener != null) {
                    pageClickListener.onPageLongClicked(position);
                }
                return true;
            });
            
            btnExport.setOnClickListener(v -> {
                if (pageClickListener != null) {
                    pageClickListener.onPageExportRequested(position);
                }
            });
            
            btnHighlight.setOnClickListener(v -> {
                // For now, just show a toast since PhotoView doesn't have built-in highlighting
                Toast.makeText(itemView.getContext(), 
                    "Highlight functionality will be implemented separately", 
                    Toast.LENGTH_SHORT).show();
            });
        }
    }
} 