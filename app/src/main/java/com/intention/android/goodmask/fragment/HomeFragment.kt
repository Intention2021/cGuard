package com.intention.android.goodmask.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.intention.android.goodmask.databinding.FragHomeBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        val address = arguments?.getString("address")
        val lat = arguments?.getDouble("latitude")
        val long = arguments?.getDouble("longitude")

        maskFanPower = binding.seekBar
        maskFanPowerText = binding.fanTitle
        val (tmX, tmY) = setWGS84TM(lat!!, long!!)
        // 구파발
        // val tmX = 192321.484115
        // val tmY = 458993.467457
        Log.d("TM_XY", "$tmX / $tmY")

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service: StationService? = retrofit.create(StationService::class.java)
        val key = "reoV++nM+7IsY4GLfsMfFBjKc/0t6gmgytKyqzrR7DbEaCNasNyCT131Qk2yPuPeC5uQqcHFlt4nWQLhDsnWDw=="
        service?.getInfo(key, "json", tmX, tmY)
            ?.enqueue(object : Callback<com.intention.android.goodmask.stationData.StationInfo> {
                override fun onResponse(call: Call<com.intention.android.goodmask.stationData.StationInfo>, response: Response<com.intention.android.goodmask.stationData.StationInfo>) {
                    val list = response.body()?.response?.body?.items
                    val nearestStationAddress = list?.get(0)?.addr
                    val nearestStationName = list?.get(0)?.stationName
                    Log.d("JSON Test", "가장 가까운 측정소 주소는 $nearestStationAddress, 지역은 $nearestStationName 입니다.")
                }

                override fun onFailure(call: Call<com.intention.android.goodmask.stationData.StationInfo>, t: Throwable) {
                    Log.d("onFailure", t.message!!)
                }

            })


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
            getNewLocation()
        }
        return view
    }

    private fun getNewLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    var geocoder = Geocoder(requireContext(), Locale.KOREA)
                    // Log.d("현재 위치 ", "${location.latitude} / ${location.longitude}")
                    addressInfo = getNewAddress(geocoder, location!!)
                    binding.locationText.text = addressInfo
                    Log.d("Clicked refresh button ", addressInfo)
                }
        }

    }

    private fun getNewAddress(geocoder: Geocoder, location: Location): String {
        val addrList =
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        for (addr in addrList) {
            val splitedAddr = addr.getAddressLine(0).split(" ")
            addressList = splitedAddr
        }
        addressInfo = "${addressList[1]} ${addressList[2]} ${addressList[3]}"

        return addressInfo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
}

interface StationService {
    @GET("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("returnType") returnType: String,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
    ): Call<com.intention.android.goodmask.stationData.StationInfo>
}