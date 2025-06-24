package dinhnguyen.filza.file.manager.ui;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import dinhnguyen.filza.file.manager.R;
import dinhnguyen.filza.file.manager.databinding.ActivityImageViewerBinding;
import dinhnguyen.filza.file.manager.ui.manager.ColorPickerDialog;
import dinhnguyen.filza.file.manager.ui.view.DrawingImageView;
import dinhnguyen.filza.file.manager.ui.viewmodel.ImageViewerViewModel;

public class ImageViewerActivity extends AppCompatActivity implements ColorPickerDialog.ColorPickerListener, View.OnClickListener {

    private static final String EXTRA_FILE_PATH = "filePath";

    private ActivityImageViewerBinding binding; // Sử dụng View Binding
    private ImageViewerViewModel viewModel; // Sử dụng ViewModel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ImageViewerViewModel.class);
        
        setupToolbar();
        setupInitialState();
        setupClickListeners();
        observeViewModel(); // Lắng nghe các thay đổi từ ViewModel

        loadImageFromIntent();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupInitialState() {
        // Lấy màu mặc định từ DrawingImageView và gửi lên ViewModel
        viewModel.setColors(
            binding.drawingImageView.getDrawColor(),
            binding.drawingImageView.getHighlightColor()
        );
    }
    
    private void setupClickListeners() {
        binding.btnDraw.setOnClickListener(this);
        binding.btnHighlight.setOnClickListener(this);
        binding.btnEraser.setOnClickListener(this);
        binding.btnZoom.setOnClickListener(this);
        binding.btnReset.setOnClickListener(this);
        binding.btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnDraw) {
            handleModeClick(DrawingImageView.DrawingMode.DRAW);
        } else if (id == R.id.btnHighlight) {
            handleModeClick(DrawingImageView.DrawingMode.HIGHLIGHT);
        } else if (id == R.id.btnEraser) {
            viewModel.setDrawingMode(DrawingImageView.DrawingMode.ERASER);
        } else if (id == R.id.btnZoom) {
            viewModel.setDrawingMode(DrawingImageView.DrawingMode.ZOOM);
        } else if (id == R.id.btnReset) {
            binding.drawingImageView.resetTransform();
            showToast("Đã đặt lại chế độ xem");
        } else if (id == R.id.btnSave) {
            viewModel.saveImage(binding.drawingImageView.getEditedBitmap());
        }
    }
    
    private void handleModeClick(DrawingImageView.DrawingMode mode) {
        if (viewModel.drawingMode.getValue() == mode) {
            launchColorPicker(mode);
        } else {
            viewModel.setDrawingMode(mode);
        }
    }

    private void observeViewModel() {
        viewModel.drawingMode.observe(this, mode -> {
            binding.drawingImageView.setDrawingMode(mode);
            updateButtonHighlight(mode);
            showModeToast(mode);
        });

        viewModel.imageBitmap.observe(this, bitmap -> {
            if (bitmap != null) {
                binding.drawingImageView.setBitmap(bitmap);
            } else {
                showToast("Không thể giải mã ảnh");
            }
        });

        viewModel.toastMessage.observe(this, this::showToast);

        viewModel.saveResult.observe(this, success -> {
            showToast(success ? "Đã lưu ảnh thành công" : "Lưu ảnh thất bại");
        });
    }

    private void loadImageFromIntent() {
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (filePath == null || filePath.isEmpty()) {
            showToast("Không nhận được đường dẫn ảnh");
            finish();
            return;
        }
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        viewModel.loadImage(filePath, screenWidth, screenHeight);
    }

    private void updateButtonHighlight(DrawingImageView.DrawingMode activeMode) {
        // Đặt lại màu cho tất cả các nút
        int defaultTint = android.R.color.transparent; // Màu mặc định
        binding.btnDraw.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, defaultTint)));
        binding.btnHighlight.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, defaultTint)));
        binding.btnEraser.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, defaultTint)));
        binding.btnZoom.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, defaultTint)));

        // Đặt màu highlight cho nút đang hoạt động
        int highlightColor = R.color.design_default_color_primary; // Màu highlight mới
        MaterialButton activeButton = getButtonForMode(activeMode);
        if (activeButton != null) {
            activeButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, highlightColor)));
        }
    }

    private MaterialButton getButtonForMode(DrawingImageView.DrawingMode mode) {
        switch (mode) {
            case DRAW: return binding.btnDraw;
            case HIGHLIGHT: return binding.btnHighlight;
            case ERASER: return binding.btnEraser;
            case ZOOM: return binding.btnZoom;
            default: return null;
        }
    }

    private void launchColorPicker(DrawingImageView.DrawingMode mode) {
        int currentColor = (mode == DrawingImageView.DrawingMode.DRAW)
                ? viewModel.getCurrentDrawColor()
                : viewModel.getCurrentHighlightColor();
        
        ColorPickerDialog dialog = ColorPickerDialog.newInstance(currentColor);
        dialog.show(getSupportFragmentManager(), "color_picker_dialog");
    }

    @Override
    public void onColorSelected(int color) {
        DrawingImageView.DrawingMode currentMode = viewModel.drawingMode.getValue();
        if (currentMode == DrawingImageView.DrawingMode.DRAW) {
            viewModel.updateDrawColor(color);
            binding.drawingImageView.setDrawColor(color);
        } else if (currentMode == DrawingImageView.DrawingMode.HIGHLIGHT) {
            viewModel.updateHighlightColor(color);
            binding.drawingImageView.setHighlightColor(color);
        }
    }
    
    private void showModeToast(DrawingImageView.DrawingMode mode) {
        String modeText;
        switch (mode) {
            case DRAW: modeText = "Chế độ vẽ"; break;
            case HIGHLIGHT: modeText = "Chế độ đánh dấu"; break;
            case ZOOM: modeText = "Chế độ phóng to"; break;
            case ERASER: modeText = "Chế độ xóa"; break;
            default: modeText = "";
        }
        if (!modeText.isEmpty()) showToast(modeText);
    }

    private void showToast(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                .setTitle("Lưu thay đổi?")
                .setMessage("Bạn có các thay đổi chưa được lưu. Bạn có muốn lưu trước khi thoát?")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    viewModel.saveImage(binding.drawingImageView.getEditedBitmap());
                    // Đợi lưu xong rồi mới thoát
                    viewModel.saveResult.observe(this, success -> {
                        if (success) super.onBackPressed();
                    });
                })
                .setNegativeButton("Không lưu", (dialog, which) -> super.onBackPressed())
                .setNeutralButton("Hủy", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
