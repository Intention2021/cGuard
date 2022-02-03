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
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.intention.android.goodmask.databinding.FragHomeBinding
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var maskFanPower : SeekBar
    lateinit var maskFanPowerText : TextView
    var addressList: List<String> = listOf("서울시", "중구", "명동")
    var addressInfo: String = "서울시 중구 명동"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        var address = arguments?.getString("address")
        Log.d("receive data address", address!!)

        maskFanPower = binding.seekBar
        maskFanPowerText = binding.fanTitle

        maskFanPower.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
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

    private fun getNewLocation(){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
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

    private fun getNewAddress(geocoder: Geocoder, location: Location): String{
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
}

