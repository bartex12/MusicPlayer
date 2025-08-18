package com.example.muzpleer.ui.my.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class MyTracksAdapter(
    private val viewModel: SharedViewModel,
    val onLineListener:(Song)->Unit,
    val onLongClickListener:(Song)->Unit)
    : RecyclerView.Adapter<MyTracksAdapter.ViewHolder>() {

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

        // Следим за изменениями выбранной позиции
        viewModel.selectedSongPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }

        viewModel.currentSong
            .observe(holder.itemView.context as LifecycleOwner) { currentSong ->
                try{
                    holder.itemView.isSelected =  if (data[position].isLocal){
                        data[position].mediaUri == currentSong?.mediaUri
                    }else{
                        data[position].resourceId == currentSong?.resourceId
                    }
                }catch(e: Exception){
                    Log.d(TAG, "SongsAdapter Ошибка: ${e.message}")
                }
            }
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
                    viewModel.setSelectedPosition(absoluteAdapterPosition)
                    onLineListener.invoke(item)
                }
                // устанавливаем слушатель долгих нажатий на списке
                itemView.setOnLongClickListener {
                    viewModel.setSelectedPosition(absoluteAdapterPosition)
                    onLongClickListener.invoke(item)
                    false
                }
            }
        }
    }
companion object{
    const val TAG="33333"
}
}