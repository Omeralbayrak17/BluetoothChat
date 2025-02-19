import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.util.*

class BluetoothRepository(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothSocket: BluetoothSocket? = null

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Paired devices list
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices

    // BluetoothManager Callback'ini tanımlıyoruz
    private val bluetoothProfileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d("Bluetooth", "Bluetooth servisi başarıyla bağlandı.")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d("Bluetooth", "Bluetooth servisi bağlantısı kesildi.")
            }
        }
    }

    // Bluetooth servisini başlatma
    fun initializeBluetooth() {
        bluetoothAdapter?.let { adapter ->
            if (!adapter.isEnabled) {
                // Bluetooth açık değilse aç - bu kısım için izin kontrolü yapmalısınız
                adapter.enable()
            }

            // BluetoothManagerCallback hatası için düzeltme
            try {
                bluetoothManager.getAdapter()?.getProfileProxy(
                    context,
                    bluetoothProfileServiceListener,
                    BluetoothProfile.HEADSET
                )
            } catch (e: Exception) {
                Log.e("Bluetooth", "getProfileProxy hatası: ${e.message}")
            }
        } ?: run {
            Log.e("Bluetooth", "Bluetooth adaptörü mevcut değil.")
        }
    }

    // Eşleşmiş cihazları almak
    suspend fun getPairedDevices() {
        bluetoothAdapter?.let { adapter ->
            val devices = adapter.bondedDevices.toList()
            _pairedDevices.value = devices
            devices.forEach { device ->
                Log.d("Bluetooth", "Cihaz: ${device.name}, ${device.address}")
            }
        } ?: run {
            Log.e("Bluetooth", "Eşleşmiş cihazlar alınamıyor.")
        }
    }

    // Bluetooth cihazına bağlanma ve mesaj gönderme
// BluetoothRepository.kt sınıfında düzeltilmesi gereken bölüm
    suspend fun connectAndSendMessage(device: BluetoothDevice, message: String): Boolean {
        // Keşif modunu iptal et - bu bağlantıyı hızlandırır
        bluetoothAdapter?.cancelDiscovery()

        var socket: BluetoothSocket? = null

        // Önce standart yöntemi deneyelim (2 saniye timeout ile)
        try {
            Log.d("Bluetooth", "Cihaza bağlanmaya çalışılıyor: ${device.address}")
            socket = device.createRfcommSocketToServiceRecord(MY_UUID)

            // Bazı cihazlar için bağlantı zaman aşımı koyalım
            val connectThread = Thread {
                try {
                    socket?.connect()
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Bağlantı hatası (thread): ${e.message}")
                    try {
                        socket?.close()
                    } catch (ce: IOException) {}
                }
            }

            connectThread.start()
            connectThread.join(5000) // 5 saniye timeout

            if (connectThread.isAlive) {
                connectThread.interrupt()
                throw IOException("Bağlantı zaman aşımı")
            }

            // Bağlantının açık olup olmadığını kontrol et
            if (socket?.isConnected != true) {
                throw IOException("Bağlantı başarısız oldu")
            }

            Log.d("Bluetooth", "Bağlantı başarılı, mesaj gönderiliyor")

            // Mesajı gönder
            val outputStream = socket.outputStream
            outputStream.write(message.toByteArray())
            outputStream.flush()

            return true
        } catch (e: IOException) {
            Log.e("Bluetooth", "Standart bağlantı başarısız: ${e.message}")

            // Standart yöntem başarısız olursa, fallback deneyelim
            try {
                socket?.close()

                // Bağlantıyı tekrar denemeden önce kısa bir bekleme yapalım
                kotlinx.coroutines.delay(1000)

                // İnsecure bağlantı deneyelim
                Log.d("Bluetooth", "Insecure bağlantı deneniyor")
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
                socket.connect()

                // Bağlantı başarılı, mesajı gönder
                val outputStream = socket.outputStream
                outputStream.write(message.toByteArray())
                outputStream.flush()

                return true
            } catch (e2: IOException) {
                Log.e("Bluetooth", "Insecure bağlantı da başarısız: ${e2.message}")

                try {
                    // Son çare - cihaz özel bağlantı yöntemi
                    socket?.close()

                    // Bu yöntem bazı Samsung cihazlarda işe yarayabilir
                    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
                    socket = method.invoke(device, 1) as BluetoothSocket

                    Log.d("Bluetooth", "Reflection yöntemi deneniyor")
                    socket.connect()

                    val outputStream = socket.outputStream
                    outputStream.write(message.toByteArray())
                    outputStream.flush()

                    return true
                } catch (e3: Exception) {
                    Log.e("Bluetooth", "Tüm bağlantı yöntemleri başarısız oldu", e3)
                    socket?.close()
                }
            }
        } finally {
            this.bluetoothSocket = socket
        }

        return false
    }
    
    // Bağlantıyı kapatmak
    fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
