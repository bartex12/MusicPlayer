package com.example.muzpleer.ui.tabs.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.model.Song

class RecyclerViewTabAdapter(val onLineListener:(Song)->Unit, val onLongClickListener:(Song)->Unit)
    : RecyclerView.Adapter<RecyclerViewTabAdapter.ViewHolder>() {

    var data:List<Song> = listOf()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: Song) {
            itemView.apply {
                findViewById<TextView>(R.id.tv_title).text = item.title
                findViewById<TextView>(R.id.tv_artist).text = item.artist

                //загружаем картинки обложек
                Glide.with(context)
                    .load(item.cover)
                    .centerCrop()
                    .placeholder(R.drawable.gimme)
                    .into(findViewById<ImageView>(R.id.artworkImageView))

                //устанавливаем слущатель щелчков на списке
                itemView.setOnClickListener {
                    onLineListener.invoke(item)
                }
                // устанавливаем слушатель долгих нажатий на списке для вызова контекстного меню
                //передаём имя файла раскладки
                itemView.setOnLongClickListener {
                    onLongClickListener.invoke(item)
                    false
                }
            }
        }
    }

}