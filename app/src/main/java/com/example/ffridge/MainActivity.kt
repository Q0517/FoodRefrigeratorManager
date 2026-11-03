package com.example.ffridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: FoodDatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = FoodDatabaseHelper(this)
        setContent {
            FoodPage(dbHelper)
        }
    }
}

@Composable
fun FoodPage(helper: FoodDatabaseHelper) {
    var name by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }
    var addDate by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf("") }
    val foodList = remember { mutableStateListOf<Food>() }

    fun refreshList() {
        foodList.clear()
        foodList.addAll(helper.getAllFood())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "智能冰箱食材管理")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("食材名称") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = countStr,
            onValueChange = { countStr = it },
            label = { Text("数量") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = addDate,
            onValueChange = { addDate = it },
            label = { Text("入库日期 yyyy-MM-dd") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = expireDate,
            onValueChange = { expireDate = it },
            label = { Text("过期日期 yyyy-MM-dd") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val count = countStr.toIntOrNull()
                if (name.isNotBlank() && count != null && addDate.isNotBlank() && expireDate.isNotBlank()) {
                    helper.insertFood(name, count, addDate, expireDate)
                    name = ""
                    countStr = ""
                    addDate = ""
                    expireDate = ""
                    refreshList()
                }
            }) {
                Text("添加食材")
            }
            Button(onClick = { refreshList() }) {
                Text("刷新列表")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(foodList) { food ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Column {
                            Text("名称：${food.name}")
                            Text("数量：${food.count}")
                            Text("入库：${food.addDate}")
                            Text("过期：${food.expireDate}")
                        }
                        Button(onClick = {
                            helper.deleteFood(food.id)
                            refreshList()
                        }) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}
