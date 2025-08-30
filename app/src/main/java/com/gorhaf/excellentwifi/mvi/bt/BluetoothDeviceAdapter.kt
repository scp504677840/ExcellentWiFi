package com.gorhaf.excellentwifi.mvi.bt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gorhaf.excellentwifi.R

class BluetoothDeviceAdapter(
    private var devices: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceInfo: TextView = view.findViewById(R.id.device_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceInfo.text = devices[position]
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = devices.size

    fun updateData(newDevices: List<String>) {
        this.devices = newDevices
        notifyDataSetChanged()
    }
}
