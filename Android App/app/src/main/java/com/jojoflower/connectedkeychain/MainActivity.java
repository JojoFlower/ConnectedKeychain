package com.jojoflower.connectedkeychain;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    MediaPlayer playedbip;
    Timer timer;
    TimerTask task;
    ImageView image;
    Bitmap niveau1;
    Bitmap niveau2;
    Bitmap niveau3;
    Bitmap niveau4;
    Bitmap niveau5;
    Bitmap niveau6;
    Bitmap accueil;
    Bitmap recherche;
    Bitmap trouve;
    Bitmap erreur;
    MediaPlayer mniveau1;
    MediaPlayer mniveau2;
    MediaPlayer mniveau3;
    MediaPlayer mniveau4;
    MediaPlayer mniveau5;
    MediaPlayer mniveau6;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    int rssi1;
    int rssi2;
    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    Button connectToDevice;
    Button disconnectDevice;
    BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 25000;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mniveau1 = MediaPlayer.create(MainActivity.this, R.raw.son1);
        mniveau2 = MediaPlayer.create(MainActivity.this, R.raw.son2);
        mniveau3 = MediaPlayer.create(MainActivity.this, R.raw.son3);
        mniveau4 = MediaPlayer.create(MainActivity.this, R.raw.son4);
        mniveau5 = MediaPlayer.create(MainActivity.this, R.raw.son5);
        mniveau6 = MediaPlayer.create(MainActivity.this, R.raw.son6);
        niveau1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau1);
        niveau2 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau2);
        niveau3 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau3);
        niveau4 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau4);
        niveau5 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau5);
        niveau6 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.niveau6);
        accueil = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.accueil);
        recherche = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.attente);
        trouve = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.trouve);
        erreur = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.erreur);


        image = (ImageView) findViewById(R.id.image);

        image.setImageBitmap(accueil);

        /*peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        deviceIndexInput = (EditText) findViewById(R.id.InputIndex);
        deviceIndexInput.setText("0");*/

        connectToDevice = (Button) findViewById(R.id.ConnectButton);
        connectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToDeviceSelected();
            }
        });

        disconnectDevice = (Button) findViewById(R.id.DisconnectButton);
        disconnectDevice.setVisibility(View.INVISIBLE);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDeviceSelected();
            }
        });

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                image.setImageBitmap(accueil);
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
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

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getAddress().equals("24:0A:C4:01:DE:7A") == true ){ //24:0A:C4:05:B2:7E
                bluetoothGatt = result.getDevice().connectGatt(getApplicationContext(), false, btleGattCallback);
                image.setImageBitmap(trouve);
                stopScanning();
            }


        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, final int rssi1, int rssi2) {

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (rssi1<-100 && mniveau1.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau1;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau1);
                    }
                    if (rssi1>-100 && rssi1<-88 && mniveau2.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau2;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau2);
                    }
                    if (rssi1>88 && rssi1<-75 && mniveau3.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau3;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau3);
                    }
                    if (rssi1>-75 && rssi1<-62 && mniveau4.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau4;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau4);
                    }
                    if (rssi1>-62 && rssi1<-50 && mniveau5.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau5;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau5);
                    }
                    if (rssi1>-50 && mniveau6.isLooping() == false){
                        if (playedbip.isLooping()==true) {
                            playedbip.setLooping(false);
                        }
                        playedbip.pause();
                        playedbip=mniveau6;
                        playedbip.setLooping(true);
                        playedbip.start();
                        image.setImageBitmap(niveau6);
                    }
                }
            });
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("device read or wrote to\n");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device disconnected\n");
                            /*connectToDevice.setVisibility(View.VISIBLE);
                            disconnectDevice.setVisibility(View.INVISIBLE);*/
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device connected\n");
                            /*connectToDevice.setVisibility(View.INVISIBLE);
                            disconnectDevice.setVisibility(View.VISIBLE);*/
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                    break;
            }
        }


        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
        btScanning = true;
        image.setImageBitmap(recherche);
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (btScanning == true){
                image.setImageBitmap(erreur);
                stopScanning();
                }
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        btScanning = false;
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    public void connectToDeviceSelected() {

        timer = new Timer();
        task = new TimerTask(){
            public void run(){
                bluetoothGatt.readRemoteRssi();
            }
        };
        timer.schedule(task,0,300);
        connectToDevice.setVisibility(View.INVISIBLE);
        disconnectDevice.setVisibility(View.VISIBLE);
        playedbip=mniveau1;
    }


    public void disconnectDeviceSelected() {
        timer.cancel();
        playedbip.setLooping(false);
        playedbip.pause();
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        image.setImageBitmap(accueil);
        connectToDevice.setVisibility(View.VISIBLE);
        disconnectDevice.setVisibility(View.INVISIBLE);
    }



    @Override
    public void onStart() {
        super.onStart();

        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.joelwasserman.androidbleconnectexample/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.joelwasserman.androidbleconnectexample/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
