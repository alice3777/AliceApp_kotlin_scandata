package com.alice377.alice377_android_kotlin.prividers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alice377.alice377_android_kotlin.Alice377_android_kotlin.Companion.appname

class AppLogDb(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {

    companion object { //同java中的static
        private val DB_NAME: String = "alice377.db" //資料庫名稱
        private val VERSION: Int = 1 //資料庫版本，資料庫結構改變時要更改數字，通常+1
        private val DB_TABLE: String = "alice377_user_action" //資料表名稱
        private val crTBsql: String = "CREATE TABLE $DB_TABLE (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "action_view TEXT NOT NULL," +
                                      "app_name TEXT NOT NULL," +
                                      "action_action TEXT NOT NULL," +
                                      "action_date TEXT NOT NULL," +
                                      "status TEXT NOT NULL," +
                                      "insert_date TEXT NOT NULL)"
        private var db_alice377_list: SQLiteDatabase? = null //資料庫名稱
        var action_view: String = "" //記錄使用者所在view
        var action_action: String = "" //記錄使用者執行動作的時間
        var action_date: String = "" //記錄使用者執行動作的時間
        var status: Int = 0 //記錄執行結果
        var insert_date: String = "" //記錄資料寫入的時間

        //需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
        fun getDatabase(context: Context): SQLiteDatabase? {

            if (db_alice377_list == null || !db_alice377_list!!.isOpen) {
                db_alice377_list = AppLogDb(context, DB_NAME,null, VERSION).writableDatabase
            }

            return db_alice377_list
        }

        //直接傳入SQLite語法做查詢
        fun rawquery(context: Context, sql: String, selectionArgs: Array<String>): Cursor? {
            getDatabase(context) //開啟資料庫
            var c: Cursor? = null

            try {
                 c = db_alice377_list?.rawQuery(sql, selectionArgs)
            }catch (e: Exception){
                e.toString()
            }

            return c
        }

        //新增資料：寫log
        fun insert(context: Context): Long{
            getDatabase(context) //開啟資料庫
            val newRow: ContentValues = ContentValues()
            newRow.put("app_name", appname)
            newRow.put("action_view", action_view)
            newRow.put("action_action", action_action)
            newRow.put("action_date", action_date)
            newRow.put("status", status)
            newRow.put("insert_date", insert_date)
            val rowId: Long = db_alice377_list?.insert(DB_TABLE,null, newRow) ?: 0
            db_alice377_list!!.close() //寫入完成關閉

            return rowId
        }

        @SuppressLint("Recycle")
        //刪除資料：殺log
        fun delete(context: Context, where: String): Int{
            getDatabase(context) //開啟資料庫
            val sql: String = "SELECT * FROM $DB_TABLE"
            val c: Cursor? = db_alice377_list?.rawQuery(sql,null)

            return if (c?.count ?: 0 != 0){
                c!!.close()
                val rowsAffected: Int = db_alice377_list?.delete(DB_TABLE, where,null) ?: 0
                db_alice377_list!!.close()

                rowsAffected

            }else{
                c!!.close()
                db_alice377_list!!.close()

                -1
            }

        }

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(crTBsql)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }

    //資料庫升版：系統自動偵測調用
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE if exists $DB_TABLE")
        onCreate(db)
    }

}