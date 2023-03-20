package com.example.bininfo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bininfo.R
import com.example.bininfo.databinding.ItemRequestLayoutBinding

class RequestAdapter : RecyclerView.Adapter<RequestAdapter.RequestHolder>() {

    var requestList = mutableSetOf<String>()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class RequestHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ItemRequestLayoutBinding.bind(item)
        fun bind(request: String) {
            binding.tvRequest.text = request
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RequestHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_request_layout, p0, false)
        return RequestHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(p0: RequestHolder, p1: Int) {
        p0.bind(requestList.elementAt(p1))
    }

    fun setList(list: MutableSet<String>) {
        requestList = list
        notifyDataSetChanged()
    }
}