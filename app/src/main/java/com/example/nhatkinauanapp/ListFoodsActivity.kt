package com.example.nhatkinauanapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nhatkinauanapp.adapter.FoodAdapter
import com.example.nhatkinauanapp.databinding.ActivityListFoodsBinding
import com.example.nhatkinauanapp.model.Foods
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListFoodsActivity : AppCompatActivity() {
    lateinit var binding : ActivityListFoodsBinding
    private lateinit var foodList : ArrayList<Foods>
    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListFoodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvFoods.layoutManager = LinearLayoutManager(this)
        binding.rvFoods.setHasFixedSize(true)

        foodList = arrayListOf<Foods>()
        getFoodsData()

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getFoodsData() {
        binding.rvFoods.visibility = View.GONE
        binding.tvLoading.visibility = View.VISIBLE

        dbRef = FirebaseDatabase.getInstance().getReference("Foods")
        dbRef.addValueEventListener(object:  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                if(snapshot.exists()) {
                    for(foodSnap in snapshot.children) {
                        val foodData = foodSnap.getValue(Foods::class.java)
                        foodList.add(foodData!!)
                    }
                    if(foodList.size > 1) {
                        foodList = foodList.reversed() as ArrayList<Foods>
                    }


                    val mAdapter = FoodAdapter(foodList)
                    binding.rvFoods.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object : FoodAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@ListFoodsActivity, FoodDetails::class.java)

                            //put extra
                            intent.putExtra("foodId", foodList[position].foodId)
                            intent.putExtra("foodName", foodList[position].foodName)
                            intent.putExtra("foodRecipe", foodList[position].foodRecipe)
                            intent.putExtra("foodImage", foodList[position].imgUri)
                            intent.putExtra("foodRating", foodList[position].foodRating)
                            intent.putExtra("foodSecret", foodList[position].foodSecret)
                            startActivity(intent)
                        }
                    })

                    binding.rvFoods.visibility = View.VISIBLE
                    binding.tvLoading.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}