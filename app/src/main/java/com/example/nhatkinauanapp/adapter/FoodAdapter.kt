package com.example.nhatkinauanapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nhatkinauanapp.model.Foods
import com.example.nhatkinauanapp.R
import com.squareup.picasso.Picasso

class FoodAdapter (private val foodList : ArrayList<Foods>): RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int) {

        }
    }

    fun setOnItemClickListener(clickListener: onItemClickListener) {
        mListener = clickListener
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) :  RecyclerView.ViewHolder(itemView) {
        val foodName : TextView = itemView.findViewById(R.id.tvName)
        val foodRecipe : TextView = itemView.findViewById(R.id.tvRecipe)
        val foodImage : ImageView = itemView.findViewById(R.id.imFoodImage)
        val createTime : TextView = itemView.findViewById(R.id.tvCreateTime)
        val foodRating : RatingBar = itemView.findViewById(R.id.foodRating)
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return ViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: FoodAdapter.ViewHolder, position: Int) {
        val currentFood = foodList[position]
        holder.foodName.text = currentFood.foodName
        holder.foodRecipe.text = currentFood.foodRecipe
        Picasso.get().load(currentFood.imgUri).into(holder.foodImage)
        holder.createTime.text = currentFood.createTime
        holder.foodRating.rating = currentFood.foodRating?: 0.0f

    }

    override fun getItemCount(): Int {
        return foodList.size
    }
}