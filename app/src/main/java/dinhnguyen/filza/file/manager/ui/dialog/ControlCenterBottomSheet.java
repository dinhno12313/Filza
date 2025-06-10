package dinhnguyen.filza.file.manager.ui.dialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dinhnguyen.filza.file.manager.R;

public class ControlCenterBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ControlCenterBottomSheet";

    public static ControlCenterBottomSheet newInstance() {
        return new ControlCenterBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_control_center, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        // Sign Up Section
        view.findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            // TODO: Implement sign up functionality
            Toast.makeText(requireContext(), "Sign Up functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Cloud Connections Section
        view.findViewById(R.id.btnGoogleDrive).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), dinhnguyen.filza.file.manager.ui.GoogleDriveActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btnDropbox).setOnClickListener(v -> {
            // TODO: Implement Dropbox connection
            Toast.makeText(requireContext(), "Dropbox connection coming soon!", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnOneDrive).setOnClickListener(v -> {
            // TODO: Implement OneDrive connection
            Toast.makeText(requireContext(), "OneDrive connection coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Support Section
        view.findViewById(R.id.btnHelpSupport).setOnClickListener(v -> {
            // TODO: Implement help and support
            Toast.makeText(requireContext(), "Help & Support coming soon!", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnRateApp).setOnClickListener(v -> {
            openAppStore();
        });

        // Social Section
        view.findViewById(R.id.btnFacebook).setOnClickListener(v -> {
            openFacebook();
        });

        view.findViewById(R.id.btnInstagram).setOnClickListener(v -> {
            openInstagram();
        });

        // Footer Links
        view.findViewById(R.id.btnPrivacyNotice).setOnClickListener(v -> {
            openPrivacyNotice();
        });

        view.findViewById(R.id.btnTermsOfService).setOnClickListener(v -> {
            openTermsOfService();
        });

        view.findViewById(R.id.btnLegalNotes).setOnClickListener(v -> {
            openLegalNotes();
        });
    }

    private void openAppStore() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + requireContext().getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to Play Store web URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName()));
            startActivity(intent);
        }
    }

    private void openFacebook() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.facebook.com/filzafilemanager"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open Facebook", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInstagram() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.instagram.com/filzafilemanager"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open Instagram", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPrivacyNotice() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://filzafilemanager.com/privacy"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Privacy Notice not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTermsOfService() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://filzafilemanager.com/terms"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Terms of Service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLegalNotes() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://filzafilemanager.com/legal"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Legal Notes not available", Toast.LENGTH_SHORT).show();
        }
    }
} 