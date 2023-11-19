package com.example.coen390_safehit.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coen390_safehit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private MaterialButton scan_but;
    public TextView Results;

    private ImageView code_image;

    private MaterialButton camera_button;


    private static final int CAMERA_CODE = 100;
    private static final int STORAGE_CODE = 101;


    private String[] camerapermissons;
    private String[] storagepermissons;

    private Uri imageURI;

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;
    private static final String TAG = "MAIN_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scan_but = findViewById(R.id.scan_but);
        Results = findViewById(R.id.Results);
        code_image = findViewById(R.id.code_image);
        camera_button = findViewById(R.id.camera_button);

        camerapermissons = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermissons = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        barcodeScannerOptions = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);


        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraPermission()){
                    ImageCamera();
                }
                else{
                    requestCamPermission();
                }

            }
        });


        scan_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageURI == null) {
                    Toast.makeText(ScanActivity.this, "You first need to pick an image", Toast.LENGTH_SHORT).show();
                }
                    else{
                        getImage();
                    }
                }


        });


    }

    private void getImage() {
        try{
            InputImage inputImage = InputImage.fromFilePath(this,imageURI);

            Task<List<Barcode>> ResultBarcode = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            getinfo(barcodes);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ScanActivity.this, "did not scan properly please retry", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        catch(Exception e){
            Toast.makeText(ScanActivity.this, "did not scan properly please retry", Toast.LENGTH_SHORT).show();

        }
    }

    private void getinfo(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes){
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            String rawValue = barcode.getRawValue();

            Log.d(TAG,"extracted barcode data: " + rawValue);

            int valueType = barcode.getValueType();

            switch (valueType) {
                case Barcode.TYPE_WIFI:
                    String ssid = barcode.getWifi().getSsid();
                    String password = barcode.getWifi().getPassword();
                    int type = barcode.getWifi().getEncryptionType();
                    Log.d(TAG,"ssid is: " +ssid);
                    break;
                case Barcode.TYPE_URL:
                    String title = barcode.getUrl().getTitle();
                    String url = barcode.getUrl().getUrl();
                    break;
            }


        }
    }

    private void ImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);

    }
    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == Activity.RESULT_OK){
                Intent info = o.getData();

                Log.d(TAG, "onActivityResult: imageURI:" + imageURI);

                code_image.setImageURI(imageURI);
            }
            else{
                Toast.makeText(ScanActivity.this, " cancel", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private boolean storagePermission(){

        boolean Storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return Storage_result;
    }

    private void requestStorePermission(){
        ActivityCompat.requestPermissions(this, storagepermissons,STORAGE_CODE);
    }

    private boolean cameraPermission(){
        boolean Camera_result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED;

        boolean Storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return Camera_result && Storage_result;
    }

    private void requestCamPermission(){
        ActivityCompat.requestPermissions(this,camerapermissons,CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

                if (grantResults.length>0){
                    boolean AcceptCamera = grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    boolean AcceptStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(AcceptCamera && AcceptStorage){
                        ImageCamera();
                    }
                    else{
                        Toast.makeText(ScanActivity.this,"need camera permission", Toast.LENGTH_SHORT).show();
                    }
                }



    }





//    private void pickImageGallery(){ //probaly wont need
//        Intent intent =new Intent(Intent.ACTION_PICK);
//
//        intent.setType("image/*");
//        //       galleryActivityResultLauncher.launch(intent);
//    }



}