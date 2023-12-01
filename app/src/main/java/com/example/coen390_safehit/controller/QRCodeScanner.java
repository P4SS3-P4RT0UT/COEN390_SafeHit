package com.example.coen390_safehit.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.coen390_safehit.model.Player;
import com.example.coen390_safehit.view.PlayerProfileActivity;
import com.example.coen390_safehit.view.SettingsActivity;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

public class QRCodeScanner {

    public static void onAttachDeviceClicked(String id, Context context) {
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(context);

        scanner
                .startScan()
                .addOnSuccessListener(
                        barcode -> {
                            // Task completed successfully
                            String rawValue = barcode.getRawValue();
                            Player player = new Player();
                            player.setPid(id);
                            player.setMac(rawValue);
                            PlayerProfileActivity.showImpactButton(context);

                        })
                .addOnCanceledListener(
                        () -> {
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            Log.d("SETTINGS EXCEPTION", "Error ");
                            Toast.makeText(context, "Scanning did not work", Toast.LENGTH_SHORT).show();
                        });
    }
}
