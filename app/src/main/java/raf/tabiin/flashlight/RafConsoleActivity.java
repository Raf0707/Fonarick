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

        b.promoCode.setOnClickListener(v -> {
            if (b.promoInput.getText().toString().equals("rafgpt")
                    || b.promoInput.getText().toString().equals("raf")
                    || b.promoInput.getText().toString().equals("otkl")
                    || b.promoInput.getText().toString().equals("fonar")
                    || b.promoInput.getText().toString().equals("iBa")
                    || b.promoInput.getText().toString().equals("iBremus")
                    || b.promoInput.getText().toString().equals("iBagram")) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("promoCode", true);
                startActivity(intent);

                Snackbar.make(b.getRoot(),
                                "Промокод активирован",
                                Snackbar.LENGTH_LONG)
                        .setAction("Ура", v1 -> {
                            Toast.makeText(this, "Долгое нажатие на on или off сразу перекинет в телеграм", Toast.LENGTH_LONG).show();
                        }).show();
            } else if (b.promoInput.getText().toString().equals("yarcost")
                    || b.promoInput.getText().toString().equals("bright")
                    || b.promoInput.getText().toString().equals("slider")
                    || b.promoInput.getText().toString().equals("vim")
                    || b.promoInput.getText().toString().equals("qwert")
                    || b.promoInput.getText().toString().equals("ghjasd")) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.putExtra("promoCode", true);
                startActivity(intent);

                Snackbar.make(b.getRoot(),
                                "Промокод активирован",
                                Snackbar.LENGTH_LONG)
                        .setAction("Ура", v1 -> {
                            Toast.makeText(this, "Долгое нажатие на on или off сразу перекинет в телеграм", Toast.LENGTH_LONG).show();
                        }).show();

            } else {
                Snackbar.make(b.getRoot(),
                                "Не то ввёл",
                                Snackbar.LENGTH_LONG)
                        .setAction("Ну ёлы палы", v1 -> {
                            Toast.makeText(this, "Ну а шо поделать...", Toast.LENGTH_LONG).show();
                        }).show();
            }
        });
    }
}