package com.example.opencvsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.core.CvType.CV_32F;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {
    private CameraBridgeViewBase m_cameraView;


    static {
        System.loadLibrary("opencv_java4");
    }

    public static void permitCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permitCamera(this);
        m_cameraView = findViewById(R.id.camera_view);
        m_cameraView.setCvCameraViewListener(this);
        m_cameraView.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    Mat prevInputFrame;
    Mat prevResult;

    int startGC = 0;

    @Override
    public synchronized Mat onCameraFrame(Mat inputFrame) {
//        try {
//            Thread.sleep(4l);
//        } catch (InterruptedException e) {
//            // ignore
//            e.printStackTrace();
//        }
        Mat diff =  createDiff(inputFrame);
        Mat added = add(diff);
        Mat result = reduce(added);
        prevResult = result;

        startGC--;
        if (startGC < 0) {
            System.gc();
            System.runFinalization();
            startGC=100;
        }

        return result;
    }

    private Mat reduce(Mat added) {
        Mat result = new Mat();

        Core.addWeighted(added, 0.8, prevResult, 0.1, 0, result);
        return result;
    }

    private Mat add(Mat diff) {
        Mat result = new Mat();
        if (prevResult == null){
            prevResult = diff;
        }

        Core.add(prevResult, diff, result);
        return result;
    }


    private Mat createDiff(Mat inputFrame) {
        if (prevInputFrame == null) {
            prevInputFrame = inputFrame;
            return inputFrame;
        } else {
            Mat result = new Mat();
            Core.absdiff(inputFrame, prevInputFrame , result);
            prevInputFrame = inputFrame;
            return result;
        }
    }

}
