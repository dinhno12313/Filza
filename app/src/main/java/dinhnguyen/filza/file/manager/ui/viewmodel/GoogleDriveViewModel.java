package dinhnguyen.filza.file.manager.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.model.GoogleDriveFile;
import dinhnguyen.filza.file.manager.service.GoogleDriveService;
import dinhnguyen.filza.file.manager.service.GoogleDriveServiceImpl;

public class GoogleDriveViewModel extends AndroidViewModel {
    
    private static final String TAG = "GoogleDriveViewModel";
    
    private GoogleDriveService googleDriveService;
    private GoogleSignInClient googleSignInClient;
    
    // LiveData for UI state
    private MutableLiveData<List<GoogleDriveFile>> files;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> error;
    private MutableLiveData<Boolean> isSignedIn;
    private MutableLiveData<String> currentFolderName;
    private MutableLiveData<Boolean> canNavigateToParent;
    private MutableLiveData<File> downloadedFile;
    private MutableLiveData<Integer> downloadProgress;
    
    // Upload related LiveData
    private MutableLiveData<Boolean> isUploading;
    private MutableLiveData<Integer> uploadProgress;
    private MutableLiveData<String> uploadFileName;
    private MutableLiveData<GoogleDriveFile> uploadedFile;
    
    public GoogleDriveViewModel(@NonNull Application application) {
        super(application);
        
        // Initialize LiveData
        files = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        error = new MutableLiveData<>();
        isSignedIn = new MutableLiveData<>(false);
        currentFolderName = new MutableLiveData<>("Google Drive");
        canNavigateToParent = new MutableLiveData<>(false);
        downloadedFile = new MutableLiveData<>();
        downloadProgress = new MutableLiveData<>(0);
        
        // Initialize upload LiveData
        isUploading = new MutableLiveData<>(false);
        uploadProgress = new MutableLiveData<>(0);
        uploadFileName = new MutableLiveData<>();
        uploadedFile = new MutableLiveData<>();
        
        // Initialize Google Sign-In
        initializeGoogleSignIn();
        
        // Initialize Google Drive service
        googleDriveService = new GoogleDriveServiceImpl(application);
    }
    
    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file"))
            .build();
        
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getApplication(), gso);
    }
    
    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }
    
    public void handleSignInResult(GoogleSignInAccount account) {
        if (account != null) {
            googleDriveService.initialize(account);
            isSignedIn.postValue(true);
            loadFiles("root");
        } else {
            isSignedIn.postValue(false);
            error.postValue("Sign in failed");
        }
    }
    
    public void signOut() {
        googleDriveService.signOut();
        googleSignInClient.signOut();
        isSignedIn.postValue(false);
        files.postValue(null);
        currentFolderName.postValue("Google Drive");
        canNavigateToParent.postValue(false);
    }
    
    public void loadFiles(String folderId) {
        if (!googleDriveService.isInitialized()) {
            error.postValue("Please sign in to Google Drive first");
            return;
        }
        
        isLoading.postValue(true);
        error.postValue(null);
        
        googleDriveService.getFiles(folderId, new GoogleDriveService.GoogleDriveCallback<List<GoogleDriveFile>>() {
            @Override
            public void onSuccess(List<GoogleDriveFile> result) {
                files.postValue(result);
                isLoading.postValue(false);
                updateNavigationState();
            }
            
            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }
    
    public void navigateToFolder(GoogleDriveFile folder) {
        if (folder.isFolder()) {
            loadFiles(folder.getId());
            currentFolderName.postValue(folder.getName());
        }
    }
    
    public void navigateToParent() {
        if (googleDriveService.canNavigateToParent()) {
            googleDriveService.navigateToParent(new GoogleDriveService.GoogleDriveCallback<String>() {
                @Override
                public void onSuccess(String parentFolderId) {
                    loadFiles(parentFolderId);
                    if (parentFolderId.equals("root")) {
                        currentFolderName.postValue("Google Drive");
                    } else {
                        // Get parent folder name
                        googleDriveService.getFileById(parentFolderId, new GoogleDriveService.GoogleDriveCallback<GoogleDriveFile>() {
                            @Override
                            public void onSuccess(GoogleDriveFile parentFolder) {
                                currentFolderName.postValue(parentFolder.getName());
                            }
                            
                            @Override
                            public void onError(String errorMessage) {
                                currentFolderName.postValue("Google Drive");
                            }
                        });
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        }
    }
    
    public void downloadFile(GoogleDriveFile driveFile) {
        if (!googleDriveService.isInitialized()) {
            error.postValue("Please sign in to Google Drive first");
            return;
        }
        
        // Create external downloads directory
        File downloadDir = new File(getApplication().getExternalFilesDir(null), "downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        File localFile = new File(downloadDir, driveFile.getName());
        
        // Check if file already exists
        if (localFile.exists()) {
            downloadedFile.postValue(localFile);
            return;
        }
        
        downloadProgress.postValue(0);
        
        googleDriveService.downloadFile(driveFile, localFile, new GoogleDriveService.GoogleDriveCallback<File>() {
            @Override
            public void onSuccess(File result) {
                downloadedFile.postValue(result);
                downloadProgress.postValue(100);
                Log.d(TAG, "File downloaded successfully: " + result.getAbsolutePath());
            }
            
            @Override
            public void onError(String errorMessage) {
                error.postValue("Download failed: " + errorMessage);
                downloadProgress.postValue(0);
            }
        });
    }
    
    public void uploadFile(File localFile) {
        if (!googleDriveService.isInitialized()) {
            error.postValue("Please sign in to Google Drive first");
            return;
        }
        
        if (!localFile.exists()) {
            error.postValue("File does not exist");
            return;
        }
        
        isUploading.postValue(true);
        uploadProgress.postValue(0);
        uploadFileName.postValue(localFile.getName());
        error.postValue(null);
        
        String currentFolderId = googleDriveService.getCurrentFolderId();
        boolean isTemporaryFile = localFile.getParentFile() != null && 
                                 localFile.getParentFile().equals(getApplication().getCacheDir());
        
        googleDriveService.uploadFile(localFile, currentFolderId, new GoogleDriveService.UploadProgressCallback() {
            @Override
            public void onProgress(int progress) {
                uploadProgress.postValue(progress);
            }
            
            @Override
            public void onSuccess(GoogleDriveFile result) {
                uploadedFile.postValue(result);
                isUploading.postValue(false);
                uploadProgress.postValue(100);
                uploadFileName.postValue(null);
                
                // Clean up temporary file if it was created for upload
                if (isTemporaryFile) {
                    localFile.delete();
                }
                
                // Refresh the file list to show the uploaded file
                loadFiles(currentFolderId);
                
                Log.d(TAG, "File uploaded successfully: " + result.getName());
            }
            
            @Override
            public void onError(String errorMessage) {
                error.postValue("Upload failed: " + errorMessage);
                isUploading.postValue(false);
                uploadProgress.postValue(0);
                uploadFileName.postValue(null);
                
                // Clean up temporary file if it was created for upload
                if (isTemporaryFile) {
                    localFile.delete();
                }
            }
        });
    }
    
    public void createFolder(String folderName) {
        if (!googleDriveService.isInitialized()) {
            error.postValue("Please sign in to Google Drive first");
            return;
        }
        
        if (folderName == null || folderName.trim().isEmpty()) {
            error.postValue("Folder name cannot be empty");
            return;
        }
        
        isLoading.postValue(true);
        error.postValue(null);
        
        String currentFolderId = googleDriveService.getCurrentFolderId();
        
        googleDriveService.createFolder(folderName.trim(), currentFolderId, new GoogleDriveService.GoogleDriveCallback<GoogleDriveFile>() {
            @Override
            public void onSuccess(GoogleDriveFile result) {
                isLoading.postValue(false);
                
                // Refresh the file list to show the new folder
                loadFiles(currentFolderId);
                
                Log.d(TAG, "Folder created successfully: " + result.getName());
            }
            
            @Override
            public void onError(String errorMessage) {
                error.postValue("Failed to create folder: " + errorMessage);
                isLoading.postValue(false);
            }
        });
    }
    
    private void updateNavigationState() {
        canNavigateToParent.postValue(googleDriveService.canNavigateToParent());
    }
    
    public void clearError() {
        error.postValue(null);
    }
    
    public String getDownloadDirectoryPath() {
        File downloadDir = new File(getApplication().getExternalFilesDir(null), "downloads");
        return downloadDir.getAbsolutePath();
    }
    
    public String getCurrentFolderId() {
        return googleDriveService.getCurrentFolderId();
    }
    
    // Getters for LiveData
    public LiveData<List<GoogleDriveFile>> getFiles() {
        return files;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsSignedIn() {
        return isSignedIn;
    }
    
    public LiveData<String> getCurrentFolderName() {
        return currentFolderName;
    }
    
    public LiveData<Boolean> getCanNavigateToParent() {
        return canNavigateToParent;
    }
    
    public LiveData<File> getDownloadedFile() {
        return downloadedFile;
    }
    
    public LiveData<Integer> getDownloadProgress() {
        return downloadProgress;
    }
    
    // Upload related getters
    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }
    
    public LiveData<Integer> getUploadProgress() {
        return uploadProgress;
    }
    
    public LiveData<String> getUploadFileName() {
        return uploadFileName;
    }
    
    public LiveData<GoogleDriveFile> getUploadedFile() {
        return uploadedFile;
    }
} 