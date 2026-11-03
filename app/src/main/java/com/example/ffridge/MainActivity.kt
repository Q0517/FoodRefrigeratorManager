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
import androidx.compose.runtime.LaunchedEffect
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
    val foodList = remember { mutableStateListOf<Food>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var expire by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }

    fun refreshData() {
        foodList.clear()
        foodList.addAll(helper.getAllFood())
    }

    // 页面打开先加载一次数据
    LaunchedEffect(Unit) {
        refreshData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "冰箱食材管理", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Button(onClick = { showAddDialog = true }) {
            Text("添加食材")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(foodList) { food ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("名称：${food.name}")
                            Text("数量：${food.count}")
                            Text("过期：${food.expireDate}")
                        }
                        Button(onClick = {
                            helper.deleteFood(food)
                            refreshData()
                        }) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增食材") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("食材名称") })
                    OutlinedTextField(value = expire, onValueChange = { expire = it }, label = { Text("过期日期") })
                    OutlinedTextField(value = countStr, onValueChange = { countStr = it }, label = { Text("数量") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val num = countStr.toIntOrNull() ?: 1
                    helper.addFood(name, expire, num)
                    showAddDialog = false
                    name = ""
                    expire = ""
                    countStr = ""
                    refreshData()
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
