package com.quash.bugger.ui

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quash.bugger.data.MarvelEntity
import com.quash.bugger.databinding.RowBuggerBinding

class BuggerViewHolder(private val binding : RowBuggerBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(item: com.quash.bugger.data.Character?) {
        item?.let { marvelEntity ->
            Glide.with(itemView.context)
                .load(marvelEntity.img)
                .into(binding.imageView)

            binding.tvCrashTitle.text = marvelEntity.name
        }
    }
}