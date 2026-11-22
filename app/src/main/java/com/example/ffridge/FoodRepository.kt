
package com.example.ffridge

import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao
) {

    // 获取所有食材
    val allFood: Flow<List<FoodEntity>> =
        foodDao.getAllFood()

    // 添加食材
    suspend fun insert(food: FoodEntity) {
        foodDao.insertFood(food)
    }

    // 修改食材
    suspend fun update(food: FoodEntity) {
        foodDao.updateFood(food)
    }

    // 删除食材
    suspend fun delete(food: FoodEntity) {
        foodDao.deleteFood(food)
    }
}