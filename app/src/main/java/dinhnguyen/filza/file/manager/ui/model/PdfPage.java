package dinhnguyen.filza.file.manager.ui.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.util.Log;

public class PdfPage {
    private static final String TAG = "PdfPage";
    private static final int DEFAULT_DPI = 300;
    
    private final int pageIndex;
    private final PdfRenderer pdfRenderer;
    private final Context context;
    
    private Bitmap pageBitmap;
    private boolean isLoaded = false;
    private float scaleFactor = 1.0f;
    
    public PdfPage(int pageIndex, PdfRenderer pdfRenderer, Context context) {
        this.pageIndex = pageIndex;
        this.pdfRenderer = pdfRenderer;
        this.context = context;
    }
    
    public int getPageIndex() {
        return pageIndex;
    }
    
    public Bitmap getPageBitmap() {
        return pageBitmap;
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        // Reload bitmap with new scale if already loaded
        if (isLoaded) {
            loadPageBitmap();
        }
    }
    
    public void loadPageBitmap() {
        if (isLoaded && pageBitmap != null) {
            return; // Already loaded
        }
        
        try {
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
            
            if (page == null) {
                Log.e(TAG, "Failed to open page " + pageIndex);
                return;
            }
            
            // Calculate bitmap size based on scale factor
            int width = (int) (page.getWidth() * scaleFactor / 72.0f * DEFAULT_DPI);
            int height = (int) (page.getHeight() * scaleFactor / 72.0f * DEFAULT_DPI);
            
            // Create bitmap with ARGB_8888 config for better quality
            pageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            
            // Render the page
            page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            
            page.close();
            isLoaded = true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading page " + pageIndex, e);
            isLoaded = false;
        }
    }
    
    public void unloadPageBitmap() {
        if (pageBitmap != null && !pageBitmap.isRecycled()) {
            pageBitmap.recycle();
            pageBitmap = null;
        }
        isLoaded = false;
    }
    
    public void reloadPageBitmap() {
        unloadPageBitmap();
        loadPageBitmap();
    }
    
    public int getWidth() {
        try {
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
            if (page != null) {
                int width = (int) (page.getWidth() * scaleFactor / 72.0f * DEFAULT_DPI);
                page.close();
                return width;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting page width", e);
        }
        return 0;
    }
    
    public int getHeight() {
        try {
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
            if (page != null) {
                int height = (int) (page.getHeight() * scaleFactor / 72.0f * DEFAULT_DPI);
                page.close();
                return height;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting page height", e);
        }
        return 0;
    }
} 