package com.androidexam.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String DEFAULT_CARD_ID = "12345665000000041270";
    private static final String DEFAULT_PIN = "1234";
    private static final String DEFAULT_TABLE = "";
    private static final String DEFAULT_SEAT = "";
    private static final String DEFAULT_SESSION_ID = "";
    private static final String DEFAULT_SERVICE_ID = "";

    private static String CARD_ID = DEFAULT_CARD_ID;
    private static String PIN = DEFAULT_PIN;
    private static String TABLE = DEFAULT_TABLE;
    private static String SEAT = DEFAULT_SEAT;
    private static String SESSION_ID = DEFAULT_SESSION_ID;
    private static String SERVICE_ID = DEFAULT_SERVICE_ID;

    private Button connectButton;
    private Button disconnectButton;
    private  Button sendButton;
    private ScrollView logScroller;
    private TextView logViewer;
    private Spinner spinner;
    private BluetoothAdapter bluetoothAdapter;
    private static final int BLE_RESULT = 0;
    private static final int BLUETOOTH_SCAN_PERMISSION_REQUEST = 123;

    private BluetoothLeScanner bluetoothLeScanner;
    private static final int CALIBRATION_DIFF = 20; // Ngưỡng Auto-Calibration

    private static final int BLUETOOTH_PERMISSION_REQUEST = 1;
    private boolean isScanning = false;


    private HashMap<String, BaseOptions> optionsMap = new java.util.HashMap<>();

    private BaseOptions[] optionsList = new BaseOptions[] {
            new CardInOptions(),
            new CardOutOptions()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);



        connectButton = findViewById(R.id.connect_button);
        logViewer = (TextView)findViewById(R.id.log_view);
        disconnectButton = findViewById(R.id.disconnect_button);
        sendButton = (Button) findViewById(R.id.send_button);
        spinner = (Spinner)findViewById(R.id.eventSpinner);
        logScroller = (ScrollView) findViewById(R.id.log_scroller);

        setUpMap();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getOptionNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                RelativeLayout optionsLayout = (RelativeLayout)findViewById(R.id.eventOptionsLayout);
                String selected = (String)adapterView.getItemAtPosition(position);
                BaseOptions options = optionsMap.get(selected);
                options.bind(optionsLayout, getLayoutInflater());
                options.setFieldValue(R.id.cardid, prefs.getString("cardId",CARD_ID));
                options.setFieldValue(R.id.table,prefs.getString("table", TABLE));
                options.setFieldValue(R.id.seat,prefs.getString("seat",  SEAT));
                options.setFieldValue(R.id.sessionid, prefs.getString("sessionId",SESSION_ID));
                options.setFieldValue(R.id.serviceid,prefs.getString("serviceId",  SERVICE_ID));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (!hasBluetoothPermission()) {
            requestBluetoothPermission();
        } else {
            checkBLEStatus();
        }
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    long currentTimeMillis = System.currentTimeMillis();
                    Date currentDate = new Date(currentTimeMillis);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    String formattedDateTime = sdf.format(currentDate);
                    logViewer.append("["+formattedDateTime+"]"+"\n");
                    startScan();
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                } else {
                    stopScan();
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IEvent event = getEvent();
                logScroller.fullScroll(View.FOCUS_DOWN);
                long currentTimeMillis = System.currentTimeMillis();
                Date currentDate = new Date(currentTimeMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                String formattedDateTime = sdf.format(currentDate);
                logViewer.append("["+formattedDateTime+"]"+"\n");
                logViewer.append("Sending "+event.toString()+"\n");

            }
        });



    }

    private IEvent getEvent() {
        String selected = (String)spinner.getSelectedItem();
        BaseOptions options = optionsMap.get(selected);
        String carId = options.getFieldValue(R.id.cardid);
        if(carId != null){
            prefs.edit().putString("cardId",carId).apply();
        }

        String serviceId = options.getFieldValue(R.id.serviceid);
        if(serviceId!=null){
            prefs.edit().putString("serviceId",serviceId).apply();
        }
        return  options.getEvent();

    }

    private void checkBLEStatus() {
        if (bluetoothAdapter == null) {
            showToast("BLE is not supported on this device.");
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, BLE_RESULT);
        } else {
            showToast("BLE Ready");
        }
    }

    private boolean hasBluetoothPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST);
    }
    private void setUpMap() {
        for (BaseOptions option : optionsList) {
            optionsMap.put(getString(option.getName()), option);
        }
    }

    private List<String> getOptionNames() {
        ArrayList<String> names = new ArrayList<>(optionsMap.size());
        for (BaseOptions option : optionsList) {
            names.add(getString(option.getName()));
        }
        return names;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BLE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                showToast("Bluetooth đã bật");
                checkBLEStatus();
            } else {
                showToast("Cần phải bật Bluetooth");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }



    private void startScan() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {

            isScanning = true;
            showToast("Scanning...");
            logViewer.append("Starting bluetooth scan"+"\n");

            try {
                bluetoothLeScanner.startScan(scanCallback);
            } catch (SecurityException e) {
                Log.d("Hoang", "startScan: "+e.getMessage());
                showToast("SecurityException: " + e.getMessage());
            }
        } else {
            requestBluetoothPermission1();
        }


    }



    private void requestBluetoothPermission1() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                BLUETOOTH_SCAN_PERMISSION_REQUEST);
    }


    private void stopScan() {
        showToast("Scan stopped");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothLeScanner.stopScan(scanCallback);
        isScanning = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_SCAN_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                showToast("Quyền sử dụng Bluetooth Scan đ b từ chối.");
            }
        }
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBLEStatus();
            } else {
                showToast("Quyền sử dụng Bluetooth đã bị từ chối");
            }
        }
    }
    private ScanCallback scanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            Log.d("Hoang", "onScanResult: "+rssi);

            if (rssi > CALIBRATION_DIFF) {
                connectToDevice(device);
            }


            Log.d("Address: ", device.getAddress());
        //    Log.d("Name: ", "s" + device.getName());
            Log.d("RSSI: ", String.valueOf(rssi));
            String deviceInfo = "Address: " + device.getAddress() + "\nRSSI: " + rssi;
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            showToast("Scan failed with error code: " + errorCode);
        }
    };


    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        showToast("Connecting to device: " + device.getName());
        stopScan();
    }
}
