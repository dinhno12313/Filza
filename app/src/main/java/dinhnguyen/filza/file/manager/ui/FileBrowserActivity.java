package dinhnguyen.filza.file.manager.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.ui.adapter.FileAdapter;
import dinhnguyen.filza.file.manager.handlers.FileHandler;
import dinhnguyen.filza.file.manager.handlers.FileOperationHandler;
import dinhnguyen.filza.file.manager.handlers.ImageFileHandler;
import dinhnguyen.filza.file.manager.manager.DirectoryManager;
import dinhnguyen.filza.file.manager.manager.FileImportManager;
import dinhnguyen.filza.file.manager.manager.FileOpenManager;
import dinhnguyen.filza.file.manager.ui.manager.DialogManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.FileBrowserViewModel;
import dinhnguyen.filza.file.manager.viewmodel.FileBrowserViewModelFactory;
import dinhnguyen.filza.file.manager.constants.FileConstants;
import dinhnguyen.filza.file.manager.ui.dialog.ControlCenterBottomSheet;
import dinhnguyen.filza.file.manager.utils.PermissionManager;

public class FileBrowserActivity extends AppCompatActivity implements FileOperationHandler.Refreshable {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private Toolbar toolbar;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private FloatingActionButton fabImportFile;
    private FileAdapter fileAdapter;
    
    private FileBrowserViewModel viewModel;
    private DialogManager dialogManager;
    private FileOperationHandler fileOperationHandler;
    
    // Multi-select state
    private boolean isMultiSelectMode = false;
    private MaterialButton btnMenu;
    private MaterialButton btnLeft;

    private final ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        viewModel.importFile(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        initializeManagers();
        initializeViewModel();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // Đảm bảo viewModel đã được khởi tạo trước khi kiểm tra quyền
        checkAndRequestPermissions();
    }
    
    private void checkAndRequestPermissions() {
        if (!PermissionManager.hasFileAccessPermissions(this)) {
            if (PermissionManager.shouldShowPermissionRationale(this)) {
                showPermissionRationaleDialog();
            } else {
                requestPermissions();
            }
        } else {
            loadInitialDirectory();
        }
    }
    
    private void showPermissionRationaleDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs file access permission to browse and manage files on your device.")
                .setPositiveButton("Grant Permission", (dialog, which) -> requestPermissions())
                .setNegativeButton("Cancel", (dialog, which) -> showPermissionDeniedMessage())
                .setCancelable(false)
                .show();
    }
    
    private void requestPermissions() {
        String[] permissions = PermissionManager.getRequiredPermissions();
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                loadInitialDirectory();
            } else {
                showPermissionDeniedMessage();
            }
        }
    }
    
    private void showPermissionDeniedMessage() {
        Toast.makeText(this, 
                "File access permission is required to use this app", 
                Toast.LENGTH_LONG).show();
        // You might want to show a dialog explaining why permissions are needed
        // and guide users to settings if they denied permanently
    }

    private void initializeManagers() {
        dialogManager = new DialogManager(this);
        fileOperationHandler = new FileOperationHandler(this, this);
    }

    private void initializeViewModel() {
        DirectoryManager directoryManager = new DirectoryManager(this);
        FileOpenManager fileOpenManager = new FileOpenManager(this, this::refresh);
        FileImportManager fileImportManager = new FileImportManager(this, getFileHandlers());
        
        FileBrowserViewModelFactory factory = new FileBrowserViewModelFactory(
                directoryManager, fileOpenManager, fileImportManager);
        viewModel = new ViewModelProvider(this, factory).get(FileBrowserViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewFiles);
        fabImportFile = findViewById(R.id.fabImportFile);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        btnMenu = findViewById(R.id.btnMenu);
        btnLeft = findViewById(R.id.btnLeft);

        setupMenuButton(btnMenu);
        setupLeftButton(btnLeft);
        setupNavigationButton();
    }

    private void setupMenuButton(MaterialButton btnMenu) {
        btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, btnMenu);
            popupMenu.getMenuInflater().inflate(R.menu.file_browser_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
            popupMenu.show();
        });
    }

    private void setupLeftButton(MaterialButton btnLeft) {
        btnLeft.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                exitMultiSelectMode();
            } else {
                showControlCenter();
            }
        });
    }

    private void setupNavigationButton() {
        toolbar.setNavigationOnClickListener(v -> {
            if (isMultiSelectMode) {
                exitMultiSelectMode();
            } else if (viewModel.canNavigateToParent()) {
                viewModel.navigateToParent();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this, null, fileOperationHandler, this::onFileClicked, 
            new FileAdapter.OnMultiSelectListener() {
                @Override
                public void onSelectionChanged(int selectedCount) {
                    onMultiSelectChanged(selectedCount);
                }
                
                @Override
                public void onMultiSelectModeChanged(boolean isMultiSelectMode) {
                    // Handle mode change if needed
                }
            });
        recyclerView.setAdapter(fileAdapter);
    }

    private void setupObservers() {
        viewModel.getFiles().observe(this, files -> {
            fileAdapter.setFiles(files);
        });
        
        viewModel.getCurrentDirectory().observe(this, directory -> {
            updateToolbarTitle(directory);
            fileOperationHandler.setCurrentDirectory(directory);
        });
    }

    private void setupClickListeners() {
        FloatingActionButton fabCreateFolder = findViewById(R.id.fabCreateFolder);
        fabCreateFolder.setOnClickListener(v -> showCreateFolderDialog());
        fabImportFile.setOnClickListener(v -> openFilePicker());
    }

    private void loadInitialDirectory() {
        // Check if a specific path was passed via intent
        String initialPath = getIntent().getStringExtra("initialPath");
        File initialDirectory;
        
        if (initialPath != null) {
            initialDirectory = new File(initialPath);
            if (!initialDirectory.exists() || !initialDirectory.isDirectory()) {
                // Fallback to default directory if the specified path is invalid
                initialDirectory = getExternalFilesDir(null);
            }
        } else {
            initialDirectory = getExternalFilesDir(null);
        }
        
        // Check if we can access the directory
        if (PermissionManager.canAccessDirectory(this, initialDirectory.getAbsolutePath())) {
            viewModel.loadDirectory(initialDirectory);
        } else {
            // Fallback to app's private directory
            viewModel.loadDirectory(getExternalFilesDir(null));
        }
    }

    private void onFileClicked(File file) {
        if (isMultiSelectMode) {
            // In multi-select mode, clicking toggles selection
            return;
        }
        
        if (file.isDirectory()) {
            viewModel.loadDirectory(file);
        } else {
            viewModel.openFile(file);
        }
    }
    
    private void onMultiSelectChanged(int selectedCount) {
        updateMultiSelectUI(selectedCount);
    }

    private void showCreateFolderDialog() {
        dialogManager.showCreateFolderDialog(folderName -> 
            viewModel.createFolder(folderName));
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        importFileLauncher.launch(intent);
    }

    private void updateToolbarTitle(File directory) {
        if (directory != null) {
            TextView toolbarTitle = findViewById(R.id.toolbarTitle);
            if (toolbarTitle != null) {
                String title = directory.getName();
                // Show "Home" for the app's external files directory
                if (directory.equals(getExternalFilesDir(null))) {
                    title = "Home";
                }
                toolbarTitle.setText(title);
            }
        }
    }
    
    private void updateMultiSelectUI(int selectedCount) {
        if (isMultiSelectMode) {
            btnLeft.setText("Cancel");
            btnMenu.setText(selectedCount > 0 ? "Actions (" + selectedCount + ")" : "Actions");
            btnMenu.setOnClickListener(v -> showMultiSelectActionsMenu(btnMenu));
        } else {
            btnLeft.setText("Left");
            btnMenu.setText("Menu");
            btnMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(this, btnMenu);
                popupMenu.getMenuInflater().inflate(R.menu.file_browser_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
                popupMenu.show();
            });
        }
    }
    
    private void enterMultiSelectMode() {
        isMultiSelectMode = true;
        fileAdapter.setMultiSelectMode(true);
        updateMultiSelectUI(0);
        Toast.makeText(this, FileConstants.MULTI_SELECT_ENTERED, Toast.LENGTH_SHORT).show();
    }
    
    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        fileAdapter.setMultiSelectMode(false);
        updateMultiSelectUI(0);
        Toast.makeText(this, FileConstants.MULTI_SELECT_EXITED, Toast.LENGTH_SHORT).show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select) {
            enterMultiSelectMode();
            return true;
        } else if (id == R.id.action_view_mode_grid) {
            setViewMode(FileAdapter.ViewMode.GRID);
            return true;
        } else if (id == R.id.action_view_mode_list) {
            setViewMode(FileAdapter.ViewMode.LIST);
            return true;
        } else if (id == R.id.action_sort_by_name) {
            setSortMode(FileAdapter.SortMode.NAME);
            return true;
        } else if (id == R.id.action_sort_by_date) {
            setSortMode(FileAdapter.SortMode.DATE);
            return true;
        } else if (id == R.id.action_sort_by_size) {
            setSortMode(FileAdapter.SortMode.SIZE);
            return true;
        }
        return false;
    }
    
    private void setViewMode(FileAdapter.ViewMode viewMode) {
        fileAdapter.setViewMode(viewMode);
        
        if (viewMode == FileAdapter.ViewMode.GRID) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            Toast.makeText(this, FileConstants.VIEW_MODE_GRID, Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            Toast.makeText(this, FileConstants.VIEW_MODE_LIST, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setSortMode(FileAdapter.SortMode sortMode) {
        fileAdapter.setSortMode(sortMode);
        String message;
        switch (sortMode) {
            case NAME:
                message = FileConstants.SORT_BY_NAME;
                break;
            case DATE:
                message = FileConstants.SORT_BY_DATE;
                break;
            case SIZE:
                message = FileConstants.SORT_BY_SIZE;
                break;
            default:
                message = "Sorted";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private List<FileHandler> getFileHandlers() {
        return Arrays.asList(new ImageFileHandler());
    }

    private void showControlCenter() {
        ControlCenterBottomSheet bottomSheet = ControlCenterBottomSheet.newInstance();
        bottomSheet.show(getSupportFragmentManager(), ControlCenterBottomSheet.TAG);
    }

    @Override
    public void refresh() {
        File currentDirectory = viewModel.getCurrentDirectory().getValue();
        if (currentDirectory != null) {
            viewModel.loadDirectory(currentDirectory);
        }
    }

    private void showMultiSelectActionsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.multiselect_actions_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            Set<File> selectedFiles = fileAdapter.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
                return true;
            }
            int id = item.getItemId();
            if (id == R.id.action_multi_zip) {
                batchZipFiles(selectedFiles);
                return true;
            } else if (id == R.id.action_multi_move) {
                batchMoveFiles(selectedFiles);
                return true;
            } else if (id == R.id.action_multi_copy) {
                batchCopyFiles(selectedFiles);
                return true;
            } else if (id == R.id.action_multi_duplicate) {
                batchDuplicateFiles(selectedFiles);
                return true;
            } else if (id == R.id.action_multi_delete) {
                batchDeleteFiles(selectedFiles);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void batchZipFiles(Set<File> files) {
        if (files == null || files.isEmpty()) return;
        // Hỏi tên file zip
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Zip Files");
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter zip file name");
        input.setText("archive.zip");
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String zipName = input.getText().toString().trim();
            if (!zipName.endsWith(".zip")) zipName += ".zip";
            File destDir = fileAdapter.getSelectedFiles().iterator().next().getParentFile();
            File zipFile = new File(destDir, zipName);
            fileOperationHandler.zipMultipleFiles(files, zipFile,
                () -> runOnUiThread(() -> {
                    Toast.makeText(this, "Zipped to " + zipFile.getName(), Toast.LENGTH_SHORT).show();
                    exitMultiSelectMode();
                    loadInitialDirectory();
                }),
                () -> runOnUiThread(() -> exitMultiSelectMode())
            );
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void batchDeleteFiles(Set<File> files) {
        if (files == null || files.isEmpty()) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Files")
            .setMessage("Are you sure you want to delete all selected files and folders?")
            .setPositiveButton("Delete", (dialog, which) -> {
                for (File file : files) {
                    fileOperationHandler.onDelete(file);
                }
                exitMultiSelectMode();
                loadInitialDirectory();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void batchMoveFiles(Set<File> files) {
        if (files.isEmpty()) return;
        File anyFile = files.iterator().next();
        new dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog(this, anyFile.getParentFile(), new dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog.FolderPickerCallback() {
            @Override
            public void onFolderSelected(File destFolder) {
                for (File file : files) {
                    fileOperationHandler.onMove(file);
                }
                exitMultiSelectMode();
                loadInitialDirectory();
            }
            @Override
            public void onCancelled() {}
        }).show();
    }

    private void batchCopyFiles(Set<File> files) {
        if (files.isEmpty()) return;
        File anyFile = files.iterator().next();
        new dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog(this, anyFile.getParentFile(), new dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog.FolderPickerCallback() {
            @Override
            public void onFolderSelected(File destFolder) {
                for (File file : files) {
                    fileOperationHandler.onCopy(file);
                }
                exitMultiSelectMode();
                loadInitialDirectory();
            }
            @Override
            public void onCancelled() {}
        }).show();
    }

    private void batchDuplicateFiles(Set<File> files) {
        for (File file : files) {
            fileOperationHandler.onDuplicate(file);
        }
        exitMultiSelectMode();
    }
}

