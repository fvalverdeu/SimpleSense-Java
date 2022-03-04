package com.example.simplesensev1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.minew.beaconplus.sdk.MTCentralManager;
import com.minew.beaconplus.sdk.MTFrameHandler;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.enums.BluetoothState;
import com.minew.beaconplus.sdk.enums.FrameType;
import com.minew.beaconplus.sdk.frames.HTFrame;
import com.minew.beaconplus.sdk.frames.MinewFrame;
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener;
import com.minew.beaconplus.sdk.interfaces.OnBluetoothStateChangedListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int PERMISSION_COARSE_LOCATION = 2;

    private MTCentralManager mMtCentralManager;

    List<MTPeripheral> mtPeripherals = new ArrayList<>();
    public static MTPeripheral mtPeripheral;

    double temperature = 0.0;
    String qrMacCodeS1 = "AC:23:3F:A9:F2:08";
    String qrMacCodeS3 = "AC:23:3F:AA:86:A0";
    TextView txtData, txtTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button processButtonS1 = findViewById(R.id.btnProcessS1);
        processButtonS1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                process(qrMacCodeS1);
            }
        });

        Button processButtonS3 = findViewById(R.id.btnProcessS3);
        processButtonS3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                process(qrMacCodeS3);
            }
        });

        txtData = findViewById(R.id.txtData);
        txtTemp = findViewById(R.id.txtTemp);
    }

    private void process(String qrMacCode) {
        Toast.makeText(this, "Validando permisos", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 12345);
            txtData.setText("Permisos solicitados agregados");
        }
        try {
            MTCentralManager mtCentralManager = MTCentralManager.getInstance(this);
            txtTemp.setText("Se obtuvo instancia MTCentralManager");
            mtCentralManager.setBluetoothChangedListener(new OnBluetoothStateChangedListener() {
                @Override
                public void onStateChanged(BluetoothState state) {
                    txtData.setText("Cambio BluetoothState");
                }
            });
            mtCentralManager.startService();
            txtTemp.setText("Servicio iniciado");
            mtCentralManager.startScan();
            txtData.setText("Escaneando");
            mtCentralManager.setMTCentralManagerListener(new MTCentralManagerListener() {
                @Override
                public void onScanedPeripheral(final List<MTPeripheral> peripherals) {
                    txtData.setText("Se ha encontrado " + peripherals.size() + " dispositivos");
                    for (MTPeripheral mtPeripheral : peripherals) {
                        // get FrameHandler of a device.
                        MTFrameHandler mtFrameHandler = mtPeripheral.mMTFrameHandler;
                        String mac = mtFrameHandler.getMac(); 		//mac address of device
                        String name = mtFrameHandler.getName();		// name of device
                        ArrayList<MinewFrame> advFrames = mtFrameHandler.getAdvFrames();
                        txtData.setText(mac + " encontrado, validando coincidencia");
                        if (mac == qrMacCode) {
                            // Toast.makeText(this, "Dispositivo encontrado $mac", Toast.LENGTH_SHORT).show()
                            txtData.setText("Dispositivo encontrado " + mac);
                            for (MinewFrame minewFrame : advFrames) {
                                if (minewFrame.getFrameType() == FrameType.FrameHTSensor) {
                                    HTFrame htFrame = (HTFrame) minewFrame;
                                    temperature = htFrame.getTemperature();
                                    String strTemperature = Double.toString(temperature);
                                    // Toast.makeText(this, "Temperatura obtenida: $mac | $name => $temperature", Toast.LENGTH_SHORT).show();
                                    txtData.setText("Temperatura obtenida: " + strTemperature);
                                    TextView txtTemp = findViewById(R.id.txtTemp);
                                    txtTemp.setText(strTemperature);
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            txtData.setText("Error: " + e.getMessage());
        }


    }

}