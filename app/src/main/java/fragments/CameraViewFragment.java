package fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.koobookandroidapp.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.IOException;

import controllers.BookController;
import extras.SoundEffect;

import static com.google.android.gms.vision.barcode.Barcode.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraViewFragment extends Fragment {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textview_scan_msg;
    BarcodeDetector barcodeDetector;
    BookController bookController;
    String isbn;


    public CameraViewFragment() {
        // Required empty public constructor
    }

    //Credit to https://www.youtube.com/watch?v=ej51mAYXbKs for the tutorial on implementation the scanner using Google vision api
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        surfaceView = getView().findViewById(R.id.camera_view);

        textview_scan_msg = getView().findViewById(R.id.textview_scan_msg);

        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector).setRequestedPreviewSize(640, 480).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                } try{
                    cameraSource.start(holder);
                }catch(IOException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }


            //This overriden method will detect the barcode and store the ISBN number it in a shared preference file for future use. After storing it,
            //the Book Controller is used to search the book using the detected ISBN number.
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> isbnCodes = detections.getDetectedItems();
                if(isbnCodes.size() != 0){
                    textview_scan_msg.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();
                            bookController = new BookController(getContext());
                            SoundEffect soundEffect = new SoundEffect();
                            soundEffect.play(getActivity().getApplicationContext());
                            Vibrator vibrator = (Vibrator) getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            isbn = isbnCodes.valueAt(0).displayValue;
                            bookController.storeBookIsbn(getContext(), isbn);
                            bookController.searchBook(isbn,"","");
                        }
                    });
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_view, container, false);
    }

}
