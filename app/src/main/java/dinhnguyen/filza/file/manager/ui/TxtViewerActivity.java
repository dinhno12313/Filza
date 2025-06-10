package dinhnguyen.filza.file.manager.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dinhnguyen.filza.file.manager.R;

public class TxtViewerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "filePath";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB limit
    
    private Toolbar toolbar;
    private LinearLayout searchLayout;
    private EditText searchEditText;
    private MaterialButton btnSearch;
    private MaterialButton btnClear;
    private TextView textContent;
    private TextView textLineNumbers;
    
    private String originalContent = "";
    private String currentSearchQuery = "";
    private List<Integer> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;
    private boolean showLineNumbers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_viewer);
        
        initializeViews();
        setupToolbar();
        setupSearch();
        handleTextFile();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchLayout = findViewById(R.id.searchLayout);
        searchEditText = findViewById(R.id.searchEditText);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        textContent = findViewById(R.id.textContent);
        textLineNumbers = findViewById(R.id.textLineNumbers);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Text Viewer");
        }
    }
    
    private void setupSearch() {
        btnSearch.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });
        
        btnClear.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearch();
        });
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    clearSearch();
                } else if (query.length() >= 2) {
                    performSearch(query);
                }
            }
        });
    }

    private void handleTextFile() {
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        
        if (filePath == null) {
            showToast("Không nhận được đường dẫn file text");
            finish();
            return;
        }

        File textFile = new File(filePath);
        
        if (!textFile.exists()) {
            showToast("File không tồn tại");
            return;
        }

        if (!isTextFile(textFile.getName())) {
            showToast("File không phải là file text");
            return;
        }

        if (textFile.length() > MAX_FILE_SIZE) {
            showToast("File quá lớn (giới hạn 10MB)");
            return;
        }

        loadTextFile(textFile);
    }

    private boolean isTextFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".txt") || 
               lowerFileName.endsWith(".log") || 
               lowerFileName.endsWith(".md") || 
               lowerFileName.endsWith(".json") || 
               lowerFileName.endsWith(".xml") || 
               lowerFileName.endsWith(".csv");
    }

    private void loadTextFile(File textFile) {
        try {
            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(textFile.getName());
            }
            
            // Read file with UTF-8 encoding
            FileInputStream fis = new FileInputStream(textFile);
            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[8192];
            int bytesRead;
            
            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }
            
            reader.close();
            fis.close();
            
            originalContent = content.toString();
            displayContent(originalContent);
            
        } catch (IOException e) {
            showToast("Không thể đọc file: " + e.getMessage());
        }
    }

    private void displayContent(String content) {
        if (showLineNumbers) {
            displayContentWithLineNumbers(content);
        } else {
            textContent.setText(content);
            textLineNumbers.setVisibility(View.GONE);
        }
    }

    private void displayContentWithLineNumbers(String content) {
        String[] lines = content.split("\n", -1);
        StringBuilder numberedContent = new StringBuilder();
        StringBuilder lineNumbers = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            lineNumbers.append(String.format("%4d\n", i + 1));
            numberedContent.append(lines[i]).append("\n");
        }
        
        textLineNumbers.setText(lineNumbers.toString());
        textContent.setText(numberedContent.toString());
        textLineNumbers.setVisibility(View.VISIBLE);
    }

    private void performSearch(String query) {
        if (query.equals(currentSearchQuery)) {
            // Navigate to next result
            navigateToNextResult();
            return;
        }
        
        currentSearchQuery = query;
        searchResults.clear();
        currentSearchIndex = -1;
        
        if (query.isEmpty()) {
            clearSearch();
            return;
        }
        
        // Find all occurrences
        Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalContent);
        
        while (matcher.find()) {
            searchResults.add(matcher.start());
        }
        
        if (searchResults.isEmpty()) {
            showToast("Không tìm thấy: " + query);
        } else {
            showToast("Tìm thấy " + searchResults.size() + " kết quả");
            navigateToNextResult();
        }
    }

    private void navigateToNextResult() {
        if (searchResults.isEmpty()) return;
        
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        highlightSearchResults();
        
        // Scroll to current result
        int position = searchResults.get(currentSearchIndex);
        scrollToPosition(position);
        
        showToast("Kết quả " + (currentSearchIndex + 1) + "/" + searchResults.size());
    }

    private void highlightSearchResults() {
        if (searchResults.isEmpty()) {
            textContent.setText(originalContent);
            return;
        }
        
        SpannableString spannableString = new SpannableString(originalContent);
        
        for (int i = 0; i < searchResults.size(); i++) {
            int start = searchResults.get(i);
            int end = start + currentSearchQuery.length();
            
            int color = (i == currentSearchIndex) ? 
                ContextCompat.getColor(this, R.color.search_current) : 
                ContextCompat.getColor(this, R.color.search_highlight);
            
            spannableString.setSpan(
                new BackgroundColorSpan(color),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        
        textContent.setText(spannableString);
    }

    private void scrollToPosition(int position) {
        // Calculate line number for the position
        String contentBeforePosition = originalContent.substring(0, position);
        int lineNumber = contentBeforePosition.split("\n").length;
        
        // Scroll to the line
        textContent.post(() -> {
            int lineHeight = textContent.getLineHeight();
            int scrollY = (lineNumber - 1) * lineHeight;
            textContent.scrollTo(0, scrollY);
        });
    }

    private void clearSearch() {
        currentSearchQuery = "";
        searchResults.clear();
        currentSearchIndex = -1;
        textContent.setText(originalContent);
        showToast("Đã xóa tìm kiếm");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.txt_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_search) {
            toggleSearch();
            return true;
        } else if (id == R.id.action_line_numbers) {
            toggleLineNumbers();
            return true;
        } else if (id == R.id.action_font_size_increase) {
            increaseFontSize();
            return true;
        } else if (id == R.id.action_font_size_decrease) {
            decreaseFontSize();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void toggleSearch() {
        if (searchLayout.getVisibility() == View.VISIBLE) {
            searchLayout.setVisibility(View.GONE);
            clearSearch();
        } else {
            searchLayout.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
        }
    }

    private void toggleLineNumbers() {
        showLineNumbers = !showLineNumbers;
        displayContent(originalContent);
        showToast(showLineNumbers ? "Đã bật số dòng" : "Đã tắt số dòng");
    }

    private void increaseFontSize() {
        float currentSize = textContent.getTextSize();
        float newSize = Math.min(currentSize + 2, 24);
        textContent.setTextSize(newSize);
        textLineNumbers.setTextSize(newSize);
    }

    private void decreaseFontSize() {
        float currentSize = textContent.getTextSize();
        float newSize = Math.max(currentSize - 2, 10);
        textContent.setTextSize(newSize);
        textLineNumbers.setTextSize(newSize);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 