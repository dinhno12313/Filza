package dinhnguyen.filza.file.manager.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dinhnguyen.filza.file.manager.manager.DirectoryManager;
import dinhnguyen.filza.file.manager.manager.FileImportManager;
import dinhnguyen.filza.file.manager.manager.FileOpenManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.FileBrowserViewModel;

public class FileBrowserViewModelFactory implements ViewModelProvider.Factory {
    
    private final DirectoryManager directoryManager;
    private final FileOpenManager fileOpenManager;
    private final FileImportManager fileImportManager;
    
    public FileBrowserViewModelFactory(DirectoryManager directoryManager,
                                     FileOpenManager fileOpenManager,
                                     FileImportManager fileImportManager) {
        this.directoryManager = directoryManager;
        this.fileOpenManager = fileOpenManager;
        this.fileImportManager = fileImportManager;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FileBrowserViewModel.class)) {
            return (T) new FileBrowserViewModel(directoryManager, fileOpenManager, fileImportManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 