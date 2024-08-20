package com.example.testingfirebaserealtime.eksperimen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.R

class MediaPagerAdapter(
    private val context: Context,
    private val mediaList: List<String>,
    private val mediaType: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
            ImageViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
            VideoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val media = mediaList[position]
        if (holder is ImageViewHolder) {
            Glide.with(context).load(media).into(holder.imageView)
        } else if (holder is VideoViewHolder) {
            val videoUri = Uri.parse(media)
            Log.d("MediaPagerAdapter", "Video URI: $videoUri")
            holder.videoView.setVideoURI(videoUri)
            holder.videoView.setOnPreparedListener {
                Log.d("MediaPagerAdapter", "Video is prepared")
                holder.videoView.start()
            }
            holder.videoView.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPagerAdapter", "Error playing video: $what, $extra")
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mediaType == "image") VIEW_TYPE_IMAGE else VIEW_TYPE_VIDEO
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    companion object {
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_VIDEO = 2
    }
}
