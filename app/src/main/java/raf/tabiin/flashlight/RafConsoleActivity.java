package raf.tabiin.flashlight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import raf.tabiin.flashlight.databinding.ActivityRafConsoleBinding;
import raf.tabiin.utils.CustomTabUtil;

public class RafConsoleActivity extends AppCompatActivity {
    ActivityRafConsoleBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRafConsoleBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.tgStudio.setOnClickListener(v -> new CustomTabUtil().openCustomTab(this,
                "https://t.me/+OoI8UWDVVm0yMDNi", R.color.black));


    }
}