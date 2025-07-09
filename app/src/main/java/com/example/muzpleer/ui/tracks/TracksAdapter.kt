package com.example.muzpleer.ui.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.model.MediaItemApp

class TracksAdapter(
    private val onClick: (MediaItemApp) -> Unit
) : RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    var data:List<MediaItemApp> = listOf()
    set(value){
            field = value
            notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: MediaItemApp) {
            itemView.apply {
                findViewById<TextView>(R.id.titleTextView).text = item.title
                findViewById<TextView>(R.id.artistTextView).text = item.artist

                //загружаем картинки обложек
                Glide.with(context)
                    .load(item.cover)
                    .centerCrop()
                    .placeholder(R.drawable.gimme)
                    .into(findViewById<ImageView>(R.id.artworkImageView))

                setOnClickListener { onClick.invoke(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

//    class DiffCallback : DiffUtil.ItemCallback<MediaItem>() {
//        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
//            return oldItem == newItem
//        }
//    }
}
