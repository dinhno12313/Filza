package dinhnguyen.filza.file.manager.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dinhnguyen.filza.file.manager.handlers.PdfExportHandler;
import dinhnguyen.filza.file.manager.manager.PdfManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.PdfViewerViewModel;

public class PdfViewerViewModelFactory implements ViewModelProvider.Factory {
    
    private final PdfManager pdfManager;
    private final PdfExportHandler exportHandler;
    
    public PdfViewerViewModelFactory(PdfManager pdfManager, PdfExportHandler exportHandler) {
        this.pdfManager = pdfManager;
        this.exportHandler = exportHandler;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PdfViewerViewModel.class)) {
            return (T) new PdfViewerViewModel(pdfManager, exportHandler);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 