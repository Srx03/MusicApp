package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.musicapp.R
import com.example.musicapp.data.entites.Song
import com.example.musicapp.databinding.SwipeItemBinding
import javax.inject.Inject


class SwipeSongAdapter@Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SwipeSongAdapter.SwipeSongViewHolder>()  {

    inner class SwipeSongViewHolder(val binding: SwipeItemBinding): RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    var songs: List<Song>
    get() = differ.currentList
    set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeSongViewHolder {
        return SwipeSongViewHolder(SwipeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: SwipeSongViewHolder, position: Int) {
        val song = differ.currentList[position]

        val text = "${song.title} - ${song.subtitle}"

        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(song) }
        }
    }
    private var onItemClickListener: ((Song) -> Unit)? = null
    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener

    }

    override fun getItemCount(): Int {
        return songs.size
    }
}
