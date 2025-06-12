package dinhnguyen.filza.file.manager.handlers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dinhnguyen.filza.file.manager.constants.PdfConstants;

public class PdfExportHandler {
    
    private final Context context;
    
    public interface ExportListener {
        void onExportSuccess(String fileName);
        void onExportError(String error);
    }
    
    public PdfExportHandler(Context context) {
        this.context = context;
    }
    
    public void exportPageAsImage(Bitmap pageBitmap, int pageIndex, ExportListener listener) {
        if (pageIndex < 0) {
            if (listener != null) {
                listener.onExportError(PdfConstants.ERROR_PAGE_INVALID);
            }
            return;
        }
        
        if (pageBitmap == null) {
            if (listener != null) {
                listener.onExportError(PdfConstants.ERROR_BITMAP_CREATION);
            }
            return;
        }
        
        try {
            File exportFile = createExportFile(pageIndex);
            saveBitmapToFile(pageBitmap, exportFile);
            addToGallery(exportFile);
            
            if (listener != null) {
                String message = String.format(PdfConstants.SUCCESS_PAGE_EXPORTED, 
                    pageIndex + 1, exportFile.getName());
                listener.onExportSuccess(message);
            }
            
        } catch (IOException e) {
            if (listener != null) {
                listener.onExportError(PdfConstants.ERROR_EXPORT_FAILED + ": " + e.getMessage());
            }
        }
    }
    
    public void exportAllPages() {
        // TODO: Implement export all pages functionality
        // This would iterate through all pages and export them
        // For now, we'll just notify that this feature is not implemented
    }
    
    private File createExportFile(int pageIndex) throws IOException {
        // Create export directory
        File exportDir = new File(context.getExternalFilesDir(null), PdfConstants.EXPORT_DIR_NAME);
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                throw new IOException("Không thể tạo thư mục xuất");
            }
        }
        
        // Generate filename
        String fileName = PdfConstants.EXPORT_FILE_PREFIX + (pageIndex + 1) + 
                         "_" + System.currentTimeMillis() + PdfConstants.EXPORT_FILE_SUFFIX;
        
        return new File(exportDir, fileName);
    }
    
    private void saveBitmapToFile(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, PdfConstants.EXPORT_QUALITY, out);
            out.flush();
        }
    }
    
    private void addToGallery(File file) {
        // Add to gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        context.sendBroadcast(mediaScanIntent);
    }
    
    public File getExportDirectory() {
        return new File(context.getExternalFilesDir(null), PdfConstants.EXPORT_DIR_NAME);
    }
} 