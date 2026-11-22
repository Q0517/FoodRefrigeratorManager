
package com.example.ffridge

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // 查询所有食材
    @Query("SELECT * FROM food_table")
    fun getAllFood(): Flow<List<FoodEntity>>

    // 添加食材
    @Insert
    suspend fun insertFood(food: FoodEntity)

    // 修改食材
    @Update
    suspend fun updateFood(food: FoodEntity)

    // 删除食材
    @Delete
    suspend fun deleteFood(food: FoodEntity)
}