package dinhnguyen.filza.file.manager.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.ui.view.DrawingImageView;
import dinhnguyen.filza.file.manager.ui.manager.ColorPickerDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;

public class ImageViewerActivity extends AppCompatActivity implements ColorPickerDialog.ColorPickerListener {

    private static final String EXTRA_FILE_PATH = "filePath";
    
    private DrawingImageView drawingImageView;
    private MaterialButton btnDraw;
    private MaterialButton btnHighlight;
    private MaterialButton btnEraser;
    private MaterialButton btnZoom;
    private MaterialButton btnReset;
    private MaterialButton btnSave;
    
    private DrawingImageView.DrawingMode currentMode = DrawingImageView.DrawingMode.ZOOM;
    private DrawingImageView.DrawingMode previousMode = DrawingImageView.DrawingMode.ZOOM;
    private String originalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        
        initializeViews();
        setupToolbar();
        handleImageDisplay();
    }

    private void initializeViews() {
        drawingImageView = findViewById(R.id.drawingImageView);
        btnDraw = findViewById(R.id.btnDraw);
        btnHighlight = findViewById(R.id.btnHighlight);
        btnEraser = findViewById(R.id.btnEraser);
        btnZoom = findViewById(R.id.btnZoom);
        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);
    }
    
    private void setupToolbar() {
        // Set up toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        
        // Set up button click listeners
        btnDraw.setOnClickListener(v -> {
            handleModeButtonClick(DrawingImageView.DrawingMode.DRAW);
        });
        
        btnHighlight.setOnClickListener(v -> {
            handleModeButtonClick(DrawingImageView.DrawingMode.HIGHLIGHT);
        });
        
        btnEraser.setOnClickListener(v -> {
            handleModeButtonClick(DrawingImageView.DrawingMode.ERASER);
        });
        
        btnZoom.setOnClickListener(v -> {
            handleModeButtonClick(DrawingImageView.DrawingMode.ZOOM);
        });
        
        btnReset.setOnClickListener(v -> {
            drawingImageView.resetTransform();
            Toast.makeText(this, "Đã đặt lại chế độ xem", Toast.LENGTH_SHORT).show();
        });
        
        btnSave.setOnClickListener(v -> {
            saveImage();
        });
        
        // Set initial button states
        updateButtonStates();
    }
    
    private void handleModeButtonClick(DrawingImageView.DrawingMode mode) {
        if (currentMode == mode && (mode == DrawingImageView.DrawingMode.DRAW || mode == DrawingImageView.DrawingMode.HIGHLIGHT)) {
            // If the same mode is clicked and it's draw or highlight, show color picker
            showColorPicker(mode);
        } else {
            // Otherwise, switch to the new mode
            setDrawingMode(mode);
            updateButtonStates();
        }
    }
    
    private void showColorPicker(DrawingImageView.DrawingMode mode) {
        int currentColor;
        if (mode == DrawingImageView.DrawingMode.DRAW) {
            currentColor = getCurrentDrawColor();
        } else {
            currentColor = getCurrentHighlightColor();
        }
        
        ColorPickerDialog dialog = ColorPickerDialog.newInstance(currentColor);
        dialog.show(getSupportFragmentManager(), "color_picker");
    }
    
    private int getCurrentDrawColor() {
        return drawingImageView.getDrawColor();
    }
    
    private int getCurrentHighlightColor() {
        return drawingImageView.getHighlightColor();
    }
    
    @Override
    public void onColorSelected(int color) {
        if (currentMode == DrawingImageView.DrawingMode.DRAW) {
            drawingImageView.setDrawColor(color);
            Toast.makeText(this, "Đã thay đổi màu vẽ", Toast.LENGTH_SHORT).show();
        } else if (currentMode == DrawingImageView.DrawingMode.HIGHLIGHT) {
            drawingImageView.setHighlightColor(color);
            Toast.makeText(this, "Đã thay đổi màu đánh dấu", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setDrawingMode(DrawingImageView.DrawingMode mode) {
        previousMode = currentMode;
        currentMode = mode;
        drawingImageView.setDrawingMode(mode);
        
        String modeText = "";
        switch (mode) {
            case DRAW:
                modeText = "Chế độ vẽ";
                break;
            case HIGHLIGHT:
                modeText = "Chế độ đánh dấu";
                break;
            case ZOOM:
                modeText = "Chế độ phóng to";
                break;
            case ERASER:
                modeText = "Chế độ xóa";
                break;
        }
        Toast.makeText(this, modeText, Toast.LENGTH_SHORT).show();
    }
    
    private void updateButtonStates() {
        // Reset all button backgrounds
        btnDraw.setBackgroundTintList(null);
        btnHighlight.setBackgroundTintList(null);
        btnEraser.setBackgroundTintList(null);
        btnZoom.setBackgroundTintList(null);
        
        // Highlight the active button
        switch (currentMode) {
            case DRAW:
                btnDraw.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_light)));
                break;
            case HIGHLIGHT:
                btnHighlight.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_light)));
                break;
            case ZOOM:
                btnZoom.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_light)));
                break;
            case ERASER:
                btnEraser.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_light)));
                break;
        }
    }

    private void handleImageDisplay() {
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        
        if (filePath == null) {
            showToast("Không nhận được đường dẫn ảnh");
            finish();
            return;
        }

        originalFilePath = filePath; // Store the original file path
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            showToast("Tệp không tồn tại");
            return;
        }

        if (!isImageFile(imageFile.getName())) {
            showToast("Tệp không phải là ảnh");
            return;
        }

        displayImage(filePath);
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".jpg") || 
               lowerFileName.endsWith(".jpeg") || 
               lowerFileName.endsWith(".png") || 
               lowerFileName.endsWith(".gif") || 
               lowerFileName.endsWith(".bmp") || 
               lowerFileName.endsWith(".webp");
    }

    private void displayImage(String filePath) {
        try {
            // Get screen dimensions
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            
            // First, decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            
            if (options.outWidth == -1 || options.outHeight == -1) {
                showToast("Không thể đọc thông tin ảnh");
                return;
            }

            // Calculate inSampleSize to avoid OutOfMemoryError
            // Use screen dimensions to determine appropriate sample size
            options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);
            options.inJustDecodeBounds = false;
            
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            if (bitmap != null) {
                drawingImageView.setBitmap(bitmap);
            } else {
                showToast("Không thể tải ảnh");
            }
        } catch (OutOfMemoryError e) {
            showToast("Ảnh quá lớn, không thể hiển thị");
        } catch (Exception e) {
            showToast("Lỗi khi tải ảnh: " + e.getMessage());
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveImage() {
        if (originalFilePath == null) {
            showToast("Không có đường dẫn tệp gốc");
            return;
        }
        
        Bitmap editedBitmap = drawingImageView.getEditedBitmap();
        if (editedBitmap == null) {
            showToast("Không có ảnh để lưu");
            return;
        }
        
        try {
            File originalFile = new File(originalFilePath);
            String extension = getFileExtension(originalFile.getName());
            CompressFormat format = getCompressFormat(extension);
            
            FileOutputStream out = new FileOutputStream(originalFile);
            editedBitmap.compress(format, 90, out);
            out.flush();
            out.close();
            
            showToast("Đã lưu ảnh thành công");
        } catch (IOException e) {
            showToast("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private CompressFormat getCompressFormat(String extension) {
        switch (extension) {
            case "png":
                return CompressFormat.PNG;
            case "webp":
                return CompressFormat.WEBP;
            case "jpg":
            case "jpeg":
            default:
                return CompressFormat.JPEG;
        }
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
        // Check if there are unsaved changes
        // For now, just finish the activity
        super.onBackPressed();
        finish();
    }
} 