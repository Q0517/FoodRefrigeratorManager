
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
import androidx.compose.ui.Alignment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

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

    var selectedPage by remember { mutableStateOf(0) }
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
    var editingFood by remember { mutableStateOf<Food?>(null) }

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
// 按照剩余保质期排序
    val sortedList = filteredList.sortedBy {
        getRemainingDays(it.shelfLife) ?: Long.MAX_VALUE
    }
    val mintButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFFD928),
        contentColor = Color(0xFF573D16)
    )

    val mintTextButtonColors = ButtonDefaults.textButtonColors(
        contentColor = Color(0xFF0288D1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1F5FE))
            .padding(16.dp)
    ) {
        // 首页
        if (selectedPage == 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "🍍 欢迎来到比奇堡！",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF0277BD)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🧽 海绵宝宝的冰箱管理站",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8D6)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text("📦 食材总数：${foodList.size} 种")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🔴 已过期：${expiredCount} 种")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🟠 即将过期：${soonCount} 种")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🟢 正常：${normalCount} 种")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "记得及时检查冰箱里的食材哦！",
                    color = Color(0xFF0277BD)
                )
            }
        }
        if (selectedPage == 1) {
            Text(
                text = "🍍 比奇堡冰箱管理站 🧽",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD),
                    fontSize = 23.sp
                )
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
                TextButton(
                    onClick = { filter = "全部" },
                    colors = mintTextButtonColors
                ) {
                    Text("全部")
                }

                TextButton(
                    onClick = { filter = "全部" },
                    colors = mintTextButtonColors
                ) {
                    Text("已过期")
                }

                TextButton(
                    onClick = { filter = "全部" },
                    colors = mintTextButtonColors
                ) {
                    Text("即将过期")
                }

                TextButton(
                    onClick = { filter = "全部" },
                    colors = mintTextButtonColors
                ) {
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
            Spacer(modifier = Modifier.height(8.dp))
            // 输入数量
            OutlinedTextField(
                value = countStr,
                onValueChange = { countStr = it },
                label = { Text("数量") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                            if (editingFood == null) {
                                // 添加新食材
                                val newId = (foodList.maxOfOrNull { it.id } ?: 0) + 1

                                foodList.add(
                                    Food(newId, name.trim(), cnt, shelf)
                                )

                                message = "添加成功"
                            } else {
                                // 修改已有食材
                                val oldFood = editingFood!!

                                val index = foodList.indexOfFirst {
                                    it.id == oldFood.id
                                }

                                if (index != -1) {
                                    foodList[index] = Food(
                                        oldFood.id,
                                        name.trim(),
                                        cnt,
                                        shelf
                                    )
                                }

                                message = "修改成功"
                            }

                            name = ""
                            countStr = ""
                            shelf = ""
                            editingFood = null
                        }
                    }
                },
                colors = mintButtonColors,
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(if (editingFood == null) "添加食材" else "保存修改")
            }
            Text(
                text = message,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (editingFood != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        name = ""
                        countStr = ""
                        shelf = ""
                        editingFood = null
                        message = "已取消修改"
                    },
                    colors = mintButtonColors,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消修改")
                }
            }

            // 显示食材列表及过期状态

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(items = sortedList) { food ->

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
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8D6)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "食材名称：${food.name}"
                            )


                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "数量：${food.count}"
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    colors = mintButtonColors,
                                    onClick = {
                                        val index = foodList.indexOfFirst {
                                            it.id == food.id
                                        }

                                        if (index != -1) {
                                            foodList[index] = food.copy(
                                                count = food.count + 1
                                            )
                                        }
                                    }

                                ) {
                                    Text("+")
                                }

                                Button(
                                    colors = mintButtonColors,
                                    onClick = {
                                        if (food.count > 1) {
                                            val index = foodList.indexOfFirst {
                                                it.id == food.id
                                            }

                                            if (index != -1) {
                                                foodList[index] = food.copy(
                                                    count = food.count - 1
                                                )
                                            }
                                        } else {
                                            message = "食材数量不能小于1"
                                        }
                                    }
                                ) {
                                    Text("-")
                                }
                            }


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
                                colors = mintButtonColors,
                                onClick = {
                                    foodList.remove(food)
                                    message = "已删除：${food.name}"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("删除食材")

                            }

                            Spacer(modifier = Modifier.height(8.dp))

// 修改食材按钮
                            Button(
                                onClick = {
                                    editingFood = food
                                    name = food.name
                                    countStr = food.count.toString()
                                    shelf = food.shelfLife
                                    message = "正在修改：${food.name}"
                                },
                                colors = mintButtonColors,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("修改食材")
                            }
                        }
                    }
                }
            }
            // 原来的食材管理代码到这里结束
        }
        // 过期提醒页面
        if (selectedPage == 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🔔 比奇堡食材提醒",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF0277BD)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val reminderList = foodList.filter {
                    val days = getRemainingDays(it.shelfLife)
                    days != null && days <= 3
                }

                if (reminderList.isEmpty()) {
                    Text("🎉 暂时没有需要提醒的食材！")
                } else {
                    reminderList.forEach { food ->
                        val days = getRemainingDays(food.shelfLife)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF1D6)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("食材：${food.name}")
                                Text("数量：${food.count}")
                                Text("保质期：${food.shelfLife}")

                                Text(
                                    text = if (days!! < 0) {
                                        "⚠️ 已过期 ${-days} 天"
                                    } else {
                                        "⚠️ 剩余 ${days} 天到期"
                                    },
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (selectedPage != 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
        // 我的页面
        if (selectedPage == 3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "🧽",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "海绵宝宝的冰箱管家",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0277BD)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF1D6)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text("📦 食材总数：${foodList.size} 种")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🔔 需要提醒：${expiredCount + soonCount} 种")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🎨 主题：海绵宝宝")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📱 版本：1.0")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "感谢使用比奇堡冰箱管理站！",
                    color = Color(0xFF0277BD)
                )
            }
        }
        NavigationBar(
            containerColor = Color(0xFFFFF8D6)
        ) {
            NavigationBarItem(
                selected = selectedPage == 0,
                onClick = { selectedPage = 0 },
                icon = { Text("🏠") },
                label = { Text("首页") }
            )

            NavigationBarItem(
                selected = selectedPage == 1,
                onClick = { selectedPage = 1 },
                icon = { Text("🧊") },
                label = { Text("食材") }
            )

            NavigationBarItem(
                selected = selectedPage == 2,
                onClick = { selectedPage = 2 },
                icon = { Text("🔔") },
                label = { Text("提醒") }
            )

            NavigationBarItem(
                selected = selectedPage == 3,
                onClick = { selectedPage = 3 },
                icon = { Text("👤") },
                label = { Text("我的") }
            )
        }
    }
}