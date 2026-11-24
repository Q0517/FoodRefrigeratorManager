
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

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
    val foodViewModel: FoodViewModel = viewModel()
    val foodData by foodViewModel.allFood.collectAsState()
    val foodList = foodData.map { food ->
        Food(
            food.id,
            food.name,
            food.count,
            food.shelfLife,
            food.location
        )
    }.toMutableList()

    var name by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }
    var shelf by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("冷藏室") }
    var filter by remember { mutableStateOf("全部") }
    var locationFilter by remember {
        mutableStateOf("全部")
    }
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

        val matchStatus = when (filter) {
            "已过期" -> days < 0
            "即将过期" -> days in 0L..3L
            "正常" -> days > 3
            else -> true
        }

        val matchLocation = when (locationFilter) {
            "全部" -> true
            else -> item.location == locationFilter
        }

        matchStatus && matchLocation
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
            Spacer(modifier = Modifier.height(4.dp))

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

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(4.dp))

            // 食材状态筛选
            Text("食材筛选")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { filter = "全部" },
                ) {
                    Text("全部")
                }

                TextButton(
                    onClick = { filter = "全部" },
                ) {
                    Text("已过期")
                }

                TextButton(
                    onClick = { filter = "全部" },
                ) {
                    Text("即将过期")
                }

                TextButton(
                    onClick = { filter = "全部" },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 输入数量
            OutlinedTextField(
                value = countStr,
                onValueChange = { countStr = it },
                label = { Text("数量") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 输入保质期
            OutlinedTextField(
                value = shelf,
                onValueChange = { shelf = it },
                label = { Text("保质期（yyyy-MM-dd）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
// 添加食材
            Text(
                text = "选择存放位置："
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { location = "冷藏室" },
                    modifier = Modifier.weight(1f),
                    colors = mintButtonColors
                ) {
                    Text(if (location == "冷藏室") "✓ 冷藏室" else "冷藏室")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { location = "冷冻室" },
                    modifier = Modifier.weight(1f),
                    colors = mintButtonColors
                ) {
                    Text(if (location == "冷冻室") "✓ 冷冻室" else "冷冻室")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { location = "冰箱门架" },
                    modifier = Modifier.weight(1f),
                    colors = mintButtonColors
                ) {
                    Text(if (location == "冰箱门架") "✓ 冰箱门架" else "冰箱门架")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { location = "果蔬区" },
                    modifier = Modifier.weight(1f),
                    colors = mintButtonColors
                ) {
                    Text(if (location == "果蔬区") "✓ 果蔬区" else "果蔬区")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
                                val newId = (foodData.maxOfOrNull { it.id } ?: 0) + 1

                                foodViewModel.insert(
                                    FoodEntity(
                                        newId,
                                        name.trim(),
                                        cnt,
                                        shelf,
                                        location
                                    )
                                )

                                message = "添加成功"
                            } else {
                                // 修改已有食材
                                val oldFood = editingFood!!

                                foodViewModel.update(
                                    FoodEntity(
                                        oldFood.id,
                                        name.trim(),
                                        cnt,
                                        shelf,
                                        location
                                    )
                                )

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
// 按存放位置筛选

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
                            Text(
                                text = "📍 存放位置：${food.location}",
                                color = Color(0xFF0288D1)
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
                                        foodViewModel.update(
                                            FoodEntity(
                                                food.id,
                                                food.name,
                                                food.count + 1,
                                                food.shelfLife,
                                                food.location
                                            )
                                        )
                                    }

                                ) {
                                    Text("+")
                                }

                                Button(
                                    colors = mintButtonColors,
                                    onClick = {
                                        if (food.count > 1) {
                                            foodViewModel.update(
                                                FoodEntity(
                                                    food.id,
                                                    food.name,
                                                    food.count - 1,
                                                    food.shelfLife,
                                                    food.location
                                                )
                                            )
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
                                    foodViewModel.delete(
                                        FoodEntity(
                                            food.id,
                                            food.name,
                                            food.count,
                                            food.shelfLife
                                        )
                                    )
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
                                    location = food.location
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
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📍 比奇堡冰箱位置管理",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF0277BD)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("选择存放位置：")

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("全部", "冷藏室", "冷冻室").forEach { place ->
                        Button(
                            onClick = { locationFilter = place },
                            modifier = Modifier.weight(1f),
                            colors = mintButtonColors
                        ) {
                            Text(
                                if (locationFilter == place) "✓ $place"
                                else place
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("冰箱门架", "果蔬区").forEach { place ->
                        Button(
                            onClick = { locationFilter = place },
                            modifier = Modifier.weight(1f),
                            colors = mintButtonColors
                        ) {
                            Text(
                                if (locationFilter == place) "✓ $place"
                                else place
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val locationList = if (locationFilter == "全部") {
                    foodList
                } else {
                    foodList.filter { it.location == locationFilter }
                }

                Text("当前食材：${locationList.size} 种")

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(items = locationList) { food ->
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
                                Text("食材名称：${food.name}")
                                Text("数量：${food.count}")
                                Text("📍 存放位置：${food.location}")
                                Text("保质期：${food.shelfLife}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val newLocation = when (food.location) {
                                            "冷藏室" -> "冷冻室"
                                            "冷冻室" -> "冰箱门架"
                                            "冰箱门架" -> "果蔬区"
                                            else -> "冷藏室"
                                        }

                                        foodViewModel.update(
                                            FoodEntity(
                                                food.id,
                                                food.name,
                                                food.count,
                                                food.shelfLife,
                                                newLocation
                                            )
                                        )
                                    },
                                    colors = mintButtonColors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("修改存放位置")
                                }
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
                label = { Text("位置管理") }
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