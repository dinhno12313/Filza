package dinhnguyen.filza.file.manager.ui.manager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;

import dinhnguyen.filza.file.manager.R;

public class ColorPickerDialog extends DialogFragment {
    
    public interface ColorPickerListener {
        void onColorSelected(int color);
    }
    
    private ColorPickerListener listener;
    private int currentColor;
    
    // Predefined colors
    private static final int[] PREDEFINED_COLORS = {
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#FFEB3B"), // Yellow
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#795548"), // Brown
        Color.parseColor("#9E9E9E"), // Gray
        Color.parseColor("#000000"), // Black
        Color.parseColor("#FFFFFF")  // White
    };
    
    public static ColorPickerDialog newInstance(int currentColor) {
        ColorPickerDialog dialog = new ColorPickerDialog();
        Bundle args = new Bundle();
        args.putInt("current_color", currentColor);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentColor = getArguments().getInt("current_color", Color.RED);
        }
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ColorPickerListener) {
            listener = (ColorPickerListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ColorPickerListener");
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_color_picker, null);
        
        setupColorButtons(view);
        setupActionButtons(view);
        
        builder.setView(view);
        Dialog dialog = builder.create();
        
        // Remove default title bar
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        
        return dialog;
    }
    
    private void setupColorButtons(View view) {
        // Setup predefined color buttons
        int[] buttonIds = {
            R.id.btnColorRed, R.id.btnColorOrange, R.id.btnColorYellow,
            R.id.btnColorGreen, R.id.btnColorBlue, R.id.btnColorPurple,
            R.id.btnColorPink, R.id.btnColorBrown, R.id.btnColorGray,
            R.id.btnColorBlack, R.id.btnColorWhite
        };
        
        for (int i = 0; i < buttonIds.length && i < PREDEFINED_COLORS.length; i++) {
            MaterialButton button = view.findViewById(buttonIds[i]);
            final int color = PREDEFINED_COLORS[i];
            
            button.setOnClickListener(v -> {
                selectColor(color);
                dismiss();
            });
            
            // Show check mark for current color
            if (color == currentColor) {
                button.setIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_check));
            } else {
                button.setIcon(null);
            }
        }
        
        // Setup custom color button
        MaterialButton customColorButton = view.findViewById(R.id.btnCustomColor);
        customColorButton.setOnClickListener(v -> {
            showCustomColorPicker();
        });
    }
    
    private void setupActionButtons(View view) {
        MaterialButton cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> dismiss());
        
        MaterialButton applyButton = view.findViewById(R.id.btnApply);
        applyButton.setOnClickListener(v -> {
            selectColor(currentColor);
            dismiss();
        });
    }
    
    private void selectColor(int color) {
        if (listener != null) {
            listener.onColorSelected(color);
        }
    }
    
    private void showCustomColorPicker() {
        // For now, we'll use a simple color picker
        // In a production app, you might want to use a more sophisticated color picker
        // or integrate with a third-party library like ColorPickerView
        
        // For simplicity, we'll cycle through some additional colors
        int[] customColors = {
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#607D8B"), // Blue Grey
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#00BCD4")  // Cyan
        };
        
        // Find next color that's not the current one
        for (int color : customColors) {
            if (color != currentColor) {
                selectColor(color);
                dismiss();
                return;
            }
        }
        
        // If all colors are the same, just select the first one
        selectColor(customColors[0]);
        dismiss();
    }
} 