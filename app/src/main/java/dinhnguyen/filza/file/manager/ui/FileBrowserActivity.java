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
import java.util.ArrayList;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.ui.adapter.FileAdapter;
import dinhnguyen.filza.file.manager.handlers.FileHandler;
import dinhnguyen.filza.file.manager.handlers.FileOperationHandler;
import dinhnguyen.filza.file.manager.handlers.ImageFileHandler;
import dinhnguyen.filza.file.manager.manager.BulkOperationsManager;
import dinhnguyen.filza.file.manager.manager.DirectoryManager;
import dinhnguyen.filza.file.manager.manager.FileImportManager;
import dinhnguyen.filza.file.manager.manager.FileOpenManager;
import dinhnguyen.filza.file.manager.ui.manager.DialogManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.FileBrowserViewModel;
import dinhnguyen.filza.file.manager.viewmodel.FileBrowserViewModelFactory;
import dinhnguyen.filza.file.manager.constants.FileConstants;
import dinhnguyen.filza.file.manager.ui.dialog.ControlCenterBottomSheet;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import dinhnguyen.filza.file.manager.ui.dialog.FolderPickerDialog;

public class FileBrowserActivity extends AppCompatActivity implements FileOperationHandler.Refreshable {
    
    private Toolbar toolbar;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private FloatingActionButton fabImportFile;
    private FileAdapter fileAdapter;
    
    private FileBrowserViewModel viewModel;
    private DialogManager dialogManager;
    private FileOperationHandler fileOperationHandler;
    private BulkOperationsManager bulkOperationsManager;
    
    // Multi-select state
    private boolean isMultiSelectMode = false;
    private MaterialButton btnMenu;
    private MaterialButton btnLeft;
    private Set<File> selectedFiles;

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
        
        loadInitialDirectory();
    }

    private void initializeManagers() {
        dialogManager = new DialogManager(this);
        fileOperationHandler = new FileOperationHandler(this, this);
        bulkOperationsManager = new BulkOperationsManager(this, this);
        
        fileOperationHandler.setDestinationFolderPicker(new FileOperationHandler.DestinationFolderPicker() {
            @Override
            public void pickDestination(File source, DestinationCallback callback) {
                showFolderPickerDialog(callback);
            }
        });
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
            if (isMultiSelectMode && selectedFiles != null && !selectedFiles.isEmpty()) {
                // Show bulk operations menu
                showBulkOperationsMenu();
            } else {
                // Show regular menu
                PopupMenu popupMenu = new PopupMenu(this, btnMenu);
                popupMenu.getMenuInflater().inflate(R.menu.file_browser_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
                popupMenu.show();
            }
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
        
        viewModel.loadDirectory(initialDirectory);
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
        
        // Update selected files list
        if (isMultiSelectMode) {
            selectedFiles = fileAdapter.getSelectedFiles();
        }
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
            if (selectedCount > 0) {
                btnMenu.setText("Actions (" + selectedCount + ")");
                btnMenu.setEnabled(true);
            } else {
                btnMenu.setText("Actions");
                btnMenu.setEnabled(false);
            }
        } else {
            btnLeft.setText("Left");
            btnMenu.setText("Menu");
            btnMenu.setEnabled(true);
        }
    }
    
    private void enterMultiSelectMode() {
        isMultiSelectMode = true;
        fileAdapter.setMultiSelectMode(true);
        selectedFiles = null;
        updateMultiSelectUI(0);
        Toast.makeText(this, FileConstants.MULTI_SELECT_ENTERED, Toast.LENGTH_SHORT).show();
    }
    
    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        fileAdapter.setMultiSelectMode(false);
        selectedFiles = null;
        updateMultiSelectUI(0);
        Toast.makeText(this, FileConstants.MULTI_SELECT_EXITED, Toast.LENGTH_SHORT).show();
    }

    private void showBulkOperationsMenu() {
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            List<File> filesList = new ArrayList<>(selectedFiles);
            bulkOperationsManager.showBulkOperationsMenu(filesList);
        } else {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
        }
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

    private void showFolderPickerDialog(FileOperationHandler.DestinationFolderPicker.DestinationCallback callback) {
        File currentDir = viewModel.getCurrentDirectory().getValue();
        if (currentDir == null) {
            Toast.makeText(this, "No current directory", Toast.LENGTH_SHORT).show();
            callback.onDestinationChosen(null);
            return;
        }
        FolderPickerDialog dialog = FolderPickerDialog.newInstance(currentDir, folder -> {
            callback.onDestinationChosen(folder);
        });
        dialog.show(getSupportFragmentManager(), "FolderPickerDialog");
    }

    @Override
    public void refresh() {
        File currentDirectory = viewModel.getCurrentDirectory().getValue();
        if (currentDirectory != null) {
            viewModel.loadDirectory(currentDirectory);
        }
        
        // Clear selection after refresh
        if (isMultiSelectMode) {
            exitMultiSelectMode();
        }
    }
}

