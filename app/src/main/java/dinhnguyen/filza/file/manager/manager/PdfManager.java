package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dinhnguyen.filza.file.manager.constants.PdfConstants;
import dinhnguyen.filza.file.manager.ui.model.PdfPage;
import dinhnguyen.filza.file.manager.utils.FileTypeUtils;

public class PdfManager {
    
    private final Context context;
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;
    private String pdfFilePath;
    private int totalPages = 0;
    
    public interface PdfManagerListener {
        void onPdfLoaded(String fileName, int totalPages);
        void onPdfLoadError(String error);
        void onPdfPagesCreated(List<PdfPage> pages);
    }
    
    public PdfManager(Context context) {
        this.context = context;
    }
    
    public boolean loadPdfFile(String filePath) {
        if (filePath == null) {
            notifyError(PdfConstants.ERROR_NO_FILE_PATH);
            return false;
        }

        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            notifyError(PdfConstants.ERROR_FILE_NOT_EXISTS);
            return false;
        }

        if (!FileTypeUtils.isPdfFile(pdfFile)) {
            notifyError(PdfConstants.ERROR_INVALID_PDF_FILE);
            return false;
        }

        return setupPdfRenderer(pdfFile);
    }
    
    private boolean setupPdfRenderer(File pdfFile) {
        try {
            releaseResources();
            
            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
            totalPages = pdfRenderer.getPageCount();
            pdfFilePath = pdfFile.getAbsolutePath();
            
            return true;
            
        } catch (IOException e) {
            notifyError(PdfConstants.ERROR_LOAD_FAILED + ": " + e.getMessage());
            return false;
        }
    }
    
    public List<PdfPage> createPdfPages() {
        List<PdfPage> pages = new ArrayList<>();
        
        if (pdfRenderer == null) {
            return pages;
        }
        
        for (int i = 0; i < totalPages; i++) {
            PdfPage page = new PdfPage(i, pdfRenderer, context);
            pages.add(page);
        }
        
        return pages;
    }
    
    public PdfRenderer getPdfRenderer() {
        return pdfRenderer;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public String getPdfFilePath() {
        return pdfFilePath;
    }
    
    public boolean isReady() {
        return pdfRenderer != null && fileDescriptor != null;
    }
    
    public void release() {
        releaseResources();
    }
    
    private void releaseResources() {
        if (pdfRenderer != null) {
            pdfRenderer.close();
            pdfRenderer = null;
        }
        
        if (fileDescriptor != null) {
            try {
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileDescriptor = null;
        }
        
        totalPages = 0;
        pdfFilePath = null;
    }
    
    private void notifyError(String error) {
        // This would be handled by the listener pattern
        // For now, we'll just throw an exception or log the error
        throw new RuntimeException(error);
    }
} 