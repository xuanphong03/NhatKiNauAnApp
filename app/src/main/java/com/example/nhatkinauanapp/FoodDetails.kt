package com.example.nhatkinauanapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nhatkinauanapp.adapter.CommentAdapter
import com.example.nhatkinauanapp.databinding.ActivityFoodDetailsBinding
import com.example.nhatkinauanapp.model.Comments
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date

class FoodDetails : AppCompatActivity() {
    lateinit var binding: ActivityFoodDetailsBinding
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null
    private lateinit var imFoodImageUpdate: ImageView
    private lateinit var commentRef : DatabaseReference
    private lateinit var commentsList: ArrayList<Comments>


    private val changeImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                // Hiển thị hình ảnh đã chọn trong imFoodImage
                imFoodImageUpdate.setImageURI(uri)
                // Lưu uri vào biến uri để sử dụng sau này (nếu cần)
                this.uri = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Kết nối với storage
        storageRef = FirebaseStorage.getInstance().getReference("Images")
        // Set giá trị cho view
        setValuesToView()

        binding.btnUpdate.setOnClickListener {
            openUpdateDialog(
                intent.getStringExtra("foodId").toString(),
                intent.getStringExtra("foodName").toString(),
            )
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ListFoodsActivity::class.java)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            deleteItem(intent.getStringExtra("foodId").toString())
        }

        commentsList = arrayListOf<Comments>()
        binding.btnComment.setOnClickListener {
            openCommentDialog(
                intent.getStringExtra("foodId").toString(),
                intent.getStringExtra("foodName").toString()
            )
        }

    }

    private fun deleteItem(foodId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Foods").child(foodId)
        val mTask = dbRef.removeValue()
        mTask.addOnSuccessListener {
            Toast.makeText(this,"Đã xóa", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ListFoodsActivity::class.java)
            finish()
            startActivity(intent)
        }.addOnFailureListener {err ->
            Toast.makeText(this,"Lỗi ${err.message} ! Xóa thất bại", Toast.LENGTH_SHORT).show()
        }
    }



    private fun openCommentDialog(foodId: String, foodName: String) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.comment_dialog, null)
        mDialog.setView(mDialogView)
        mDialog.setTitle("COMMENTS")

        val btnSend = mDialogView.findViewById<Button>(R.id.btnSend)
        val rvComments = mDialogView.findViewById<RecyclerView>(R.id.rvComments)
        val etComment = mDialogView.findViewById<EditText>(R.id.edtComment)
        val btnBack = mDialogView.findViewById<Button>(R.id.btnBack)

        val alertDialog = mDialog.create()
        alertDialog.show()

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.setHasFixedSize(true)

        commentRef = FirebaseDatabase.getInstance().getReference("Comments").child(foodId)
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentsList.clear()
                if(snapshot.exists()) {
                    for(commentSnap in snapshot.children) {
                        val commentData = commentSnap.getValue(Comments::class.java)
                        commentsList.add(commentData!!)
                    }
                    if(commentsList.size > 1) {
                        commentsList = commentsList.reversed() as ArrayList<Comments>
                    }
                    val mAdapter = CommentAdapter(commentsList)
                    rvComments.adapter = mAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        btnSend.setOnClickListener {
            val commentID = commentRef.push().key!!
            val commentCurrentTime = getCurrentTime()
            val commentContent = etComment.text.toString()
            if(commentContent.isEmpty()) etComment.error = "Nhập comment"
            else {
                val comment = Comments(commentID, commentCurrentTime, commentContent)
                commentRef.child(commentID).setValue(comment)
                    .addOnCompleteListener {
                        Toast.makeText(this,"Gửi thành công !", Toast.LENGTH_SHORT).show()
                        etComment.text.clear()

                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(this,"Lỗi ${err.message} !", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnBack.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun openUpdateDialog(foodId: String, foodName: String) {
            val mDialog = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val mDialogView = inflater.inflate(R.layout.update_dialog, null)

            mDialog.setView(mDialogView)


            val etFoodName = mDialogView.findViewById<EditText>(R.id.etFoodName)
            val etFoodRecipe = mDialogView.findViewById<EditText>(R.id.etFoodRecipe)
            imFoodImageUpdate = mDialogView.findViewById<ImageView>(R.id.imFoodImgUpdate)
            val btnUpdateData = mDialogView.findViewById<Button>(R.id.btnUpdateData)
            val rtFoodRatingUpdate = mDialogView.findViewById<RatingBar>(R.id.rtFoodRatingUpdate)
            val btnUpdateImg = mDialogView.findViewById<Button>(R.id.btnChooseNewImg)
            val etFoodSecret = mDialogView.findViewById<EditText>(R.id.etFoodSecret)

            btnUpdateImg.setOnClickListener {
                changeImage.launch("image/*")
            }

            if(uri != null) {
                imFoodImageUpdate.setImageURI(uri)
            } else {
                val foodImg = intent.getStringExtra("foodImage")
                if (foodImg != null) {
                    Picasso.get().load(foodImg).into(imFoodImageUpdate)
                }
            }

            rtFoodRatingUpdate.rating = binding.rtFoodRating.rating
            etFoodName.setText(binding.tvFoodName.text.toString())
            etFoodRecipe.setText(binding.tvFoodRecipe.text.toString())
            etFoodSecret.setText(binding.tvFoodSecret.text.toString())

            mDialog.setTitle("Cập nhật $foodName")
            val alertDialog = mDialog.create()
            alertDialog.show()

            var currentRating = rtFoodRatingUpdate.rating
            rtFoodRatingUpdate.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (fromUser) {
                    currentRating = ratingBar.rating
                }
            }

            btnUpdateData.setOnClickListener {
                updateImageFood(foodId)
                updateFoodData(
                    foodId,
                    etFoodName.text.toString(),
                    etFoodRecipe.text.toString(),
                    currentRating,
                    etFoodSecret.text.toString()
                )
                Toast.makeText(
                    applicationContext,
                    "Đã cập nhật",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tvFoodName.text = etFoodName.text.toString()
                binding.tvFoodRecipe.text = etFoodRecipe.text.toString()
                binding.rtFoodRating.rating = rtFoodRatingUpdate.rating
                binding.tvFoodSecret.text = etFoodSecret.text.toString()
                if (uri != null) {
                    binding.imFoodImage.setImageURI(uri)
                }
                alertDialog.dismiss()
            }
    }

    fun getCurrentTime(): String {
        val currentTime = Date()
        val currentTimeFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        return currentTimeFormat.format(currentTime)
    }

    private fun updateImageFood(foodId : String) {
        uri?.let {
            storageRef.child(foodId).putFile(it)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url->
                            val dbRef = FirebaseDatabase.getInstance().getReference("Foods").child(foodId)
                            // Tạo một HashMap để chỉ cập nhật trường cụ thể bạn muốn thay đổi
                            val updateData = HashMap<String, Any>()
                            updateData["imgUri"] = url.toString()
                            dbRef.updateChildren(updateData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                } else {

                                }
                            }
                        }
                }
        }
    }

    private fun updateFoodData(
        foodId: String,
        foodName: String,
        foodRecipe: String,
        currentRating: Float,
        foodSecret: String
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Foods").child(foodId)
        // Tạo một HashMap để chỉ cập nhật trường cụ thể bạn muốn thay đổi
        val updateData = HashMap<String, Any>()
        updateData["foodName"] = foodName
        updateData["foodRecipe"] = foodRecipe
        updateData["foodRating"] = currentRating
        updateData["foodSecret"] = foodSecret
        dbRef.updateChildren(updateData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Cập nhật thành công
                Toast.makeText(applicationContext, "Đã cập nhật thành công", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Xử lý lỗi nếu có
                Toast.makeText(applicationContext, "Lỗi khi cập nhật dữ liệu", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun setValuesToView() {
        val foodImg = intent.getStringExtra("foodImage")
        if (foodImg != null) {
            Picasso.get().load(foodImg).into(binding.imFoodImage)
        }
        binding.tvFoodSecret.text = intent.getStringExtra("foodSecret")
        binding.tvFoodName.text = intent.getStringExtra("foodName")
        binding.tvFoodRecipe.text = intent.getStringExtra("foodRecipe")
        binding.rtFoodRating.rating = intent.getFloatExtra("foodRating", 0.0f)
    }
}