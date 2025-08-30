package com.gorhaf.excellentwifi.mvi.wifi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gorhaf.excellentwifi.R

class WifiNetworkAdapter(private var networks: List<String>) :
    RecyclerView.Adapter<WifiNetworkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val networkInfoTextView: TextView = view.findViewById(R.id.network_info_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_network, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.networkInfoTextView.text = networks[position]
    }

    override fun getItemCount() = networks.size

    fun updateData(newNetworks: List<String>) {
        this.networks = newNetworks
        notifyDataSetChanged()
    }
}
