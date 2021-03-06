package com.google.example.fridgefriend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/** This Class is used to invoke a SurfaceView with a camera.
 *  When the SurfaceView is toggled on, then the camera preview will pick up the barcode or QR code
 *  and return that code to the calling activity.
 *
 * NOTE: Request Permissions for camera access before calling this class
 *
 */
public class CameraView extends AppCompatActivity {

    public static final int QR_SCAN = 4;
    private static final int BARCODE_REQUEST = 11;
    static final int CODE = 10;

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;

    private Button cameraButton;

    private boolean isSetup;
    private boolean isScanning;

    private EditText editText;
    private Button enterKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameraview);
        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        cameraView.setVisibility(View.INVISIBLE);

        cameraButton = (Button) findViewById(R.id.scan_code);
        isScanning = false;

        final Activity activity = this;
        setupSurface();
        editText = findViewById(R.id.edits);
        boolean isQrScan = getIntent().getExtras().getBoolean("isQrScan");
        if(isQrScan){
            editText.setHint("Enter Friend Code");
            cameraButton.setText(R.string.scanfriendcode_hint);
        }else{
            editText.setHint("Enter Food Item");
            cameraButton.setText(R.string.scanbarcode_hint);
        }
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED){
                    //toggle the Surfaceview wwhen button pressed.
                    if(isScanning){
                        cameraView.setVisibility(View.INVISIBLE);
                        isScanning = false;
                    }
                    else{
                        cameraView.setVisibility(View.VISIBLE);
                        isScanning = true;
                    }
                    //Toast.makeText(activity, "Permission allowed", Toast.LENGTH_SHORT).show();//for testing purposes
                }else{
                    Toast.makeText(activity, "Camera permission denied\nGo into app permissions to allow access to the camera", Toast.LENGTH_SHORT).show();
                }

            }
        });



       editText = findViewById(R.id.edits);
       enterKey = findViewById(R.id.enter);

        enterKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("Product", editText.getText().toString());
                setResult(FridgeList.MANUAL_ENTRY, returnIntent);
                finish();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSetup) {
            cameraSource.release();
            barcodeDetector.release();
        }
    }

    /**
     * sets isSetup to True
     * instantiates cameraView , barcodeDetector and cameraView
     */
    private void setupSurface(){
        isSetup = true;
        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)//All_FORMATS
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    //noinspection MissingPermission
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                System.out.println("Changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections detections) {
                final SparseArray barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("barcodeTag", (Parcelable)barcodes.valueAt(0));
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

}






