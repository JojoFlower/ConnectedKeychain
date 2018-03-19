package com.jojoflower.connectedkeychain;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ImageView imageView2;
    Button startScanningButton;
    Button stopScanningButton;
    Bitmap Barrelow;
    Bitmap Barremedium;
    Bitmap Barremax;
    TextView peripheralTextView;
    MediaPlayer biplow;
    MediaPlayer bipmedium;
    MediaPlayer biphigh;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        biplow = MediaPlayer.create(MainActivity.this, R.raw.biplow);
        bipmedium = MediaPlayer.create(MainActivity.this, R.raw.bipmedium);
        biphigh = MediaPlayer.create(MainActivity.this, R.raw.biphigh);
        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        Barrelow = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.barrelow);
        Log.i("tag", String.valueOf(Barrelow));
        Barremedium = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.barremedium);
        Barremax = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.barremax);
        imageView2.setImageBitmap(Barremax);
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

                String nom = result.getDevice().getName();
                peripheralTextView.setText("Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
                if ((-85 > result.getRssi()) && biplow.isLooping() == false) {
                    if (bipmedium.isLooping() == true) {
                        bipmedium.setLooping(false);
                        //bipmedium.stop();
                    }
                    if (biphigh.isLooping() == true) {
                        biphigh.setLooping(false);
                    }
                    imageView2.setImageBitmap(Barrelow);
                    biplow.setLooping(true);
                    biplow.start();
                }
                if (-85 < result.getRssi() && result.getRssi() < -70 && bipmedium.isLooping() == false) {
                    if (biplow.isLooping() == true) {
                        biplow.setLooping(false);
                    }
                    if (biphigh.isLooping() == true) {
                        biphigh.setLooping(false);
                    }
                    imageView2.setImageBitmap(Barremedium);
                    bipmedium.setLooping(true);
                    bipmedium.start();
                }
                if (-70 < result.getRssi() && !biphigh.isLooping()) {
                    if (bipmedium.isLooping() == true) {
                        bipmedium.setLooping(false);
                    }
                    if (biplow.isLooping() == true) {
                        biplow.setLooping(false);
                    }
                    imageView2.setImageBitmap(Barremax);
                    biphigh.setLooping(true);
                    biphigh.start();
                }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {


        System.out.println("start scanning");
        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        if (biplow.isLooping()==true) {
            biplow.setLooping(false);
            biplow.stop();
        }

        if (bipmedium.isLooping()==true) {
            bipmedium.setLooping(false);
            bipmedium.stop();
        }
        if (biphigh.isLooping()==true) {
            biphigh.setLooping(false);
            biphigh.stop();
        }
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }
}

