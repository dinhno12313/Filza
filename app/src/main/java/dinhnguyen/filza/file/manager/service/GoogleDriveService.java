package dinhnguyen.filza.file.manager.service;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.model.GoogleDriveFile;

public interface GoogleDriveService {
    
    /**
     * Initialize the Google Drive service with the signed-in account
     */
    void initialize(GoogleSignInAccount account);
    
    /**
     * Check if the service is initialized and ready to use
     */
    boolean isInitialized();
    
    /**
     * Get files from Google Drive root or specific folder
     */
    void getFiles(String folderId, GoogleDriveCallback<List<GoogleDriveFile>> callback);
    
    /**
     * Get file details by ID
     */
    void getFileById(String fileId, GoogleDriveCallback<GoogleDriveFile> callback);
    
    /**
     * Download a file from Google Drive to local storage
     */
    void downloadFile(GoogleDriveFile driveFile, File localFile, GoogleDriveCallback<File> callback);
    
    /**
     * Upload a file from local storage to Google Drive
     */
    void uploadFile(File localFile, String parentFolderId, GoogleDriveCallback<GoogleDriveFile> callback);
    
    /**
     * Upload a file from local storage to Google Drive with progress tracking
     */
    void uploadFile(File localFile, String parentFolderId, UploadProgressCallback callback);
    
    /**
     * Create a new folder in Google Drive
     */
    void createFolder(String folderName, String parentFolderId, GoogleDriveCallback<GoogleDriveFile> callback);
    
    /**
     * Get the current folder path
     */
    String getCurrentFolderId();
    
    /**
     * Navigate to parent folder
     */
    boolean canNavigateToParent();
    
    /**
     * Navigate to parent folder
     */
    void navigateToParent(GoogleDriveCallback<String> callback);
    
    /**
     * Sign out and clean up resources
     */
    void signOut();
    
    /**
     * Callback interface for Google Drive operations
     */
    interface GoogleDriveCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    /**
     * Callback interface for upload operations with progress tracking
     */
    interface UploadProgressCallback {
        void onProgress(int progress);
        void onSuccess(GoogleDriveFile result);
        void onError(String error);
    }
} 