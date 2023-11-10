package com.example.nhatkinauanapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nhatkinauanapp.databinding.ActivityAddBinding
import com.example.nhatkinauanapp.model.Foods
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date

class AddActivity : AppCompatActivity() {
    lateinit var binding : ActivityAddBinding
    private lateinit var firebaseRef : DatabaseReference
    private lateinit var storageRef : StorageReference
    private var uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Foods")
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        binding.btnSave.setOnClickListener {
            saveData()
        }
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.imFoodImage.setImageURI(it)
            if(it != null) {
                uri = it
            }
        }
        binding.btnChooseImgGallery.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun getCurrentTime(): String {
        val currentTime = Date()
        val currentTimeFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        return currentTimeFormat.format(currentTime)
    }

    private fun saveData() {
        val foodName = binding.edtName.text.toString()
        val foodRecipe = binding.edtRecipe.text.toString()

        if(foodName.isEmpty())  binding.edtName.error = "Nhập tên !!!"
        if(foodRecipe.isEmpty())  binding.edtRecipe.error = "Nhập công thức !!!"

        val foodId = firebaseRef.push().key!!
        var foods : Foods
        uri?.let {
            storageRef.child(foodId).putFile(it)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url->
                            val imgUrl = url.toString()
                            val currentTime = getCurrentTime()
                            foods = Foods(foodId, foodName, foodRecipe, imgUrl, currentTime, 0.0f,"")
                            firebaseRef.child(foodId).setValue(foods)
                                .addOnCompleteListener {
                                    Toast.makeText(this,"Lưu thành công !", Toast.LENGTH_SHORT).show()
                                    binding.edtName.text.clear()
                                    binding.edtRecipe.text.clear()
                                    binding.imFoodImage.setImageResource(android.R.drawable.ic_menu_gallery)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this,"Lưu thất bại ! Lỗi ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
        }


    }
}