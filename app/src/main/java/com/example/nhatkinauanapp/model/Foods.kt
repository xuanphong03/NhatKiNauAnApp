package com.example.nhatkinauanapp.model

data class Foods(
    val foodId : String? = null,
    val foodName : String? = null,
    val foodRecipe: String? = null,
    val imgUri: String? = null,
    val createTime: String? = null,
    val foodRating: Float? = 0.0f,
    val foodSecret: String? = null
)
