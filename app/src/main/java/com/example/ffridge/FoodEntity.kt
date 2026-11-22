
package com.example.ffridge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_table")
data class FoodEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val count: Int,
    val shelfLife: String
)