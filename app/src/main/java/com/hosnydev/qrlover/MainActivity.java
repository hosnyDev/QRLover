package com.hosnydev.qrlover;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    // view
    private EditText txt_qr;
    private ImageView image_qr;
    private Button save_qr;

    // qr code
    private static final String IMAGE_DIRECTORY = "/QR_LOVER";
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find View
        txt_qr = findViewById(R.id.txt_qr);
        image_qr = findViewById(R.id.image_qr);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txt_qr.getText().toString().trim().isEmpty()) {
                    txt_qr.setError("reacquired data");
                } else {
                    hideKeyboard(MainActivity.this);
                    CreateQRCodMethod(txt_qr.getText().toString().trim());

                }

            }
        });


        save_qr = findViewById(R.id.save_qr);

        save_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                save();

            }
        });
        findViewById(R.id.scan_qr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });
    }

    private void save() {
        if (bitmap != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    // to ask user to reade external storage

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                } else {

                    saveImage(bitmap);

                }

                /**
                 *  implement code for device < Marshmallow
                 */
            } else {

                saveImage(bitmap);
            }


        } else {

            Toast.makeText(MainActivity.this, " error in Image Save in gallery...", Toast.LENGTH_SHORT).show();

        }
    }


    private void scanQRCode() {

        save_qr.setVisibility(View.GONE);
        image_qr.setVisibility(View.GONE);
        bitmap = null;


        /**
         *  Ask User to Open a Camera
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);

            } else {
                /**
                 *  this for version > 5
                 */
                OpenCameraScan();

            }

        } else {

            /**
             *  this for version < 5
             */
            OpenCameraScan();


        }
    }


    private void OpenCameraScan() {

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                .setPrompt(getString(R.string.app_name))
                .setCameraId(0)
                .setOrientationLocked(false)
                .setBeepEnabled(true)
                .setBarcodeImageEnabled(true)
                .initiateScan();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {

            if (result.getContents() == null) {

                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

            } else {

                final String contents = result.getContents();


                if (requestCode == RESULT_OK) {

                    Toast.makeText(this, "RESULT_OK", Toast.LENGTH_SHORT).show();

                } else {
                    AlertDialog.Builder m = new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(contents)
                            .setNegativeButton("cancel", null)
                            .setPositiveButton("search in google", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri uri = Uri.parse("https://www.google.com/search?q=" + contents);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            });
                    m.create();
                    m.show();
                }

            }

        } else {

            super.onActivityResult(requestCode, resultCode, data);

        }
    }


    // Method To Create QR
    private void CreateQRCodMethod(String ValueMessage) {

        try {
            bitmap = TextToImageEncode(ValueMessage);

            image_qr.setVisibility(View.VISIBLE);
            image_qr.setImageBitmap(bitmap);

            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();

            save_qr.setVisibility(View.VISIBLE);

        } catch (Exception e) {

            Toast.makeText(MainActivity.this, "error in create qr code  " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    // method to save image in gallery

    public void saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, txt_qr.getText().toString().trim() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Toast.makeText(this, "Image Is Saved", Toast.LENGTH_SHORT).show();
            f.getAbsolutePath();

        } catch (IOException e1) {
            Toast.makeText(MainActivity.this, "error" + e1, Toast.LENGTH_SHORT).show();

            e1.printStackTrace();
        }

    }

    // method to convert text to image qr code
    private Bitmap TextToImageEncode(String Value) throws Exception {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */

        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix bitMatrix = multiFormatWriter.encode(Value, BarcodeFormat.QR_CODE, 1000, 1000, hintMap);

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

        bitmap = barcodeEncoder.createBitmap(bitMatrix);

        return bitmap;
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}