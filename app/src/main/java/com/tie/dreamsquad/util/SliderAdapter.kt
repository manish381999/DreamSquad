package com.tie.dreamsquad.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tie.dreamsquad.R
import com.tie.dreamsquad.databinding.SliderItemBinding
import com.tie.dreamsquad.ui.credentials.model.ImageData

class SliderAdapter(
    private val context: Context,
    private var imageDataList: List<ImageData>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = SliderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imageData = imageDataList[position]

        // Load the image into ImageView with Glide
        Glide.with(context)
            .load(imageData.image_url)
            .placeholder(R.drawable.app_logo)
            .error(R.drawable.app_logo)
            .into(holder.binding.imageView)

        // Set the name if you want it
        holder.binding.imageViewName.text = imageData.image_name
    }

    override fun getItemCount(): Int = imageDataList.size

    fun updateImages(newImageDataList: List<ImageData>) {
        imageDataList = newImageDataList
        notifyDataSetChanged()
    }

    inner class SliderViewHolder(val binding: SliderItemBinding) : RecyclerView.ViewHolder(binding.root)
}
