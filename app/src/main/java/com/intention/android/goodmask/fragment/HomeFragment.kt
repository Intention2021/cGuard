package com.intention.android.goodmask.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.intention.android.goodmask.R
import com.intention.android.goodmask.activity.MyService
import com.intention.android.goodmask.databinding.FragHomeBinding
import com.intention.android.goodmask.dustData.DustInfo
import com.intention.android.goodmask.stationData.StationInfo
import org.locationtech.proj4j.CRSFactory
import java.util.*
import org.locationtech.proj4j.ProjCoordinate

import org.locationtech.proj4j.BasicCoordinateTransform

import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class HomeFragment : Fragment() {
    private var _binding: FragHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var maskFanPower: SeekBar
    lateinit var maskFanPowerText: TextView
    var addressList: List<String> = listOf("서울시", "중구", "명동")
    var addressInfo: String = "서울시 중구 명동"
    lateinit var address: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        address = arguments?.getString("address").toString()
        val lat = arguments?.getDouble("latitude")
        val long = arguments?.getDouble("longitude")

        maskFanPower = binding.seekBar
        maskFanPowerText = binding.fanTitle
        val (tmX, tmY) = setWGS84TM(lat!!, long!!)

        Log.d("TM_XY", "$tmX / $tmY")

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        getDustInfo(retrofit, tmX, tmY)

        maskFanPower.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maskFanPowerText.text = "팬 세기\n" + p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        Log.d("homeFrag", address.toString())
        binding.locationText.text = address
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.refreshBtn.setOnClickListener {
            getNewLocation(retrofit)
        }
        return view
    }

    // 새로고침 시 새 주소 출력
    private fun getNewLocation(retrofit: Retrofit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val geocoder = Geocoder(requireContext(), Locale.KOREA)
                    addressInfo = getNewAddress(geocoder, location!!, retrofit)
                    binding.locationText.text = addressInfo
                    Log.d("Clicked refresh button ", addressInfo)
                }
        }
    }

    // 새 좌표를 이용해 주소 반환
    private fun getNewAddress(geocoder: Geocoder, location: Location, retrofit: Retrofit): String {
        val addrList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        for (addr in addrList) {
            val splitedAddr = addr.getAddressLine(0).split(" ")
            addressList = splitedAddr
        }
        addressInfo = "${addressList[1]} ${addressList[2]} ${addressList[3]}"
        // 새 좌표를 다시 TM좌표로 변환 후 미세먼지 치수 다시 가져오기
        val (newTMX, newTMY) = setWGS84TM(location.latitude, location.longitude)
        getDustInfo(retrofit, newTMX, newTMY)
        Log.d("New Information", "$newTMX / $newTMY")

        return addressInfo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    // 가장 가까운 측정소를 구하고 미세먼지 농도 데이터 가져오기
    private fun getDustInfo(retrofit: Retrofit, tmX: Double, tmY: Double) {
        val stationService: StationService? = retrofit.create(StationService::class.java)

        val key = "reoV++nM+7IsY4GLfsMfFBjKc/0t6gmgytKyqzrR7DbEaCNasNyCT131Qk2yPuPeC5uQqcHFlt4nWQLhDsnWDw=="
        stationService?.getInfo(key, "json", tmX, tmY)
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
                    dataToService("AirKorea Service Error")
                    Toast.makeText(context, "측정소 정보를 가져오는 중 에러가 발생했습니다. 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun dataToService(status: String){
        // 포그라운드 데이터 전달 위함
        val intent = Intent(context, MyService::class.java)
        intent.putExtra("address", address)
        Log.e("Give Address", "데이터 전달 $address")
        Log.e("Home to Service Status", status)
        intent.putExtra("dustStatus", status)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent)
        } else{
            context?.startService(intent)
        }
    }

    // 해당 측정소에서 미세먼지 데이터 가져오기
    private fun getDustNum(retrofit: Retrofit, key: String, stationName: String, subStation: String) {
        val dustService: DustService? = retrofit.create(DustService::class.java)
        dustService?.getInfo(key, "json", stationName, "daily")
            ?.enqueue(object : Callback<DustInfo> {
                override fun onResponse(call: Call<DustInfo>, response: Response<DustInfo>) {
                    val dustList = response.body()?.response?.body?.items
                    val dustNum = dustList?.get(0)?.pm10Value

                    if (dustNum != "-") {
                        Log.d("Dust Num", "미세먼지 농도: $dustNum, 측정소는 $stationName")
                        binding.dust.text = "미세먼지 치수: $dustNum"
                        val status: String = setDustUI(dustNum!!.toInt())
                        // 서비스 클래스로 데이텉 전송
                        dataToService(status)
                    }
                    // 가장 가까운 측정소가 점검중일때 다음으로 가까운 측정소에 접근
                    else {
                        dustService.getInfo(key, "json", subStation, "daily")
                            .enqueue(object : Callback<DustInfo> {
                                override fun onResponse(call: Call<DustInfo>, response: Response<DustInfo>) {
                                    val subDustList = response.body()?.response?.body?.items
                                    val subDustNum = subDustList?.get(0)?.pm10Value
                                    Log.d("Sub Dust Num", "미세먼지 농도: $subDustNum, 측정소는 $subStation")
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
                status = binding.dust2.toString()
            }
            in 16..30 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_green)
                binding.imageView2.setBackgroundResource(R.drawable.smile)
                binding.dust2.text = "좋음"
                status = binding.dust2.toString()
            }
            in 31..80 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_yellow_btn)
                binding.imageView2.setBackgroundResource(R.drawable.sceptic)
                binding.dust2.text = "보통"
                status = binding.dust2.toString()
            }
            in 81..150 -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_orange_btn)
                binding.imageView2.setBackgroundResource(R.drawable.bad)
                binding.dust2.text = "나쁨"
                status = binding.dust2.toString()
            }
            else -> {
                binding.locationLayout.setBackgroundResource(R.drawable.rounded_red_btn)
                binding.imageView2.setBackgroundResource(R.drawable.angry)
                binding.dust2.text = "매우 나쁨"
                status = binding.dust2.toString()
            }
        }
        return status
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