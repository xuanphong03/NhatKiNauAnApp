package com.example.nhatkinauanapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nhatkinauanapp.R
import com.example.nhatkinauanapp.model.Comments

class CommentAdapter(private val commentList: ArrayList<Comments>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) :  RecyclerView.ViewHolder(itemView) {
        val timeComment : TextView = itemView.findViewById(R.id.tvTimeComment)
        val contentComment : TextView = itemView.findViewById(R.id.tvContentComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemtView = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(itemtView)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComment = commentList[position]
        holder.timeComment.text = currentComment.time
        holder.contentComment.text = currentComment.content
    }


}