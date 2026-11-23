
package com.example.ffridge

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "food_table")
data class FoodEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val count: Int,
    val shelfLife: String,
    @ColumnInfo(defaultValue = "'冷藏室'")
    val location: String = "冷藏室"
)