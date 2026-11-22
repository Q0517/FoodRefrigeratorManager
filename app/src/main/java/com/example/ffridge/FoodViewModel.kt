
package com.example.ffridge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = FoodRepository(
        FoodDatabase.getDatabase(application).foodDao()
    )

    // 监听数据库中的食材
    val allFood: StateFlow<List<FoodEntity>> =
        repository.allFood.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // 添加食材
    fun insert(food: FoodEntity) {
        viewModelScope.launch {
            repository.insert(food)
        }
    }

    // 修改食材
    fun update(food: FoodEntity) {
        viewModelScope.launch {
            repository.update(food)
        }
    }

    // 删除食材
    fun delete(food: FoodEntity) {
        viewModelScope.launch {
            repository.delete(food)
        }
    }
}