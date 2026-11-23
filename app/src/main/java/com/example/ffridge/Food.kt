package com.example.ffridge

data class Food(
    val id: Int,
    val name: String,
    val count: Int,
    val shelfLife: String,
    val location: String = "冷藏室"
)