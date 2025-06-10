package dinhnguyen.filza.file.manager.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.List;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.manager.FileOpenManager;
import dinhnguyen.filza.file.manager.model.GoogleDriveFile;
import dinhnguyen.filza.file.manager.ui.adapter.GoogleDriveAdapter;
import dinhnguyen.filza.file.manager.ui.viewmodel.GoogleDriveViewModel;

public class GoogleDriveActivity extends AppCompatActivity {
    
    private static final String TAG = "GoogleDriveActivity";
    private static final int RC_SIGN_IN = 9001;
    
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private MaterialButton btnBack;
    private MaterialButton btnSignOut;
    private MaterialButton btnSignIn;
    
    private View signInView;
    private View contentView;
    private View loadingView;
    private View emptyView;
    private RecyclerView recyclerViewFiles;
    
    private MaterialCardView downloadProgressCard;
    private LinearProgressIndicator downloadProgress;
    private TextView downloadFileName;
    private FloatingActionButton fabDownloadDirectory;
    
    private MaterialCardView uploadProgressCard;
    private LinearProgressIndicator uploadProgress;
    private TextView uploadFileName;
    private FloatingActionButton fabUpload;
    
    private GoogleDriveViewModel viewModel;
    private GoogleDriveAdapter adapter;
    private FileOpenManager fileOpenManager;
    
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            handleSignInResult(task);
        }
    );
    
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                handleFileSelection(uri);
            }
        }
    );
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        initializeViewModel();
        setupObservers();
        setupClickListeners();
        
        // Check if user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getGrantedScopes().contains(new Scope("https://www.googleapis.com/auth/drive.file"))) {
            viewModel.handleSignInResult(account);
        }
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        btnBack = findViewById(R.id.btnBack);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnSignIn = findViewById(R.id.btnSignIn);
        
        signInView = findViewById(R.id.signInView);
        contentView = findViewById(R.id.contentView);
        loadingView = findViewById(R.id.loadingView);
        emptyView = findViewById(R.id.emptyView);
        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        
        downloadProgressCard = findViewById(R.id.downloadProgressCard);
        downloadProgress = findViewById(R.id.downloadProgress);
        downloadFileName = findViewById(R.id.downloadFileName);
        fabDownloadDirectory = findViewById(R.id.fabDownloadDirectory);
        
        uploadProgressCard = findViewById(R.id.uploadProgressCard);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadFileName = findViewById(R.id.uploadFileName);
        fabUpload = findViewById(R.id.fabUpload);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    
    private void setupRecyclerView() {
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoogleDriveAdapter(this);
        recyclerViewFiles.setAdapter(adapter);
        
        adapter.setOnFileClickListener(this::onFileClick);
        adapter.setOnFileLongClickListener(this::onFileLongClick);
    }
    
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(GoogleDriveViewModel.class);
        fileOpenManager = new FileOpenManager(this, this::refresh);
    }
    
    private void setupObservers() {
        // Observe sign-in state
        viewModel.getIsSignedIn().observe(this, isSignedIn -> {
            if (isSignedIn) {
                showContentView();
                showDownloadDirectoryInfo();
            } else {
                showSignInView();
            }
        });
        
        // Observe files
        viewModel.getFiles().observe(this, files -> {
            adapter.setFiles(files);
            updateEmptyState(files);
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoadingView();
            } else {
                hideLoadingView();
            }
        });
        
        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observe current folder name
        viewModel.getCurrentFolderName().observe(this, folderName -> {
            toolbarTitle.setText(folderName);
        });
        
        // Observe navigation state
        viewModel.getCanNavigateToParent().observe(this, canNavigate -> {
            btnBack.setEnabled(canNavigate);
        });
        
        // Observe downloaded files
        viewModel.getDownloadedFile().observe(this, file -> {
            if (file != null) {
                String downloadPath = file.getParent();
                Toast.makeText(this, "File downloaded to: " + downloadPath, Toast.LENGTH_LONG).show();
                hideDownloadProgress();
            }
        });
        
        // Observe download progress
        viewModel.getDownloadProgress().observe(this, progress -> {
            downloadProgress.setProgress(progress);
        });
        
        // Observe upload state
        viewModel.getIsUploading().observe(this, isUploading -> {
            if (isUploading) {
                showUploadProgress();
            } else {
                hideUploadProgress();
            }
        });
        
        // Observe upload progress
        viewModel.getUploadProgress().observe(this, progress -> {
            uploadProgress.setProgress(progress);
        });
        
        // Observe upload file name
        viewModel.getUploadFileName().observe(this, fileName -> {
            if (fileName != null) {
                uploadFileName.setText(fileName);
            }
        });
        
        // Observe uploaded files
        viewModel.getUploadedFile().observe(this, file -> {
            if (file != null) {
                Toast.makeText(this, getString(R.string.file_uploaded_successfully) + ": " + file.getName(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            viewModel.navigateToParent();
        });
        
        btnSignOut.setOnClickListener(v -> {
            viewModel.signOut();
        });
        
        btnSignIn.setOnClickListener(v -> {
            signIn();
        });
        
        fabUpload.setOnClickListener(v -> {
            openFilePicker();
        });
        
        fabDownloadDirectory.setOnClickListener(v -> {
            openDownloadDirectory();
        });
    }
    
    private void signIn() {
        Intent signInIntent = viewModel.getGoogleSignInClient().getSignInIntent();
        signInLauncher.launch(signInIntent);
    }
    
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            viewModel.handleSignInResult(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Sign in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void onFileClick(GoogleDriveFile file) {
        if (file.isFolder()) {
            viewModel.navigateToFolder(file);
        } else {
            downloadAndOpenFile(file);
        }
    }
    
    private void onFileLongClick(GoogleDriveFile file, View view) {
        showFileContextMenu(file, view);
    }
    
    private void downloadAndOpenFile(GoogleDriveFile file) {
        showDownloadProgress(file.getName());
        viewModel.downloadFile(file);
    }
    
    private void showFileContextMenu(GoogleDriveFile file, View view) {
        // TODO: Implement file context menu
    }
    
    private void openFilePicker() {
        filePickerLauncher.launch("*/*");
    }
    
    private void handleFileSelection(Uri uri) {
        try {
            // Create a temporary file in the app's cache directory
            String fileName = getFileNameFromUri(uri);
            if (fileName == null) {
                fileName = "uploaded_file_" + System.currentTimeMillis();
            }
            
            File tempFile = new File(getCacheDir(), fileName);
            
            // Copy the content to the temporary file
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                
                if (inputStream != null) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    
                    // Upload the temporary file
                    viewModel.uploadFile(tempFile);
                } else {
                    Toast.makeText(this, getString(R.string.could_not_read_file), Toast.LENGTH_LONG).show();
                }
            }
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_accessing_file) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                // Fallback to URI path
                result = uri.getLastPathSegment();
            }
        } else if (uri.getScheme().equals("file")) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    
    private void openDownloadDirectory() {
        String downloadPath = viewModel.getDownloadDirectoryPath();
        Toast.makeText(this, getString(R.string.download_directory) + ": " + downloadPath, Toast.LENGTH_LONG).show();
        
        // Open the directory in the file browser
        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra("path", downloadPath);
        startActivity(intent);
    }
    
    private void showSignInView() {
        signInView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }
    
    private void showContentView() {
        signInView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }
    
    private void showLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerViewFiles.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void hideLoadingView() {
        loadingView.setVisibility(View.GONE);
        recyclerViewFiles.setVisibility(View.VISIBLE);
    }
    
    private void updateEmptyState(List<GoogleDriveFile> files) {
        if (files == null || files.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewFiles.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerViewFiles.setVisibility(View.VISIBLE);
        }
    }
    
    private void showDownloadProgress(String fileName) {
        downloadFileName.setText(fileName);
        downloadProgressCard.setVisibility(View.VISIBLE);
    }
    
    private void hideDownloadProgress() {
        downloadProgressCard.setVisibility(View.GONE);
    }
    
    private void showUploadProgress() {
        uploadProgressCard.setVisibility(View.VISIBLE);
    }
    
    private void hideUploadProgress() {
        uploadProgressCard.setVisibility(View.GONE);
    }
    
    private void refresh() {
        viewModel.loadFiles(viewModel.getCurrentFolderId());
    }
    
    private void showDownloadDirectoryInfo() {
        String downloadPath = viewModel.getDownloadDirectoryPath();
        Toast.makeText(this, "Files will be downloaded to: " + downloadPath, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (viewModel.getCanNavigateToParent().getValue() != null && viewModel.getCanNavigateToParent().getValue()) {
            viewModel.navigateToParent();
        } else {
            super.onBackPressed();
        }
    }
} 