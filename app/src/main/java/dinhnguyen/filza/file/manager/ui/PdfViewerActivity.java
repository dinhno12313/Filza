package dinhnguyen.filza.file.manager.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.constants.PdfConstants;
import dinhnguyen.filza.file.manager.handlers.PdfExportHandler;
import dinhnguyen.filza.file.manager.listener.PageClickListener;
import dinhnguyen.filza.file.manager.manager.PdfManager;
import dinhnguyen.filza.file.manager.ui.adapter.PdfPageAdapter;
import dinhnguyen.filza.file.manager.ui.viewmodel.PdfViewerViewModel;
import dinhnguyen.filza.file.manager.viewmodel.PdfViewerViewModelFactory;

public class PdfViewerActivity extends AppCompatActivity implements PageClickListener {
    
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PdfPageAdapter pdfPageAdapter;
    
    private PdfViewerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        
        initializeManagers();
        initializeViews();
        setupObservers();
        setupClickListeners();
        
        loadPdfFile();
    }

    private void initializeManagers() {
        PdfManager pdfManager = new PdfManager(this);
        PdfExportHandler exportHandler = new PdfExportHandler(this);
        
        PdfViewerViewModelFactory factory = new PdfViewerViewModelFactory(pdfManager, exportHandler);
        viewModel = new ViewModelProvider(this, factory).get(PdfViewerViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("PDF Viewer");
        }

        recyclerView = findViewById(R.id.recyclerViewPdfPages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        pdfPageAdapter = new PdfPageAdapter();
        recyclerView.setAdapter(pdfPageAdapter);
    }

    private void setupObservers() {
        viewModel.getPdfTitle().observe(this, title -> {
            if (getSupportActionBar() != null && title != null) {
                getSupportActionBar().setTitle(title);
            }
        });
        
        viewModel.getPdfPages().observe(this, pages -> {
            if (pages != null) {
                pdfPageAdapter.setPdfPages(pages);
            }
        });
        
        viewModel.getIsLoading().observe(this, isLoading -> {
            // TODO: Show/hide loading indicator
        });
        
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showToast(error);
                viewModel.clearMessages();
            }
        });
        
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null) {
                showToast(message);
                viewModel.clearMessages();
            }
        });
    }

    private void setupClickListeners() {
        pdfPageAdapter.setPageClickListener(this);
    }

    private void loadPdfFile() {
        String filePath = getIntent().getStringExtra(PdfConstants.EXTRA_FILE_PATH);
        viewModel.loadPdfFile(filePath);
    }

    @Override
    public void onPageClicked(int position) {
        String message = String.format(PdfConstants.UI_PAGE_CLICKED, position + 1);
        showToast(message);
    }

    @Override
    public void onPageLongClicked(int position) {
        // Handle long click - could show context menu
        showToast("Long click on page " + (position + 1));
    }

    @Override
    public void onPageExportRequested(int position) {
        viewModel.exportPage(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_export_all) {
            viewModel.exportAllPages();
            return true;
        } else if (id == R.id.action_fit_to_screen) {
            pdfPageAdapter.fitAllPagesToScreen();
            showToast("All pages fit to screen");
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (pdfPageAdapter != null) {
            pdfPageAdapter.cleanup();
        }
    }
} 