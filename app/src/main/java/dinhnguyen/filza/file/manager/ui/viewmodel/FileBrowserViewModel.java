package dinhnguyen.filza.file.manager.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.manager.DirectoryManager;
import dinhnguyen.filza.file.manager.manager.FileOpenManager;
import dinhnguyen.filza.file.manager.manager.FileImportManager;
import dinhnguyen.filza.file.manager.handlers.FileHandler;

public class FileBrowserViewModel extends ViewModel {
    
    private final DirectoryManager directoryManager;
    private final FileOpenManager fileOpenManager;
    private final FileImportManager fileImportManager;
    
    private final MutableLiveData<List<File>> files = new MutableLiveData<>();
    private final MutableLiveData<File> currentDirectory = new MutableLiveData<>();
    
    public FileBrowserViewModel(DirectoryManager directoryManager, 
                              FileOpenManager fileOpenManager,
                              FileImportManager fileImportManager) {
        this.directoryManager = directoryManager;
        this.fileOpenManager = fileOpenManager;
        this.fileImportManager = fileImportManager;
    }
    
    public LiveData<List<File>> getFiles() {
        return files;
    }
    
    public LiveData<File> getCurrentDirectory() {
        return currentDirectory;
    }
    
    public void loadDirectory(File directory) {
        currentDirectory.setValue(directory);
        
        List<File> fileList = directoryManager.getFilesInDirectory(directory);
        files.setValue(fileList);
    }
    
    public void navigateToParent() {
        File current = currentDirectory.getValue();
        if (current != null) {
            File parent = directoryManager.getParentDirectory(current);
            if (parent != null) {
                loadDirectory(parent);
            }
        }
    }
    
    public boolean canNavigateToParent() {
        File current = currentDirectory.getValue();
        return current != null && directoryManager.canNavigateToParent(current);
    }
    
    public void openFile(File file) {
        fileOpenManager.openFile(file);
    }
    
    public void createFolder(String folderName) {
        File current = currentDirectory.getValue();
        if (current != null) {
            boolean success = directoryManager.createFolder(current, folderName);
            if (success) {
                loadDirectory(current);
            }
        }
    }
    
    public void importFile(android.net.Uri uri) {
        File current = currentDirectory.getValue();
        if (current != null) {
            fileImportManager.importFile(uri, current, () -> loadDirectory(current));
        }
    }
} 