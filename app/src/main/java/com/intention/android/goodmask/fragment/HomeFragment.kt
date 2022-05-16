package com.intention.android.goodmask.fragment

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.content.*
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.intention.android.goodmask.TextUtil.HexWatcher
//import com.intention.android.goodmask.activity.BleService
import com.intention.android.goodmask.activity.DeviceActivity
import com.intention.android.goodmask.databinding.FragHomeBinding
import com.intention.android.goodmask.db.MaskDB
import com.intention.android.goodmask.dustData.DustInfo
import com.intention.android.goodmask.model.MaskData
import com.intention.android.goodmask.stationData.StationInfo
//import com.intention.android.goodmask.viewmodel.BleViewModel
import org.locationtech.proj4j.BasicCoordinateTransform
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.ProjCoordinate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

import android.content.Intent
import android.os.*
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.intention.android.goodmask.*
import com.intention.android.goodmask.TextUtil
import com.intention.android.goodmask.activity.BleService
import com.intention.android.goodmask.activity.SplashActivity
import com.intention.android.goodmask.issc.Bluebit
import com.intention.android.goodmask.issc.data.BLEDevice
import com.intention.android.goodmask.issc.gatt.Gatt
import com.intention.android.goodmask.issc.gatt.Gatt.ListenerHelper
import com.intention.android.goodmask.issc.gatt.GattCharacteristic
import com.intention.android.goodmask.issc.gatt.GattDescriptor
import com.intention.android.goodmask.issc.gatt.GattService
import com.intention.android.goodmask.issc.impl.GattTransaction
import com.intention.android.goodmask.issc.impl.LeService
import com.intention.android.goodmask.issc.impl.LeService.*
import com.intention.android.goodmask.issc.reliableburst.ReliableBurstData
import com.intention.android.goodmask.issc.reliableburst.ReliableBurstData.ReliableBurstDataListener
import com.intention.android.goodmask.issc.util.TransactionQueue
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer


class HomeFragment : Fragment(), TransactionQueue.Consumer<GattTransaction> {

    private var device : BluetoothDevice? = null
    private lateinit var serviceIntent : Intent
    private lateinit var binding : FragHomeBinding
    public var string_value : String = ""
    lateinit var fusedLocationClient : FusedLocationProviderClient
    lateinit var maskFanPower: SeekBar
    lateinit var maskFanPowerText: TextView
    lateinit var disConBtn : Button
    var addressList: List<String> = listOf("서울시", "중구", "명동")
    var addressInfo: String = "서울시 중구 명동"
    lateinit var address: String
    var pastFanPower = 0
    lateinit var mga : Gatt
    private var db : MaskDB? = null
    private var fanPowerResponse = ""
    private var inputData: String = ""
    public var mService: LeService = LeService()
    private var mListener: Gatt.Listener? = GattListener()

    private var mDevice: BluetoothDevice? = device
    private var mConn: SrvConnection? = null

    private var mStream: OutputStream? = null
    private var retrofit : Retrofit? = null
    private var mQueue: TransactionQueue? = null

    private val MENU_CLEAR = 0x501

    private val INFO_CONTENT = "the_information_body"

    var FileString = ""
    var countx = 0
    private var cnt = 0

    private var mMsg: TextView? = null
    private var mToggleResponse: ToggleButton? = null

    private var mTransTx: GattCharacteristic? = null
    private var mTransRx: GattCharacteristic? = null
    private var mAirPatch: GattCharacteristic? = null
    private var mTransCtrl: GattCharacteristic? = null

    private lateinit var maskFanPower_off : AppCompatButton
    private lateinit var maskFanPower_1 : AppCompatButton
    private lateinit var maskFanPower_2 : AppCompatButton
    private lateinit var maskFanPower_3 : AppCompatButton
    private lateinit var maskFanPower_4 : AppCompatButton
    private lateinit var sensorBtn : AppCompatButton

    private var mSuccess = 0
    private var mFail = 0
    private var total_bytes = 0
    private var total_received_bytes = 0
    private var time: String? = null
    private var duration = 0f
    private var speed = 0f
    private var mStartTime: Calendar? = null
    private var mTotalTime: Calendar? = null
    private var mTempStartTime: Calendar? = null
    private var mRunnable: Runnable? = null
    private var writeThread: Handler? = null
    private var throughput_update = true
    private var subStation : String = ""

    private val MAX_LINES = 50
    private var mLogBuf: ArrayList<CharSequence>? = null

    private var transmit: ReliableBurstData? = null

    private var transmitListener: ReliableBurstDataListener? = null
    private var reTry = false
    private var enableTCP = false

    fun checkResponse(p : String){
        when(p) {
            "N" -> {
                getResponse("Q", p)
            }
            "A" -> {
                getResponse("W", p)
            }
            "B" -> {
                getResponse("E", p)
            }
            "C" -> {
                getResponse("R", p)
            }
            "D" -> {
                getResponse("T", p)
            }
            "U" -> {
                getResponse("O", p)
            }
            "I" -> {
                getResponse("P", p)
            }
        }
    }

    fun getResponse(r: String, s: String){
        Toast.makeText(context, "데이터 전송중...", Toast.LENGTH_SHORT).show()
        maskFanPower_off.isEnabled = false
        maskFanPower_1.isEnabled = false
        maskFanPower_2.isEnabled = false
        maskFanPower_3.isEnabled = false
        maskFanPower_4.isEnabled = false
        sensorBtn.isEnabled = false

        var handler = Handler()
        handler.postDelayed(object : Runnable{
            override fun run() {
                while(cnt<=5){
                    Log.d("cntCall", "cnt : ${cnt}")
                    write(s)
                    cnt++
                }
                cnt = 0
                handler.removeCallbacksAndMessages(null)

            }

        },2000);
        enableNotification()
        maskFanPower_off.isEnabled = true
        maskFanPower_1.isEnabled = true
        maskFanPower_2.isEnabled = true
        maskFanPower_3.isEnabled = true
        maskFanPower_4.isEnabled = true
        sensorBtn.isEnabled = true
        Toast.makeText(context, "데이터 전송 완료", Toast.LENGTH_SHORT).show()
    }

    inner class SrvConnection : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            mService = (service as LocalBinder).service
            mService.addListener(mListener)
            var conn = 0
            conn = mService.getConnectionState(mDevice)

            mService.connectGatt(activity?.applicationContext, false, mDevice)
            mga = mService.gatt
            if (conn != BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("connect_info", "DisConnected")
                onDisconnected()
            } else {
                Log.d("connect_info", "Connected")
                onConnected()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d("connect_info", "DisConnected")
        }
    }

    private fun onDisconnected() {
        mService.disconnect(mDevice)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Bluebit.no_burst_mode = 1
        Bluebit.board_id = 70

        mDevice = arguments?.getParcelable<BluetoothDevice>("device")
        Log.d("mDevice", "mDevice : ${mDevice?.name}")

        binding = DataBindingUtil.inflate(inflater, R.layout.frag_home, container,false)
        mConn = SrvConnection()
        mQueue = TransactionQueue(this)
//        binding.viewModel = viewModel

//        if (Bluebit.no_burst_mode == 1) {
//            mToggleResponse!!.isChecked = true
//        }
        mListener = GattListener()
        Log.d("LeService","bindService start")
        activity?.bindService(Intent(requireContext(),LeService::class.java), mConn!!, Context.BIND_AUTO_CREATE)
        mLogBuf = ArrayList()

        /* Transparent is not a leaf activity. connect service in onCreate */

        //Log.d("MADHU LOG");
        transmit = ReliableBurstData()
        transmitListener = ReliableBurstDataListener { reliableBurstData, transparentDataWriteChar ->
            }
        transmit!!.setListener(transmitListener)
        transmit!!.setBoardId(Bluebit.board_id)
        val thread2 = HandlerThread("writeThread")
        thread2.start()
        writeThread = Handler(thread2.looper)
        val view = binding.root
        retainInstance = true
        db = MaskDB.getInstance(context?.applicationContext!!)

        val r = Runnable {
            val data = db?.MaskDao()?.getAll()
            // 맨 처음으로 실행할 때 데이터가 없으므로 다 0으로 세팅
            if (data!!.size == 0){
                Log.e("First!!", "첫 실행입니다.")
                for (i in 1..7){
                    val firstDB = MaskData(i.toString(), 0.toLong(), 0.toLong(), 0.toLong());
                    db?.MaskDao()?.insert(firstDB)
                }
                val data = db?.MaskDao()?.getAll()
                Log.e("DBDBDB", "${data}")
                Log.e("DBDBDB", "${data?.size}")
            }
        }
        val thread = Thread(r)
        thread.start()

        address = arguments?.getString("address").toString()
        val lat = arguments?.getDouble("latitude")
        val long = arguments?.getDouble("longitude")

        maskFanPower_off = binding.fanpower0
        maskFanPower_1 = binding.fanpower1
        maskFanPower_2 = binding.fanpower2
        maskFanPower_3 = binding.fanpower3
        maskFanPower_4 = binding.fanpower4
        sensorBtn = binding.sensorBtn

        disConBtn = binding.disconnectBtn
        disConBtn.setOnClickListener {
            mService.disconnect(mDevice)
            val intentS = Intent(activity,BleService::class.java)
            intentS.action = "STOP"
            requireContext().startForegroundService(intentS)
            mService.onDestroy()
            activity?.finishAffinity()
            System.runFinalization()
            val intent = Intent(activity, DeviceActivity::class.java)
            startActivity(intent)
            System.exit(0)
        }
        val gson = GsonBuilder().setLenient().create()

        retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        Log.d("lat/long", "${lat}, ${long}")
        val (tmX, tmY) = setWGS84TM(lat!!, long!!)
        Log.d("TM_XY", "$tmX / $tmY")
        getDustInfo(retrofit!!, tmX, tmY)


        // 정해진 시간마다 업데이트
        val timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                Log.d("getnewloc", "newloc")
                getNewLocation(retrofit!!)
//                for (i in 1..3){
//                    viewModel.onClickWrite('P')
//                    Log.d("read/", "Read Start")
//                    viewModel.onClickRead()
//                    Log.d("read/", "Read End")
//                }
                // Log.d("read", "readByteArray : ${a.toString()}")
            }
        }, 10000, 3600000)

        Log.d("homeFrag", address.toString())
        binding.locationText.text = address
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.refreshBtn.setOnClickListener {
            getNewLocation(retrofit!!)
        }

        var start: Long = 0
        var end: Long = 0
        // 요일 1~7: 일~토

        var day = ""

        sensorBtn.setOnClickListener {
            if (sensorBtn.text == "ON") {
                sensorBtn.text = "OFF"
                checkResponse("U")
            } else {
                sensorBtn.text = "ON"
                checkResponse("I")
            }
        }

        maskFanPower_off.setOnClickListener {
            inputData = "N"
            checkResponse("N")
            (maskFanPower_1 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_2 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_3 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_4 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_off as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_orange_btn))

            if(pastFanPower != 0) {
                // 팬 작동 종료
                if (pastFanPower != 0) {
                    end = System.currentTimeMillis()
                    val r = Runnable {
                        // update use time (100 부분이 새로 추가될 시간, time은 지금까지 누적된 시간)
                        val time = db?.MaskDao()?.getTime(day)
                        // 아래부분은 시간 추가할 때 사하면 될
                        Log.e("Start and End", "$day / $start / $end")
                        val updateDB = MaskData(day, start, end.toLong(), end - start + time!!);
                        db?.MaskDao()?.update(updateDB)
                        val data = db?.MaskDao()?.getAll()
                        Log.e("DBDBDB", "${data}")
                        Log.e("DBDBDB", "${data?.size}")
                    }
                    val thread = Thread(r)
                    thread.start()

                }
            }
            pastFanPower = 0
        }

        maskFanPower_1.setOnClickListener {
            inputData = "A"
            checkResponse("A")
            (maskFanPower_off as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_2 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_3 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_4 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_1 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_orange_btn))

            if (pastFanPower == 0){
                start = System.currentTimeMillis()
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()

            }
            pastFanPower = 1
        }

        maskFanPower_2.setOnClickListener {
            inputData = "B"
            checkResponse("B")
            (maskFanPower_off as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_1 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_3 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_4 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_2 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_orange_btn))

            if (pastFanPower == 0){
                start = System.currentTimeMillis()
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()

            }
            pastFanPower = 2
        }

        maskFanPower_3.setOnClickListener {
            inputData = "C"
            checkResponse("C")
            (maskFanPower_off as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_2 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_1 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_4 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_3 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_orange_btn))

            if (pastFanPower == 0){
                start = System.currentTimeMillis()
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()

            }
            pastFanPower = 3
        }

        maskFanPower_4.setOnClickListener {
            inputData = "D"
            checkResponse("D")
            (maskFanPower_off as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_2 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_1 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_3 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_gr_btn))
            (maskFanPower_4 as AppCompatButton).setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.rounded_orange_btn))

            if (pastFanPower == 0){
                start = System.currentTimeMillis()
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()

            }
            pastFanPower = 4
        }

        return view
    }

//    private fun initObserver(binding: FragHomeBinding?) {
//        viewModel.readTxt?.observe(this,{
//            val now = System.currentTimeMillis()
//            var bstatus = ""
//            binding?.txtBattery?.text = bstatus
//            val datef = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
//            val timestamp = datef.format(Date(now))
//        })
//    }

    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    // 새로고침 시 새 주소 출력
    private fun getNewLocation(retrofit: Retrofit) {
        checkIfFragmentAttached {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        val geocoder = Geocoder(requireContext(), Locale.KOREA)
                        addressInfo = getNewAddress(geocoder, location!!, retrofit)
                        binding.locationText.text = addressInfo
                        Log.d("Clicked refresh button ", addressInfo)
                    }
            }
        }
    }

    // 새 좌표를 이용해 주소 반환
    private fun getNewAddress(geocoder: Geocoder, location: Location, retrofit: Retrofit): String {
        val addrList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.d("address", "${addrList}")
        for (addr in addrList) {
            val splitedAddr = addr.getAddressLine(0).split(" ")
            addressList = splitedAddr
        }
        addressInfo = "${addressList[addressList.size-3]} ${addressList[addressList.size-2]} ${addressList[addressList.size-1]}"
        // 새 좌표를 다시 TM좌표로 변환 후 미세먼지 치수 다시 가져오기
        val (newTMX, newTMY) = setWGS84TM(location.latitude, location.longitude)
        getDustInfo(retrofit, newTMX, newTMY)
        Log.d("New Information", "$newTMX / $newTMY")

        return addressInfo
    }

    // 위/경도 -> TM좌표로 변환
    private fun setWGS84TM(lat: Double, lon: Double): Pair<Double, Double> {
        Log.d("TMchanger", lat.toString() + "," + lon.toString())

        val factory = CRSFactory()
        val grs80 = factory.createFromName("EPSG:4326")
        val wgs84 = factory.createFromParameters(
            "goodmask",
            "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=GRS80 +units=m +no_defs"
        )

        val transformer = BasicCoordinateTransform(grs80, wgs84)
        val beforeCoord = ProjCoordinate(lon, lat)
        val afterCoord = ProjCoordinate()

        Log.d("TMchanger", transformer.transform(beforeCoord, afterCoord).toString())
        val tmp = transformer.transform(beforeCoord, afterCoord).toString().substring(15)
        val TM_x = tmp.split(" ")[0].toDouble()
        val TM_y = tmp.split(" ")[1].toDouble()
        val TMInfo = Pair(TM_x, TM_y)
        return TMInfo
    }

    public fun checkLongLatNull(long: Double?, lat:Double?) : Boolean{
        if (long == null || lat == null) return true
        else return false
    }

    // 가장 가까운 측정소를 구하고 미세먼지 농도 데이터 가져오기
    private fun getDustInfo(retrofit: Retrofit, tmX: Double?, tmY: Double?) {
        val stationService: StationService? = retrofit.create(StationService::class.java)

        val key = "reoV++nM+7IsY4GLfsMfFBjKc/0t6gmgytKyqzrR7DbEaCNasNyCT131Qk2yPuPeC5uQqcHFlt4nWQLhDsnWDw=="
        if (tmX == null || tmY == null){

        }
        stationService?.getInfo(key, "json", tmX!!, tmY!!)
            ?.enqueue(object : Callback<StationInfo> {
                override fun onResponse(call: Call<StationInfo>, response: Response<StationInfo>) {
                    val stationList = response.body()?.response?.body?.items
                    val nearestStationAddress = stationList?.get(0)?.addr
                    val nearestStationName = stationList?.get(0)?.stationName
                    val subStationName = stationList?.get(1)?.stationName
                    Log.d("JSON Test", "가장 가까운 측정소 주소는 $nearestStationAddress, 지역은 $nearestStationName 입니다. 후보 지역은 $subStationName!")

                    getDustNum(retrofit, key, nearestStationName.toString(), subStationName.toString())
                }

                override fun onFailure(call: Call<StationInfo>, t: Throwable) {
                    Log.d("onFailure in Station", t.message!!)
                    Toast.makeText(context, "측정소 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun fanPowerIO(inputData : Char){
//        Toast.makeText(context, "데이터 전송중..", Toast.LENGTH_SHORT).show()
//        for (i in 1..5){
//            viewModel.onClickWrite(inputData)
//            Log.d("read/write/log", "Read Start")
//
//            viewModel.onClickRead()
//
//        }
//        Thread.sleep(3000)
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dataToService(status: String){
        // 포그라운드 데이터 전달 위함
        Log.e("Give Address", "데이터 전달 $address")
        Log.e("Home to Service Status", status)
        serviceIntent = Intent(activity?.applicationContext, BleService::class.java)
        serviceIntent.putExtra("address", address)
        serviceIntent.putExtra("dustStatus", status)
        requireContext().startForegroundService(serviceIntent)
    }

    // 해당 측정소에서 미세먼지 데이터 가져오기
    private fun getDustNum(retrofit: Retrofit, key: String, stationName: String, subStation: String) {
        val dustService: DustService? = retrofit.create(DustService::class.java)
        dustService?.getInfo(key, "json", stationName, "daily")
            ?.enqueue(object : Callback<DustInfo> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<DustInfo>, response: Response<DustInfo>) {
                    val dustList = response.body()?.response?.body?.items
                    val dustNum = dustList?.get(0)?.pm10Value

                    if (dustNum != "-") {
                        Log.d("Dust Num", "미세먼지 농도: $dustNum, 측정소는 $stationName")
                        if (addressInfo == "서울시 중구 명동"){
                            binding.locationText.text = stationName
                            address = stationName
                        }else binding.locationText.text = stationName
                        binding.dust.text = "미세먼지 치수: $dustNum"
                        val status: String = setDustUI(dustNum!!.toInt())
                        // 서비스 클래스로 데이터 전송
                        dataToService(status)
                    }
                    // 가장 가까운 측정소가 점검중일때 다음으로 가까운 측정소에 접근
                    else {
                        dustService.getInfo(key, "json", subStation, "daily")
                            .enqueue(object : Callback<DustInfo> {
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onResponse(call: Call<DustInfo>, response: Response<DustInfo>) {
                                    val subDustList = response.body()?.response?.body?.items
                                    val subDustNum = subDustList?.get(0)?.pm10Value
                                    Log.d("Sub Dust Num", "미세먼지 농도: $subDustNum, 측정소는 $subStation")
                                    if (addressInfo == "서울시 중구 명동"){
                                        binding.locationText.text = subStation
                                    }
                                    binding.dust.text = "미세먼지 치수: $subDustNum"
                                    val subStatus = setDustUI(subDustNum!!.toInt())
                                    // 서비스 클래스로 데이텉 전송
                                    dataToService(subStatus)
                                }

                                override fun onFailure(call: Call<DustInfo>, t: Throwable) {
                                    Log.d("onFailure in Sub Dust", t.message!!)
                                    Toast.makeText(context, "미세먼지 정보를 가져오는 중 에러가 발생했습니다. 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onFailure(call: Call<DustInfo>, t: Throwable) {
                    Log.d("onFailure in Dust", t.message!!)
                    Toast.makeText(context, "미세먼지 정보를 가져오는 중 에러가 발생했습니다. 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 미세먼지 수치에 따른 UI
    private fun setDustUI(dust: Int): String {
        var status: String?= "status"
        when (dust) {
            in 0..15 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_skyblue_btn)
                binding.imageView2.setBackgroundResource(R.drawable.smile)
                binding.dust2.text = "매우 좋음"
                status = binding.dust2.text.toString()
            }
            in 16..30 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_green)
                binding.imageView2.setBackgroundResource(R.drawable.smile)
                binding.dust2.text = "좋음"
                status = binding.dust2.text.toString()
            }
            in 31..80 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_yellow_btn)
                binding.imageView2.setBackgroundResource(R.drawable.sceptic)
                binding.dust2.text = "보통"
                status = binding.dust2.text.toString()
            }
            in 81..150 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_orange_btn)
                binding.imageView2.setBackgroundResource(R.drawable.bad)
                binding.dust2.text = "나쁨"
                status = binding.dust2.text.toString()
            }
            else -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_red_btn)
                binding.imageView2.setBackgroundResource(R.drawable.angry)
                binding.dust2.text = "매우 나쁨"
                status = binding.dust2.text.toString()
            }
        }
        return status
    }

    private fun didGetData(s: String) {
        //Log.d("didGetData");
        synchronized(mQueue!!) {

            //mQueue.onConsumed();
            msgShow("", "\n")
            msgShow("wrote ", s)
            msgShow("", "\n")
            if ((mQueue!!.size() == 0) && (mTotalTime != null) && (mRunning != true) && (throughput_update == true)) {
                val elapse: Long = (Calendar.getInstance()
                    .getTimeInMillis()
                        - mTotalTime!!.getTimeInMillis())
                com.intention.android.goodmask.issc.util.Log.d(
                    (" total_bytes :" + total_bytes + " current time: " +
                            Calendar.getInstance().get(Calendar.MINUTE)
                            + " : " + Calendar.getInstance().get(Calendar.SECOND)
                            + " : " + Calendar.getInstance().get(Calendar.MILLISECOND))
                )
                com.intention.android.goodmask.issc.util.Log.d(" elapse in milliseconds :" + elapse)
                time = (elapse / 1000).toString() + "." + (elapse % 1000)
                duration = elapse.toFloat() / 1000
                speed = (total_bytes.toFloat() / (elapse).toFloat()) * (1000.00.toFloat())
                com.intention.android.goodmask.issc.util.Log.d("value : +" + speed)
                //Handler handler = new Handler();
                val runnable: Runnable = object : Runnable {
                    override fun run() {
                        msgShow(
                            "time", ("spent " + (duration)
                                    + " seconds" + "  Throughput: " + (speed)
                                    + " bytes/sec")
                        )
                        total_bytes = 0
                        mSuccess = 0
                        mFail = 0
                        mTotalTime = null
                    }
                }
                writeThread!!.postDelayed(runnable, 3000)

                mTempStartTime = mStartTime
                mStartTime = null
                throughput_update = false
            }
        }
    }

    override fun onDestroy() {
        //mQueue.clear();
        mQueue!!.destroy()
        //disableNotification();
        closeStream()
        //mViewHandler.removeCallbacksAndMessages(null);

        /*
		 * Transparent is not a leaf activity. disconnect/unregister-listener in
		 * onDestroy
		 */mService.rmListener(mListener)
        requireActivity().unbindService(mConn!!)
        super.onDestroy()
    }

    private fun enableNotification() {
        val set: Boolean = mService.setCharacteristicNotification(mTransTx, true)
        val dsc = mTransTx?.getDescriptor(Bluebit.DES_CLIENT_CHR_CONFIG)
        dsc?.value = dsc?.getConstantBytes(GattDescriptor.ENABLE_NOTIFICATION_VALUE)
        mService.writeDescriptor(dsc)
        //tx_ch.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        //mService.writeCharacteristic(mTransTx);
        /*GattTransaction transaction = new GattTransaction(dsc,
				dsc.getConstantBytes(GattDescriptor.ENABLE_NOTIFICATION_VALUE));
		mQueue.add(transaction);*/
        // mQueue.process();

        /*
		 * boolean success = mService.writeDescriptor(dsc);
		 * Log.d("writing enable descriptor:" + success);
		 */
    }

    private fun disableNotification() {
        val set: Boolean = mService.setCharacteristicNotification(mTransTx, false)
        com.intention.android.goodmask.issc.util.Log.d("set notification:$set")
        val dsc = mTransTx?.getDescriptor(Bluebit.DES_CLIENT_CHR_CONFIG)
        dsc?.value = dsc?.getConstantBytes(GattDescriptor.DISABLE_NOTIFICATION_VALUE)
        val transaction = GattTransaction(
            dsc,
            dsc?.getConstantBytes(GattDescriptor.DISABLE_NOTIFICATION_VALUE)
        )
         mQueue?.add(transaction)
         mQueue?.process()
        val success: Boolean = mService.writeDescriptor(dsc)
    }

    private fun closeStream() {
        com.intention.android.goodmask.issc.util.Log.d("closeStream")
        try {
            if (mStream != null) {
                mStream!!.flush()
                mStream!!.close()
            }
        } catch (e: IOException) {
            msgShow("close stream fail", e.toString())
            e.printStackTrace()
        }
        mStream = null
    }

    private fun writeToStream(data: ByteArray) {
        if (mStream != null) {
            try {
                mStream!!.write(data, 0, data.size)
                mStream!!.flush()
            } catch (e: IOException) {
                msgShow("write fail", e.toString())
                e.printStackTrace()
            }
        }
    }

    /**
     * Received data from remote when enabling Echo.
     *
     * Display the data and transfer back to device.
     */

    private fun onReciveData(data: ByteArray?) {
        //Log.d("[R}");
        val sb = StringBuffer()
        if (data == null) {
            sb.append("Received empty data")
            val msg = Bundle()
            com.intention.android.goodmask.issc.util.Log.d("going for msg.putCharSequence(INFO_CONTENT, sb)")
            msg.putCharSequence(INFO_CONTENT, sb)
        } else {
            val recv = String(data)
            msgShow("", recv)
            writeToStream(data)
        }
    }

    private fun msgShow(prefix: CharSequence?, cs: CharSequence?) {
        val sb = StringBuffer()

        //Log.d("count:" + countx);
        countx++
        sb.append(prefix)
        //sb.append(": ");
        sb.append(cs)
        //Log.d(sb.toString());
        val msg = Bundle()
        msg.putCharSequence(INFO_CONTENT, sb.toString())
        //for(int i = 30000; i > 0 ; i--);
    }

    /**
     * Write string to remote device.
     */
    private fun write(cs: CharSequence) {
        val bytes = cs.toString().toByteArray()
        com.intention.android.goodmask.issc.util.Log.d("write(CharSequence cs)")
        write(bytes)
    }

    /**
     * Write data to remote device.
     */
    private fun write(bytes: ByteArray) {
        writeThread!!.post {
            synchronized((mQueue)!!) {
                val buf: ByteBuffer = ByteBuffer.allocate(bytes.size)
                buf.put(bytes)
                buf.position(0)
                //Log.d(" write inside thread run TOATAL LENGTH :" + bytes.length);
                if (transmit!!.transmitSize() == 0) {
                    transmit?.setBoardId(70)
                    transmit!!.setTransmitSize()
                }
                Bluebit.toatal_transactions = bytes.size / (transmit!!.transmitSize())
                Log.d("transmit_info", "size : ${transmit!!.transmitSize()}, transmit : ${transmit}")
                if (bytes.size % (transmit!!.transmitSize()) != 0) {
                    Bluebit.toatal_transactions = Bluebit.toatal_transactions+1
                }
                while (buf.remaining() != 0) {
                    val size: Int =
                        if ((buf.remaining() > transmit!!.transmitSize()))
                            transmit!!.transmitSize() else buf.remaining()
                    val dst: ByteArray = ByteArray(size)
                    buf.get(dst, 0, size)
                    Log.d("transaction", "${mTransTx}, ${dst}")
                    val t: GattTransaction = GattTransaction(mTransRx, dst)
                    mQueue!!.add(t)
                    if (mQueue!!.size() == 1) {
                        mQueue!!.process()
                    }
                }
            }
        }
    }

    private var mRunning = false

    /**
     * Add message to UI.
     */
    private fun appendMsg(msg: CharSequence) {
        val sb = StringBuffer()
        sb.append(msg)
        //sb.append("\n");
        mLogBuf!!.add(sb)
        // we don't want to display too many lines
        com.intention.android.goodmask.issc.util.Log.d("appendMsg")
        if (mLogBuf!!.size > MAX_LINES) {
            mLogBuf!!.removeAt(0)
        }
        val text = StringBuffer()
        for (i in mLogBuf!!.indices) {
            text.append(mLogBuf!![i])
        }
        //Log.d("appendMsg text"+text);
        mMsg!!.text = text
    }

    private fun onConnected() {
        val list : List<GattService>? = mService.getServices(mDevice)
        Log.d("connected","onConnected")
        if (list == null || list.size == 0) {
            mService.discoverServices(mDevice)
        } else {
            onDiscovered()
        }
    }

    private fun onDiscovered() {
        var proprietary: GattService = mService.getService(
            mDevice,
            Bluebit.SERVICE_ISSC_PROPRIETARY
        )
        mTransTx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_TX)
        mTransRx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_RX)
        mAirPatch = proprietary.getCharacteristic(Bluebit.CHR_AIR_PATCH)
        mTransCtrl = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_CTRL)
        enableTCP = false

        enableNotification()

            //proprietary = mService.getService(mDevice, Bluebit.SERVICE_ISSC_AIR_PATCH_SERVICE);
            //if (proprietary.getImpl() != null) {
            //	mAirPatch = proprietary.getCharacteristic(Bluebit.CHR_AIR_PATCH);
            //}

            //onSetEcho(mToggleEcho.isChecked());
            //enableNotification();
            //sendVendorMPEnable();

            /*
		BluetoothGatt gatt = (BluetoothGatt) mService.getGatt().getImpl();
		if (gatt != null) {

			BluetoothGattCharacteristic air_ch_temp = (BluetoothGattCharacteristic) mTransTx
					.getImpl();

			BluetoothGattCharacteristic air_ch = (BluetoothGattCharacteristic) mTransCtrl
					.getImpl();

            BluetoothGattCharacteristic tx_ch = (BluetoothGattCharacteristic) mTransTx
                    .getImpl();


			//transmit.enableReliableBurstTransmit(gatt, air_ch);
            //transmit.enableReliableBurstTransmit(gatt, air_ch_temp);
            //enableNotification();
		}*/
            //enableNotification();

    }

//    private fun enableTCP() {
//        val gatt: BluetoothGatt? = mService.getGatt().getImpl() as BluetoothGatt?
//        if (gatt != null) {
//            val air_ch = mTransCtrl?.getImpl() as BluetoothGattCharacteristic
//            transmit!!.enableReliableBurstTransmit(gatt, air_ch)
//        }
//    }

    override fun onTransact(t: GattTransaction) {
        Log.d("Transact","Home transperant ${t}, ${t.isForDescriptor}, , ${String(t.value)}");
        if (total_bytes == 0) {
            throughput_update = true
            mTotalTime = Calendar.getInstance()
        }
        if (t.isForDescriptor) {
            val dsc = t.desc
            val success: Boolean = mService.writeDescriptor(dsc)
            Log.d(
                "transact",("writing " + dsc.characteristic.uuid.toString()
                        + " descriptor:" + success)
            )
        } else {
            //Log.d("onTransact t.isForDescriptor() false");
            Log.d("transact", "t.chr.value = ${t.chr.value}, ${t.desc}, ${t.value}")
            t.chr.value = t.value
            //Log.d("Value : " + t.value);
            val str1 = String(t.value)
            //Log.d("Value (string) : "+ str1);
            if (t.isWrite) {
                //Log.d(".WRITE_TYPE_DEFAULT"+ GattCharacteristic.WRITE_TYPE_DEFAULT);
                //Log.d("GattCharacteristic.WRITE_TYPE_NO_RESPONSE"+ GattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                val type = GattCharacteristic.WRITE_TYPE_DEFAULT
                t.chr.setWriteType(type)
                //Log.d("mToggleResponse.isChecked()  ;  type : "+mToggleResponse.isChecked() +type);
                //Log.d("!t.chr.getUuid().equals(Bluebit.CHR_AIR_PATCH)"+ !t.chr.getUuid().equals(Bluebit.CHR_AIR_PATCH));
                if ((type == GattCharacteristic.WRITE_TYPE_NO_RESPONSE
                            && t.chr.uuid != Bluebit.CHR_AIR_PATCH)
                ) {

                    synchronized((mQueue)!!) {
                        com.intention.android.goodmask.issc.util.Log.d("calling canSendReliableBurstTransmit")
                        if (transmit!!.canSendReliableBurstTransmit()) {
                            val ch: BluetoothGattCharacteristic = t.chr
                                .getImpl() as BluetoothGattCharacteristic
                            com.intention.android.goodmask.issc.util.Log.d("calling reliableBurstTransmit")
                            transmit!!.reliableBurstTransmit(t.value, ch)
                        } else {
                            mQueue!!.addFirst(t)
                            mQueue!!.onConsumed()
                        }
                    }
                } else {

                    Log.d("transaction","t.chr == : ${t.chr}")
                    mService.writeCharacteristic(t.chr)
                }
            } else {
                Log.d("transaction","t.chr == read : ${t.chr}")
                mService.readCharacteristic(t.chr)
            }
        }
    }


   inner class GattListener() : ListenerHelper("ActivityTransparent") {
        override fun onConnectionStateChange(gatt: Gatt, status: Int, newState: Int) {
            Log.d("gattlistener","onConnectionStateChange: DATA TRANSFER ")
            if (mDevice?.getAddress() != gatt.device.address) {
                // not the device I care about
                Log.d("gattlistener", "not the device connected")
                return
            }
            if (reTry == true) {
                Log.d("gattlistener", "not the device connected")
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mService.connectGatt(activity, false, mDevice)
                } else {
                    transmit = null
                    com.intention.android.goodmask.issc.util.Log.d("ReliableBurstData :DATA TRANSFER")
                    transmit = ReliableBurstData()
                    transmit?.setListener(transmitListener)
                    onConnected()
                    com.intention.android.goodmask.issc.util.Log.d("setting board id for trnasmitdata" + Bluebit.board_id)
                    transmit?.setBoardId(Bluebit.board_id)
                }

                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnected()
            }
        }

        override fun onServicesDiscovered(gatt: Gatt, status: Int) {
            onDiscovered()
        }

        override fun onCharacteristicRead(
            gatt: Gatt, charac: GattCharacteristic,
            status: Int
        ) {
            val value = charac.value
            com.intention.android.goodmask.issc.util.Log.d("get value, byte length:" + value.size)
            for (i in value.indices) {
                Log.d("onCharacteristicRead","[" + i + "]" + java.lang.Byte.toString(value[i]))
            }
            mQueue?.let { synchronized(it) { mQueue?.onConsumed() } }
        }

        override fun onCharacteristicChanged(gatt: Gatt, chrc: GattCharacteristic) {
            Log.d("gogogo","onCharacteristicChanged : ${chrc.uuid} ${Bluebit.CHR_ISSC_TRANS_TX}")
            if ((chrc.uuid == Bluebit.CHR_ISSC_TRANS_TX)) {
                var value = chrc.value
                Log.d("characteristichange","value : ${String(value)}")
                total_received_bytes = total_received_bytes + value.size
                com.intention.android.goodmask.issc.util.Log.d("get value, byte length:" + value.size)
                com.intention.android.goodmask.issc.util.Log.d("total_received_bytes : $total_received_bytes")
                var buffer = ""
                for (i in value.indices) {
                    buffer = buffer + String.format("%02X ", value[i])
                }
                //msgShow("send", cs);
                onReciveData(chrc.value)
                //onEcho(arr);
                if (mRunnable != null) {
                    //byte[] value = chrc.getValue();
                    //String buffer = "";
                    for (i in value.indices) {
                        buffer = buffer + String.format("%02X ", value[i])
                    }
                    //onReciveData(chrc.getValue());
                }
            }
            if (Bluebit.board_id == 70) {
                if ((chrc.uuid == Bluebit.CHR_ISSC_TRANS_CTRL)) {
                    //Log.d(" onCharacteristicChanged Bluebit.CHR_ISSC_TRANS_CTRL " + Bluebit.CHR_ISSC_TRANS_CTRL);
                    transmit?.decodeReliableBurstTransmitEvent(chrc.value)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: Gatt, charac: GattCharacteristic,
            status: Int
        ) {
            com.intention.android.goodmask.issc.util.Log.d("onCharacteristicWrite")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                reTry = false
            }
            val value = charac.value
            //Log.d("Response length = "+ value.length);
            //Log.d("Response  = "+ value[0]);
            if (value.size == 1 && value[0].toInt() == 20) {
                return
            }
            //charac.
            /*for(byte i = 0; i< value.length;i++) {

				Log.d(" Value "+ i +" : "+value[i]);

			}*/
            val ch = charac
                .impl as BluetoothGattCharacteristic
            if (transmit!!.isReliableBurstTransmit(ch)) {
                if (status == Gatt.GATT_SUCCESS) {
                    if (status == Gatt.GATT_SUCCESS) {
                        if (Bluebit.board_id != 78) {
                            mSuccess += charac.value.size
                        }
                    } else {
                        mFail += charac.value.size
                    }
                    if (Bluebit.board_id != 78) {
                        total_bytes = total_bytes + charac.value.size
                    }
                    com.intention.android.goodmask.issc.util.Log.d(
                        ("onCharacteristicWrite isReliableBurstTransmit success transmit.isBusy()" +
                                " " + transmit!!.isBusy())
                    )
                    if (!transmit!!.isBusy() && (charac.uuid == Bluebit.CHR_AIR_PATCH)) {
                        enableNotification()
                    }
                }
            } else {
                mQueue?.let {
                    synchronized(it) {
                        //Log.d("onCharacteristicWrite callback");
                        mQueue!!.onConsumed()
                    }
                }
                if ((charac.uuid == Bluebit.CHR_AIR_PATCH)) {
                    com.intention.android.goodmask.issc.util.Log.i("Write AirPatch Characteristic:$status")
                } else {
                    //Log.d("---------------");
                    if (status == Gatt.GATT_SUCCESS) {
                        mSuccess += charac.value.size
                    } else {
                        mFail += charac.value.size
                    }
                    total_bytes = total_bytes + charac.value.size
                    val s = String.format(
                        "%d bytes , success= %d, fail= %d, pending= %d, TOTAL=%d",
                        charac.value.size, mSuccess, mFail,
                        mQueue!!.size(), total_bytes
                    )
                    didGetData(s)
                }
            }
        }

        override fun onDescriptorWrite(gatt: Gatt, dsc: GattDescriptor, status: Int) {
            val ch = dsc
                .characteristic.impl as BluetoothGattCharacteristic
            com.intention.android.goodmask.issc.util.Log.d("onDescriptorWrite")
            if (Bluebit.board_id == 70) {
//                if (!enableTCP) {
//                    enableTCP = true
//                    enableTCP()
//                } else {
//                    enableTCP = false
//                    //Log.d("### TEST!");
//                }
            }
            if (status == 5) {
                reTry = true
                return
            }
            if (reTry && status == 133) {
                mService.disconnect(mDevice)
                return
            }
            if (transmit!!.isReliableBurstTransmit(ch)) {
                if (status == Gatt.GATT_SUCCESS) {
                    if (!transmit!!.isBusy()) {
                        if (mQueue!!.size() > 0) {
                            mQueue!!.process()
                        } else {
                            enableNotification()
                        }
                    }
                }
            } else {
                mQueue!!.onConsumed()
                if ((dsc.characteristic.uuid
                            == Bluebit.CHR_AIR_PATCH)
                ) {
                    if (status == Gatt.GATT_SUCCESS) {
                        com.intention.android.goodmask.issc.util.Log.i("Write AirPatch Descriptor Success")
                    }
                } else {
                    if (status == Gatt.GATT_SUCCESS) {
                        val value = dsc.value
                        if (Arrays
                                .equals(
                                    value,
                                    dsc.getConstantBytes(GattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                )
                        ) {
                        } else if (Arrays
                                .equals(
                                    value,
                                    dsc.getConstantBytes(GattDescriptor.DISABLE_NOTIFICATION_VALUE)
                                )
                        ) {
                        }
                    }
                }
            }
        }
    }

}

interface StationService {
    @GET("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("returnType") returnType: String,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
    ): Call<StationInfo>
}

interface DustService {
    @GET("/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("returnType") returnType: String,
        @Query("stationName") stationName: String,
        @Query("dataTerm") dataTerm: String
    ): Call<DustInfo>
}