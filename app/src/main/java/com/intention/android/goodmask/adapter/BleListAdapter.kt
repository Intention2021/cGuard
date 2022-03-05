package com.intention.android.goodmask.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.intention.android.goodmask.R
import com.intention.android.goodmask.activity.MainActivity
import com.intention.android.goodmask.fragment.HomeFragment
import com.intention.android.goodmask.fragment.MaskFragment
import com.intention.android.goodmask.model.MaskData
import com.intention.android.goodmask.model.NoticeData

class BleListAdapter(context: Context)
    : RecyclerView.Adapter<BleListAdapter.BleViewHolder>(){

    lateinit var mContext: Context
    public var items: ArrayList<BluetoothDevice>? = ArrayList()
    private lateinit var itemClickListner: ItemClickListener
    lateinit var itemView:View


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        mContext = parent.context
        itemView = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_item_layout, parent, false)
        return BleViewHolder(itemView)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        holder.bind(items?.get(position))
        holder.itemView.setOnClickListener {
            itemClickListner.onClick(it, items?.get(position), position)
        }

    }
    override fun getItemCount(): Int {
        return items?.size?:0
    }

    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(currentDevice: BluetoothDevice?) {
            val bleName = itemView.findViewById<TextView>(R.id.title)
            bleName.text = currentDevice?.name
            Log.d("lele", bleName.text.toString())
        }
    }
    interface ItemClickListener {
        fun onClick(view: View, device: BluetoothDevice?, position: Int)
    }
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    fun addDevice(device: BluetoothDevice?) {
        if (device != null) {
            Log.d("lele", "${device}")
            items?.add(device)
        }
    }

    fun clear() {
        items?.clear()
    }

}