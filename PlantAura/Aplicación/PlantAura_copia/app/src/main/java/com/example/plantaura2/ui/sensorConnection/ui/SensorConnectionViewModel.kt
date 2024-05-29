package com.example.plantaura2.ui.sensorConnection.ui

import android.Manifest
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class SensorConnectionViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    }

    private val _readCharacteristicValue = MutableLiveData<String>()
    val readCharacteristicValue: LiveData<String> = _readCharacteristicValue

    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null

    init {
        Log.d("SensorConnectionViewModel", "Iniciando ViewModel")
        if (hasBluetoothPermissions(application.applicationContext)) {
            scanForSensors(application.applicationContext)
        }
    }

    private fun hasBluetoothPermissions(context: Context): Boolean {
        Log.d("SensorConnectionViewModel", "Checking permissions")
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun scanForSensors(context: Context) {
        Log.d("SensorConnectionViewModel", "Scanning for sensors")
        if (hasBluetoothPermissions(context)) {
            Log.d("SensorConnectionViewModel", "Permisos para Bluetooth habilitados")
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                Log.d("SensorConnectionViewModel", "Bluetooth habilitado")
                val scanFilter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                    .build()
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
                    Log.d("SensorConnectionViewModel", "Escaneando dispositivos Bluetooth")
                } else {
                    Log.d("SensorConnectionViewModel", "No se tienen permisos para escanear dispositivos Bluetooth")
                }
            }
        } else {
            Log.d("SensorConnectionViewModel", "No se tienen permisos para Bluetooth")
            }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            connectToDevice(result.device, getApplication<Application>().applicationContext)
        }
    }

    private fun connectToDevice(device: BluetoothDevice, context: Context) {
        if (hasBluetoothPermissions(context)) {
            device.connectGatt(context, false, gattCallback)
        } else {
            Log.d("SensorConnectionViewModel", "No se tienen permisos para conectar a dispositivos Bluetooth")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt
                if (ActivityCompat.checkSelfPermission(
                        getApplication<Application>().applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    gatt.discoverServices()
                } else {
                    Log.d("SensorConnectionViewModel", "No se tienen permisos para conectar a dispositivos Bluetooth")
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(UUID.fromString(SERVICE_UUID))
                bluetoothGattCharacteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                readCharacteristicValue()
                Log.d("SensorConnectionViewModel", "onServicesDiscovered: $service")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            val value = characteristic.getStringValue(0)
            Log.d("SensorConnectionViewModel", "Characteristic value changed: $value")
            _readCharacteristicValue.postValue(value)
        }
    }

    private fun readCharacteristicValue() {
        bluetoothGattCharacteristic?.let { characteristic ->
            if (hasBluetoothPermissions(getApplication<Application>().applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        getApplication<Application>().applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothGatt?.readCharacteristic(characteristic)
                } else {
                    Log.d("SensorConnectionViewModel", "No se tienen permisos para leer características de dispositivos Bluetooth")
                }
            } else {
                Log.d("SensorConnectionViewModel", "No se tienen permisos para Bluetooth")
            }
        }
    }
}

class SensorConnectionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorConnectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}