
package com.example.ffridge

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FridgePage()
        }
    }
}

// 计算距离过期还有多少天
fun getRemainingDays(shelfLife: String): Long? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        format.isLenient = false
        format.timeZone = TimeZone.getTimeZone("UTC")

        val expiryDate = format.parse(shelfLife) ?: return null
        val todayText = format.format(Date())
        val today = format.parse(todayText) ?: return null

        TimeUnit.MILLISECONDS.toDays(
            expiryDate.time - today.time
        )
    } catch (e: Exception) {
        null
    }
}

// 检查日期格式是否正确
fun isValidDate(date: String): Boolean {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        format.isLenient = false
        format.parse(date)?.let {
            format.format(it) == date
        } ?: false
    } catch (e: Exception) {
        false
    }
}

// 获取食材状态
fun getFoodStatus(days: Long): String {
    return when {
        days < 0 -> "已过期"
        days <= 3 -> "即将过期"
        else -> "正常"
    }
}

@Composable

fun FridgePage() {

    val foodList = remember {
        mutableStateListOf(
            Food(1, "鸡蛋", 6, "2026-12-01"),
            Food(2, "牛奶", 4, "2026-11-20")
        )
    }

    var name by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }
    var shelf by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("全部") }
    var message by remember { mutableStateOf("") }

    // 统计各种状态的数量
    val expiredCount = foodList.count {
        (getRemainingDays(it.shelfLife) ?: 0L) < 0
    }

    val soonCount = foodList.count {
        val days = getRemainingDays(it.shelfLife) ?: 999L
        days in 0L..3L
    }

    val normalCount = foodList.count {
        (getRemainingDays(it.shelfLife) ?: -999L) > 3
    }

    // 根据筛选条件显示食材
    val filteredList = foodList.filter { item ->
        val days = getRemainingDays(item.shelfLife) ?: 999L

        when (filter) {
            "已过期" -> days < 0
            "即将过期" -> days in 0L..3L
            "正常" -> days > 3
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "冰箱食材过期管理",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "食材状态统计",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("已过期：${expiredCount}种", color = Color.Red)

            Text("即将过期：${soonCount}种", color = Color(0xFFFF9800))

            Text("正常：${normalCount}种", color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(12.dp))

        // 食材状态筛选
        Text("食材筛选")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { filter = "全部" }) {
                Text("全部")
            }

            TextButton(onClick = { filter = "已过期" }) {
                Text("已过期")
            }

            TextButton(onClick = { filter = "即将过期" }) {
                Text("即将过期")
            }

            TextButton(onClick = { filter = "正常" }) {
                Text("正常")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // 输入食材名称
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("食材名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 输入数量
        OutlinedTextField(
            value = countStr,
            onValueChange = { countStr = it },
            label = { Text("数量") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 输入保质期
        OutlinedTextField(
            value = shelf,
            onValueChange = { shelf = it },
            label = { Text("保质期（yyyy-MM-dd）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))


// 添加食材
        Button(
            onClick = {
                val cnt = countStr.toIntOrNull()

                when {
                    name.isBlank() -> {
                        message = "请输入食材名称"
                    }

                    cnt == null || cnt <= 0 -> {
                        message = "数量必须是大于0的整数"
                    }

                    !isValidDate(shelf) -> {
                        message = "日期格式错误，请输入 yyyy-MM-dd"
                    }

                    else -> {
                        val newId =
                            (foodList.maxOfOrNull { it.id } ?: 0) + 1

                        foodList.add(
                            Food(newId, name.trim(), cnt, shelf)
                        )

                        name = ""
                        countStr = ""
                        shelf = ""
                        message = "添加成功"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()

        ) {
            Text("添加食材")
        }

        // 显示食材列表及过期状态
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredList) { food ->

                val days = getRemainingDays(food.shelfLife)

                val status = when {
                    days == null -> "日期无效"
                    days < 0 -> "已过期"
                    days <= 3 -> "即将过期"
                    else -> "正常"
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "食材名称：${food.name}"
                        )

                        Text(
                            text = "数量：${food.count}"
                        )

                        Text(
                            text = "保质期：${food.shelfLife}"
                        )

                        Text(
                            text = "状态：$status",
                            color = when {
                                days == null -> Color.Gray
                                days < 0 -> Color.Red
                                days <= 3 -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            }
                        )

                        Text(
                            text = when {
                                days == null -> "剩余时间：日期无效"
                                days < 0 -> "已超过保质期 ${-days} 天"
                                days == 0L -> "今天到期"
                                else -> "剩余 ${days} 天"
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

// 删除食材按钮
                        Button(
                            onClick = {
                                foodList.remove(food)
                                message = "已删除：${food.name}"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("删除食材")
                        }
                    }
                }
            }
        }
    }
}