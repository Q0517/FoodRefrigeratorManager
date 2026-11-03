package com.example.ffridge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FoodDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_FOOD = "food"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_EXPIRE = "expireDate"
        const val KEY_COUNT = "count"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE $TABLE_FOOD (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT,
                $KEY_EXPIRE TEXT,
                $KEY_COUNT INTEGER
            )
        """.trimIndent()
        db.execSQL(createSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOOD")
        onCreate(db)
    }

    // 添加食材
    fun addFood(name: String, expireDate: String, count: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)
            put(KEY_EXPIRE, expireDate)
            put(KEY_COUNT, count)
        }
        return db.insert(TABLE_FOOD, null, values)
    }

    // 查询全部食材
    fun getAllFood(): List<Food> {
        val list = mutableListOf<Food>()
        val db = readableDatabase
        val cursor = db.query(TABLE_FOOD, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
            val expire = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPIRE))
            val cnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT))
            list.add(Food(id, name, expire, cnt))
        }
        cursor.close()
        return list
    }

    // 修改食材
    fun updateFood(food: Food): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, food.name)
            put(KEY_EXPIRE, food.expireDate)
            put(KEY_COUNT, food.count)
        }
        return db.update(TABLE_FOOD, values, "$KEY_ID=?", arrayOf(food.id.toString()))
    }

    // 删除食材
    fun deleteFood(food: Food): Int {
        val db = writableDatabase
        return db.delete(TABLE_FOOD, "$KEY_ID=?", arrayOf(food.id.toString()))
    }
}
