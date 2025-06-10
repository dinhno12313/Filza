package dinhnguyen.filza.file.manager.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.constants.PdfConstants;
import dinhnguyen.filza.file.manager.handlers.PdfExportHandler;
import dinhnguyen.filza.file.manager.manager.PdfManager;
import dinhnguyen.filza.file.manager.ui.model.PdfPage;
import dinhnguyen.filza.file.manager.utils.FileTypeUtils;

public class PdfViewerViewModel extends ViewModel {
    
    private final PdfManager pdfManager;
    private final PdfExportHandler exportHandler;
    
    private final MutableLiveData<String> pdfTitle = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>();
    private final MutableLiveData<List<PdfPage>> pdfPages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    private String currentFilePath;
    
    public PdfViewerViewModel(PdfManager pdfManager, PdfExportHandler exportHandler) {
        this.pdfManager = pdfManager;
        this.exportHandler = exportHandler;
    }
    
    public LiveData<String> getPdfTitle() {
        return pdfTitle;
    }
    
    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }
    
    public LiveData<List<PdfPage>> getPdfPages() {
        return pdfPages;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    public boolean loadPdfFile(String filePath) {
        currentFilePath = filePath;
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        try {
            if (filePath == null) {
                errorMessage.setValue(PdfConstants.ERROR_NO_FILE_PATH);
                return false;
            }
            
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                errorMessage.setValue(PdfConstants.ERROR_FILE_NOT_EXISTS);
                return false;
            }
            
            if (!FileTypeUtils.isPdfFile(pdfFile)) {
                errorMessage.setValue(PdfConstants.ERROR_INVALID_PDF_FILE);
                return false;
            }
            
            boolean success = pdfManager.loadPdfFile(filePath);
            if (success) {
                setupPdfContent(pdfFile);
                return true;
            } else {
                errorMessage.setValue(PdfConstants.ERROR_LOAD_FAILED);
                return false;
            }
            
        } catch (Exception e) {
            errorMessage.setValue(PdfConstants.ERROR_LOAD_FAILED + ": " + e.getMessage());
            return false;
        } finally {
            isLoading.setValue(false);
        }
    }
    
    private void setupPdfContent(File pdfFile) {
        int pages = pdfManager.getTotalPages();
        totalPages.setValue(pages);
        
        String title = FileTypeUtils.getFileNameWithoutExtension(pdfFile.getName()) + 
                      " (" + pages + " trang)";
        pdfTitle.setValue(title);
        
        List<PdfPage> pagesList = pdfManager.createPdfPages();
        pdfPages.setValue(pagesList);
        
        successMessage.setValue(PdfConstants.SUCCESS_PDF_LOADED);
    }
    
    public void exportPage(int pageIndex) {
        List<PdfPage> pages = pdfPages.getValue();
        if (pages == null || pageIndex < 0 || pageIndex >= pages.size()) {
            errorMessage.setValue(PdfConstants.ERROR_PAGE_INVALID);
            return;
        }
        
        PdfPage page = pages.get(pageIndex);
        if (page == null) {
            errorMessage.setValue(PdfConstants.ERROR_PAGE_INVALID);
            return;
        }
        
        exportHandler.exportPageAsImage(page.getPageBitmap(), pageIndex, 
            new PdfExportHandler.ExportListener() {
                @Override
                public void onExportSuccess(String message) {
                    successMessage.setValue(message);
                }
                
                @Override
                public void onExportError(String error) {
                    errorMessage.setValue(error);
                }
            });
    }
    
    public void exportAllPages() {
        successMessage.setValue(PdfConstants.UI_EXPORT_ALL_FEATURE);
    }
    
    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
    
    public boolean isPdfReady() {
        return pdfManager.isReady();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        pdfManager.release();
    }
} 