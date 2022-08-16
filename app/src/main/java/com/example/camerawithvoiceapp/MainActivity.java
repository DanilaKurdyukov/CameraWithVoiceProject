package com.example.camerawithvoiceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.GridLayoutAnimationController;

import com.example.camerawithvoiceapp.utils.RecordingThread;
import com.example.camerawithvoiceapp.utils.WaveSoundView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_CODE = 100;

    private RecordingThread recordingThread;
    private WaveSoundView waveSoundView;


    CameraManager cameraManager;

    private TextureView previewForm;
    private FloatingActionButton btnFlipCamera;

    private String cameraLensFacing = "0";

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    private CaptureRequest.Builder mBuilder;

    StreamConfigurationMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waveSoundView= findViewById(R.id.waveSoundView);
        previewForm = findViewById(R.id.cameraPreviewForm);
        btnFlipCamera = findViewById(R.id.fab_flipCamera);

        btnFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCamera();
            }
        });
    }

    private void flipCamera() {
        if (cameraLensFacing.equals("0")){
            finishCamera();
            cameraLensFacing = "1";
            startCamera(cameraLensFacing,previewForm.getWidth(), previewForm.getHeight());
        }
        else{
            finishCamera();
            cameraLensFacing = "0";
            startCamera(cameraLensFacing,previewForm.getWidth(), previewForm.getHeight());
        }
    }

    private void finishCamera() {
    }


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            try {
                createCameraPreview();
            }
            catch (Exception e){
                e.getMessage();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = previewForm.getSurfaceTexture();
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            surfaceTexture.setDefaultBufferSize(metrics.widthPixels/2,metrics.heightPixels/2);
            Surface surface = new Surface(surfaceTexture);
            mBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice==null){
                        return;
                    }
                    mCaptureSession = session;
                    try {
                        if (mCameraDevice == null) {
                            return;
                        }
                        mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                        mCaptureSession.setRepeatingRequest(mBuilder.build(), null, null);
                    }
                    catch (Exception e){
                        e.getMessage();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },null);
        }
        catch (Exception e){
            e.getMessage();
        }
    }


    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            startCamera(cameraLensFacing,width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private void startCamera(String cameraLensFacing, int width, int height) {
        try {

            /* setUpCameraOutputs(width,height);*/

            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraLensFacing);

            map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA},100);
            }
            else{
                cameraManager.openCamera(cameraLensFacing, stateCallback, null);
            }

        }
        catch (Exception e){
            e.getMessage();
        }
    }


    private boolean hasPermissions(){
        return checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions()){
            recordingThread = new RecordingThread(getApplicationContext(),waveSoundView);
            recordingThread.start();
            if (previewForm.isAvailable()){
                startCamera(cameraLensFacing,previewForm.getWidth(),previewForm.getHeight());
            }
            else{
                previewForm.setSurfaceTextureListener(surfaceTextureListener);
            }
        }
        else{
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordingThread!=null&&mCameraDevice!=null){
            recordingThread.stopRunning();
            recordingThread=null;
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSIONS_REQUEST_CODE){
            if (grantResults!=null){
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    startCamera(cameraLensFacing,previewForm.getWidth(), previewForm.getHeight());
                    recordingThread = new RecordingThread(getApplicationContext(),waveSoundView);
                    recordingThread.start();
                }
            }
        }
    }
}