package com.example.ffridge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FoodDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "FoodDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_FOOD = "food"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_COUNT = "count"
        private const val KEY_ADD_DATE = "addDate"
        private const val KEY_EXPIRE_DATE = "expireDate"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_FOOD (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT,
                $KEY_COUNT INTEGER,
                $KEY_ADD_DATE TEXT,
                $KEY_EXPIRE_DATE TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOOD")
        onCreate(db)
    }

    // 插入食材
    fun insertFood(name: String, count: Int, addDate: String, expireDate: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)
            put(KEY_COUNT, count)
            put(KEY_ADD_DATE, addDate)
            put(KEY_EXPIRE_DATE, expireDate)
        }
        val id = db.insert(TABLE_FOOD, null, values)
        db.close()
        return id
    }

    // 查询全部食材
    fun getAllFood(): MutableList<Food> {
        val list = mutableListOf<Food>()
        val db = readableDatabase
        val cursor = db.query(TABLE_FOOD, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
            val count = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT))
            val addDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADD_DATE))
            val expireDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPIRE_DATE))
            list.add(Food(id, name, count, addDate, expireDate))
        }
        cursor.close()
        db.close()
        return list
    }

    // 删除食材
    fun deleteFood(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_FOOD, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return result
    }
}
