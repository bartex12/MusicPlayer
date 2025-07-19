package com.example.muzpleer.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.databinding.ItemHomeBinding
import com.example.muzpleer.model.DataHome

class HomeAdapter(private val listener:(Int)->Unit)
    : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

var dataHomeList:List<DataHome> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(dataHomeList[position])

        holder.itemView.setOnClickListener {
            listener.invoke(position)
        }
    }

    override fun getItemCount(): Int = dataHomeList.size

    inner class HomeViewHolder(private val binding: ItemHomeBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(data: DataHome){
            binding.ivRoundPicture.setImageDrawable(data.picture)
            binding.tvNameAction.text = data.head
            binding.tvDescription.text = data.subHead
        }
    }
}