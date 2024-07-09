package com.technoserve.cpqi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.technoserve.cpqi.R
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter(private val imageResources: Array<Int>) :
    SliderViewAdapter<ImageSliderAdapter.SliderViewHolder>() {

    override fun getCount(): Int {
        // Return the size of our image URL array.
        return imageResources.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        // Inflate the layout for our slider item.
        val inflate: View =
            LayoutInflater.from(parent!!.context).inflate(R.layout.image_slider_item, parent, false)
        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder?, position: Int) {
        // Load the image using Glide library into the image view.
        Glide.with(viewHolder!!.imageView.context)
            .load(imageResources[position])
            .fitCenter()
            .into(viewHolder.imageView)
    }

    // Define a ViewHolder for our slider view.
    class SliderViewHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        // Reference to the image view in the slider item layout.
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
