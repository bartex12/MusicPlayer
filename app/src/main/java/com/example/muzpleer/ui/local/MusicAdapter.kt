package com.example.muzpleer.ui.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.local.MusicAdapter.ViewHolder
import com.example.muzpleer.ui.tracks.TracksAdapter

class MusicAdapter(
    private val onItemClick: (MusicTrack) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    var data:List<MusicTrack> = listOf()
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_track, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (MusicTrack) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val artwork: ImageView = itemView.findViewById(R.id.trackArtwork)
        private val title: TextView = itemView.findViewById(R.id.trackTitle)
        private val artist: TextView = itemView.findViewById(R.id.trackArtist)
        private val duration: TextView = itemView.findViewById(R.id.trackDuration)

        fun bind(track: MusicTrack) {
            title.text = track.title
            artist.text = track.artist
            duration.text = track.duration.formatAsTime()

            // Загрузка обложки
            track.artworkUri?.takeIf { it.isNotEmpty() }?.let { uri ->
                Glide.with(itemView)
                    .load(uri)
                    .placeholder(R.drawable.placeholder1024)
                    .into(artwork)
            } ?: run {
                artwork.setImageResource(R.drawable.placeholder1024)
            }

            itemView.setOnClickListener { onItemClick(track) }
        }

        private fun Long.formatAsTime(): String {
            val seconds = this / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
}