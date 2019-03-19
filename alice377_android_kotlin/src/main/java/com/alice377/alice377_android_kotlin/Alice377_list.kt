package com.alice377.alice377_android_kotlin

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.alice377_list.*
import android.widget.*
import com.alice377.alice377_android_kotlin.Alice377_android_kotlin.Companion.appname
import com.alice377.alice377_android_kotlin.Alice377_android_kotlin.Companion.catchdb
import com.alice377.alice377_android_kotlin.Alice377_android_kotlin.Companion.langnum
import com.alice377.alice377_android_kotlin.Alice377_android_kotlin.Companion.mobiletoday
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_action
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_date
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_view
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.delete
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.insert
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.insert_date
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.rawquery
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.status
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Alice377_list: AppCompatActivity() {

    private val context: Context = this
    private var today: String = "" //記錄今天的日期
    private lateinit var alice377view: View //宣告自定義dialog
    private lateinit var alice377AlertDialog: Alice377AlertDialog
    private lateinit var alice377AlertDialog2: Alice377AlertDialog
    private lateinit var m_alice377_list_login_E_account: EditText
    private lateinit var m_alice377_list_login_E_password: EditText
    private lateinit var m_sql_E_write: EditText
    private lateinit var m_table_S_db: Spinner
    private lateinit var m_table_S_table: Spinner
    private lateinit var m_table_E_sql_where: EditText
    private lateinit var m_table_E_sql_order: EditText
    private val classname: String = "Alice377_list"
    private lateinit var msgcolor: SpannableString //宣告字串顏色方法變數
    private var Col_name = ""
    private val TAG = "alice377 =>"
    private var table_page: Int = 0 //記錄工程模式-資料表查詢頁面：1=使用中
    private var sql_where = "" //儲存使用者輸入的sqlite_where語法
    private var sql_order = "" //儲存使用者輸入的sql_order語法
    private var first_create = 0 //記錄SQLite_table查詢初次使用情形：0=初次使用

    //連線及SQLite相關宣告----------------------------------------------------------------------------
    private lateinit var mContRes: ContentResolver
    private val USERCOLUMN: Array<String> = arrayOf("id","app_name","action_view","action_action",
                                                    "action_date","status","insert_date")
    private val ALICE377DATACOLUMN: Array<String> = arrayOf("id","so_date","so_nbr","so_nbr_step")
    private var tablecolumn = USERCOLUMN //預設為user_action
    private val singleThreadExecutor: Executor = Executors.newSingleThreadExecutor()
    private var recSet: ArrayList<String> = ArrayList()
    private var reclist: ArrayList<String> = ArrayList()
    private var mList: ArrayList<Map<String, Any>> = ArrayList()
    private var data: Int = 0 //儲存SQLite撈到的總資料筆數
    private var data_show: Int = 0 //儲存SQLite顯示的資料筆數(最多100筆)
    private var user_write: String = "" //儲存使用者輸入的SQLite語法
    //----------------------------------------------------------------------------------------------

    private lateinit var db_spinner: ArrayAdapter<String> //存放資料庫陣列
    private lateinit var table_spinner: ArrayAdapter<String> //存放資料表陣列
    private val db_name = arrayOf("打包區","電鍍委外","維修通報","維修助手") //資料庫中文清單
    private val db_id = arrayOf("packing_scan.db","Elec_com.db","main_bull.db","main_at.db") //資料庫id清單
    private val table_id = arrayOf("packing_scan","electroplating_commission",
                                                "maintenance_bulletin","maintenance_assistant") //資料表id清單
    private var table_num = 0 //預設打包區
    private val packing_scan = arrayOf("台北廠","雲科廠") //打包區資料表中文清單
    private val packing_scan_id = arrayOf("packing_scan_NJ","packing_scan_RP") //打包區db資料表清單
    private val electroplating_commission = arrayOf("台北廠","雲科廠") //電鍍委外資料表中文清單
    private val electroplating_commission_id = arrayOf("Elec_com_NJ","Elec_com_RP") //電鍍委外db資料表清單
    private val maintenance_bulletin = arrayOf("報修主表","報修子表","teamplus訊息表") //維修通報資料表中文清單
    private val maintenance_bulletin_id = arrayOf("main_bull_sentdata_m","main_bull_sentdata_d",
                                                                "main_bull_sentteamplus") //維修通報db資料表清單
    private val maintenance_assistant = arrayOf("報修主表","報修子表","報修teamplus訊息表","維修主表",
                                                "維修子表","維修teamplus訊息表") //維修助手資料表中文清單
    private val maintenance_assistant_id = arrayOf("main_at_sentdata_m","main_at_sentdata_d",
                                                   "main_at_sentteamplus","main_at_main_data_m",
                                                   "main_at_main_data_d","main_at_main_data_teamplus") //維修助手db資料表清單
    private var subtable_num = 0 //預設台北
    private var exitTime: Long = 0

    companion object {
        var myselection: String = ""
        var myorder: String = "id DESC" //排序欄位
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alice377_list)

        catchdb() //抓資料庫執行緒
        setupViewComponent() //自定義
    }

    private fun setupViewComponent() {
        today = mobiletoday("yyyy/M/d") //取得手機今天日期
        alice377_list_T_gettoday.text = today
        alice377_list_T_gettoday.setTextColor(ContextCompat.getColor(context, R.color.blue))

        //Copyright加入年份區間----------------------------------------------------------------------
        val startyearnun = getString(R.string.alice377_list_t_copyright).indexOf("8")
        var copyrightyear = getString(R.string.alice377_list_t_copyright).substring(0, startyearnun + 1)
        copyrightyear += "-" + mobiletoday("yyyy") + getString(R.string.alice377_list_t_copyright).substring(
            startyearnun + 1)
        alice377_list_T_copyright.text = copyrightyear
        alice377_list_T_copyright.setTextColor(ContextCompat.getColor(context, R.color.alice377_copyright))
        //------------------------------------------------------------------------------------------

        layoutstart() //layout初始化設定
        alice377dialog() //初始化工程模式登入視窗
        cleardata() //SQLite僅保留七天資料，其餘清掉 ※注意：執行後資料排序會改變，注意撈資料庫行為要在此method執行完再做

        //加入文字並給予顏色--------------------------------------------------------------------------
        var newmsg = "\n《點一下輸入帳號密碼！》"

        if (langnum == 1) //簡體
            newmsg = "\n《点一下输入帐号密码！》"

        val msg = resources.getString(R.string.alice377_list_t_loginmsg) + newmsg
        val redstart = resources.getString(R.string.alice377_list_t_loginmsg).length
        val redend = msg.length

        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),redstart,redend,0) //紅色提醒
        alice377_list_T_loginmsg.setText(msgcolor, TextView.BufferType.SPANNABLE)
        //------------------------------------------------------------------------------------------

        alice377_list_T_loginmsg.setOnClickListener(showlogin()) //login提示訊息監聽
    }

    //layout初始化設定
    private fun layoutstart() {
        alice377_list_R_logo.visibility = View.VISIBLE //顯示Logo頁面
        alice377_list_R_rel.visibility = View.GONE //殺掉工程模式頁面
        alice377_list_R_query.visibility = View.GONE //殺掉工程模式-資料表查詢頁面
    }

    //初始化工程模式登入視窗
    @SuppressLint("InflateParams")
    private fun alice377dialog() {
        alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_login,null)

        //設定選單選擇視窗
        alice377AlertDialog = Alice377AlertDialog(context)
        alice377AlertDialog.setView(alice377view,0,0,0,
                0) //設定自定義layout
        m_alice377_list_login_E_account = alice377view.findViewById(R.id.alice377_list_login_E_account) //帳號
        m_alice377_list_login_E_password = alice377view.findViewById(R.id.alice377_list_login_E_password) //密碼
        alice377AlertDialog.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params: WindowManager.LayoutParams = alice377AlertDialog.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog.window.attributes = params //dialog參數綁定

        alice377log("Alice377_list login_layout start.",1) //寫log
    }

    //寫log
    private fun alice377log(getaction: String, getstatus: Int){
        action_view = classname
        action_action = getaction
        action_date = mobiletoday("yyyy/M/d HH:mm:ss")
        status = getstatus //0=失敗,1=成功
        insert_date = mobiletoday("yyyy/M/d")
        insert(context) //寫入資料
    }

    //SQLite掃描資料只保留最近七天的資料：避免log資料過於龐大
    private fun cleardata() {

        //開單一執行緒清資料
        singleThreadExecutor.execute(Runnable {
            val sqlite: String = "SELECT insert_date FROM alice377_user_action ORDER BY id DESC"
            val cur: Cursor? = rawquery(context, sqlite, emptyArray())
            cur?.moveToFirst() //?:表示不是null時執行moveToFirst
            val data: Int = cur?.count ?: 0

            if (data > 0){

                //設定暫存Array內容
                reclist = ArrayList<String>()
                var str: String = "" //記錄上一筆string

                while(!cur!!.isAfterLast){ //!!:非空斷言，絕對不是空值或null
                    val fldset: String = cur.getString(0)

                    if (str != fldset){ //insert日期不同時再紀錄
                        reclist.add(fldset) //存放到arraylist中
                        str = fldset
                    }

                    cur.moveToNext()
                }

                val j = reclist.size

                if (j > 7){

                    for (i in 7..(j - 1)){
                        myselection = "insert_date = '${reclist.get(i)}'"
                        val k = delete(context, myselection)

                        if (k == 0) //出現錯誤就停止
                            break
                    }

                }
            }

            cur?.close()

        })

    }

    //login提示訊息監聽
    private fun showlogin(): View.OnClickListener? = View.OnClickListener{
        alice377dialog() //開啟工程模式登入視窗
        alice377log("$classname login_layout start.",1) //寫log
    }

    //alice377AlertDialog上的login按鈕
    fun login(view: View) {
        val account = m_alice377_list_login_E_account.text.toString().trim { it <= ' ' }
        val password = m_alice377_list_login_E_password.text.toString().trim { it <= ' ' }
//        Log.d(TAG,"account=$account ,password=$password")

        if (account != "" && password != "" && account == "sa" && password == "password"){ //驗證帳密,alice377_acget(account) && alice377_pwget(password):要再寫
            Toast.makeText(context,"登入成功", Toast.LENGTH_SHORT).show()
            alice377log("Accout:******** login success.",1) //寫log
            actionshow() //顯示使用者活動列表
            alice377_list_R_logo.visibility = View.GONE //殺掉Logo頁面
            alice377_list_R_rel.visibility = View.VISIBLE //顯示工程模式頁面
            alice377AlertDialog.dismiss() //關閉dialog

        }else{
            var msg = "帳號密碼輸入錯誤！"

            if (langnum == 1) //簡體
                msg = "帐号密码输入错误！"

            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            m_alice377_list_login_E_password.setText("") //清空輸入的密碼
            alice377log("Accout:******** login failure.",0) //寫log
        }

    }

    //顯示使用者活動列表
    private fun actionshow() {

        //開工作執行緒撈SQLite掃描資料
        singleThreadExecutor.execute {
            var error = 0
            var sql = "SELECT * FROM alice377_user_action WHERE 1=1 AND app_name = '$appname'" +
                             " ORDER BY action_date DESC" //最多顯示100筆
            try {
                val c = rawquery(context, sql, emptyArray())
                c!!.moveToFirst()
                data = c.count //記錄總資料筆數
                c.close() //用完關閉

                //使用SQLite語法撈資料:context=這支內容,uri=哪張table
                sql = "SELECT * FROM alice377_user_action WHERE 1=1 AND app_name = '$appname' ORDER BY " +
                        "id DESC LIMIT 100" //最多顯示100筆

                if (user_write != "")
                    sql = user_write
                else
                    user_write = sql

                val cur = rawquery(context, sql, emptyArray())
                data_show = cur!!.count

                if (data_show > 0){ //有資料,設定筆數
                    val ColName: Array<String> = cur.columnNames
                    cur.moveToFirst()

                    //設定listview內容
                    recSet = ArrayList() //重設recSet的值為空
                    val columnCount = cur.columnCount
                    mList = ArrayList() //重設mList的值為空

                    while (!cur.isAfterLast){
                        val fldSet = StringBuilder()

                        for (ii in 0..(columnCount - 1)){
                            fldSet.append(cur.getString(ii))

                            if (ii < (columnCount - 1))
                                fldSet.append("#")
                        }

                        recSet.add(fldSet.toString()) //存放到arraylist中
                        cur.moveToNext()
                    }

                    cur.close() //用完關掉

                    for (i in 0..(recSet.size - 1)){
                        val item: HashMap<String, Any> = HashMap()
                        val fld: Array<String> = recSet[i].split("#").toTypedArray()
                        var str = ""

                        for (k in 1..(fld.size)){

                            when(k){
                                1 -> {
                                    changename(ColName[k - 1])
                                    str += Col_name + fld[k - 1]
                                }

                                2,3,4,5,7 -> {
                                    changename(ColName[k - 1])
                                    str += "\n" + Col_name + fld[k - 1]
                                }

                                6 -> {
                                    var status_CH = "成功"

                                    if (fld[5] == "0")
                                        status_CH = "失敗"

                                    changename(ColName[k - 1])
                                    str += "\n" + Col_name + status_CH
                                }

                            }

                        }

                        item["textview"] = str //"APP名稱：" + fld[1] + "\n執行介面：" + fld[2] + "\n執行動作：" + fld[3] + "\n執行時間：" + fld[4] + "\n執行結果：" + status_CH
                        mList.add(item)
                    }

                }else {
                    cur.close()
                    mList = ArrayList() //重設mList的值為空
                }

            }catch (e: Exception){
                Log.d(TAG, "error=$e")
                error = 1
            }

            //更新UI
            runOnUiThread {
                //===========設定Listview==========//
                val adapter: SimpleAdapter = SimpleAdapter(
                        context,
                        mList,
                        R.layout.alice377_list_item,
                        arrayOf("textview"),
                        intArrayOf(R.id.alice377_list_item_T_data)
                )
                //--------------------
                adapter.notifyDataSetChanged() //通知UI更新數據
                alice377_list_L_menu.adapter = adapter
                alice377_list_L_menu.isEnabled = true

                if (error == 1)
                    Toast.makeText(context, "語法輸入錯誤", Toast.LENGTH_SHORT).show()
                else
                    textmsg() //設定訊息框
            }

        }

    }

    //欄位名稱轉換為中文
    private fun changename(s: String): String {

        when(s){
            "id" -> Col_name = "id："
            "action_view" -> Col_name = "APP名稱："
            "app_name" -> Col_name = "執行介面："
            "action_action" -> Col_name = "執行動作："
            "action_date" -> Col_name = "執行時間："
            "status" -> Col_name = "執行結果："
            "insert_date" -> Col_name = "儲存日期："
        }

        return Col_name
    }

    //設定訊息框
    private fun textmsg() {
        var msg = "目前共有${data}筆資料，顯示最新${data_show}筆"
        val msgbluestart = "目前共有"
        var msg2 = "資料已更新"

        if (langnum == 1){ //簡體
            msg = "目前共有${data}笔资料，显示最新${data_show}笔"
            msg2 = "资料已更新"
        }

        val bluestart = msgbluestart.length
        val blueend = data.toString().length
        val redstart = ("目前共有${data}筆資料，顯示最新").length
        val redend = data_show.toString().length

        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), bluestart,
                bluestart + blueend,0) //藍色表示
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)), redstart,
                redstart + redend,0) //紅色表示

        alice377_list_T_msg.setText(msgcolor, TextView.BufferType.SPANNABLE)
        Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
    }

    //alice377AlertDialog上的yes按鈕
    fun yesbtn(view: View) {
        alice377log("User click yesbtn to finish this.",1) //寫log
        alice377AlertDialog.dismiss() //關閉dialog視窗
        Toast.makeText(context,"已離開工程模式", Toast.LENGTH_SHORT).show()
        this.finish()
    }

    //alice377AlertDialog上的calcel按鈕
    fun cancelbtn(view: View) {
        alice377AlertDialog.dismiss() //關閉dialog視窗
    }

    //工程模式title右邊的image
    @SuppressLint("InflateParams")
    fun I_setting(view: View) {
        alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_menu,null) //自定義Layout:dialog
        alice377AlertDialog = Alice377AlertDialog(context)
        alice377AlertDialog.setView(alice377view,0,0,0,
                0)
        alice377AlertDialog.show()

        //自定義Dialog視窗參數
        val params: WindowManager.LayoutParams = alice377AlertDialog.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog.window.attributes = params //dialog參數綁定
    }

    //工程模式選單：SQL查詢
    @SuppressLint("InflateParams")
    fun sqlbtn(view: View) {
        alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_menu_sql,null) //自定義Layout:dialog
        alice377AlertDialog2 = Alice377AlertDialog(context)
        alice377AlertDialog2.setView(alice377view,0,0,0,
                0)
        m_sql_E_write = alice377view.findViewById(R.id.sql_E_write) //SQLite_where以後語法輸入框
        val begin = "SELECT * FROM alice377_user_action ".length

        //讀取資料----------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        val str = textsetdata.getString("SQLite_set","")
//        Log.d(TAG,"str=$str")

        if (str != "") //儲存值為空
            user_write = str.substring(begin) //載入預設值
        else
            user_write = user_write.substring(begin)

//        Log.d(TAG,"user_write=$user_write")
        //-----------------------------------------------------------------------------------

        m_sql_E_write.setText(user_write) //顯示儲存的SQLite語法
        alice377AlertDialog2.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alice377AlertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog2.window.attributes = params //dialog參數綁定

        alice377AlertDialog.dismiss() //關閉dialog
    }

    //SQL查詢視窗：查詢紐
    fun querybtn(view: View) {
        val str = m_sql_E_write.text.toString() //重新取得值

        when(str.substring(0,1)){
            "w","W","o","O" -> {
                user_write = "SELECT * FROM alice377_user_action $str"
                actionshow() //依使用者輸入的語法重新查詢資料

                //儲存SQLite語法資料------------------------------------------------------------------
                val textsetdata = getSharedPreferences("TEXT_SET",0)
                textsetdata.edit().putString("SQLite_set", user_write).apply()
                //-----------------------------------------------------------------------------------
            }

            else -> {
                var msg = "發生錯誤，請輸入where條件或order by語法"

                if (langnum == 1) //簡體
                    msg = "发生错误，请输入where条件或order by语法"

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        table_page = 0 //工程模式-資料表查詢頁面關閉
        alice377_list_R_rel.visibility = View.VISIBLE //顯示工程模式頁面
        alice377_list_R_query.visibility = View.GONE //殺掉工程模式-資料表查詢頁面
        alice377AlertDialog2.dismiss() //關閉dialog
    }

    //SQL查詢視窗：欄位說明
    @SuppressLint("InflateParams")
    fun supportbtn(view: View) {
        alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_sql_support,null) //自定義Layout:dialog
        alice377AlertDialog = Alice377AlertDialog(context)
        alice377AlertDialog.setView(alice377view,0,0,0,
                0) //設定自定義layout
        val m_support_T_detail: TextView = alice377view.findViewById(R.id.support_T_detail) //LOG欄位說明視窗
        var msg = getString(R.string.support_t_detail) + "\n"

        if (langnum == 1) //簡體
            msg += "id：id\naction_view：APP名称\napp_name：执行介面\naction_action：执行动作\n" +
                   "action_date：执行时间\nstatus：执行结果\ninsert_date：储存日期"
        else
            msg += "id：id\naction_view：APP名稱\napp_name：執行介面\naction_action：執行動作\n" +
                   "action_date：執行時間\nstatus：執行結果\ninsert_date：儲存日期"

        m_support_T_detail.text = msg
        alice377AlertDialog.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alice377AlertDialog.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog.window.attributes = params //dialog參數綁定
    }

    //工程模式選單：選擇資料表查詢
    @SuppressLint("InflateParams")
    fun tablebtn(view: View) {
        alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_menu_table,null) //自定義Layout:dialog
        alice377AlertDialog2 = Alice377AlertDialog(context)
        alice377AlertDialog2.setView(alice377view,0,0,0,
                0) //設定自定義layout
        m_table_S_db = alice377view.findViewById(R.id.table_S_db) //資料庫選單
        m_table_S_table = alice377view.findViewById(R.id.table_S_table) //資料表選單
        m_table_E_sql_where = alice377view.findViewById(R.id.table_E_sql_where) //SQLite_where語法
        m_table_E_sql_order = alice377view.findViewById(R.id.table_E_sql_order) //SQLite_order語法
        db_spinner() //資料庫項目生成

        if (sql_where != "")
            m_table_E_sql_where.setText(sql_where)

        if (sql_order != "")
            m_table_E_sql_order.setText(sql_order)

        alice377AlertDialog2.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alice377AlertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog2.window.attributes = params //dialog參數綁定

        alice377AlertDialog.dismiss() //關閉dialog
    }

    //spinner項目生成
    private fun db_spinner() {

        if (first_create == 0){ //初次顯示項目

            if (appname != ""){ //不為空值時

                if (appname.substring(0,1) == "電")
                    table_num = 1

                if ((appname.substring(0,1) == "維") && appname.contains("通報"))
                    table_num = 2

                if ((appname.substring(0,1) == "維") && appname.contains("助手"))
                    table_num = 3
            }

            first_create = 1
        }

        //資料庫spinner
        db_spinner = ArrayAdapter(this, R.layout.table_simple_spinner_item, db_name)
        db_spinner.setDropDownViewResource(R.layout.table_dropdown_spinner_item)
        db_spinner.notifyDataSetChanged() //綁定更新
        m_table_S_db.adapter = db_spinner
        m_table_S_db.setSelection(table_num,true) //預設為打包區db
        m_table_S_db.onItemSelectedListener = dbchoice()

        //資料表spinner
        when(table_num){
            0 -> { //打包區掃描
                table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item, packing_scan)

                if (subtable_num > 2)
                    subtable_num = 0 //打包區只有三個選項所以回到預設值
            }

            1 -> { //電鍍委外
                table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                        electroplating_commission)

                if (subtable_num > 1)
                    subtable_num = 0 //電鍍委外只有兩個選項所以回到預設值
            }

            2 -> { //維修通報
                table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                        maintenance_bulletin)

                if (subtable_num > 2)
                    subtable_num = 0 //維修通報只有三個選項所以回到預設值
            }

            3 -> { //維修助手
                table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                        maintenance_assistant)
            }
        }

        table_spinner.setDropDownViewResource(R.layout.table_dropdown_spinner_item)
        table_spinner.notifyDataSetChanged() //綁定更新
        m_table_S_table.adapter = table_spinner
        m_table_S_table.setSelection(subtable_num,true) //預設為台北
        m_table_S_table.onItemSelectedListener = tablechoice()
    }

    //資料庫db選項監聽
    private fun dbchoice(): AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            table_num = position //記錄選擇第幾項

            when(table_id[table_num]){
                "packing_scan" -> { //打包區
                    table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                            packing_scan)

                    if (subtable_num > 2)
                        subtable_num = 0 //打包區只有三個選項所以回到預設值
                }

                "electroplating_commission" -> { //電鍍委外
                    table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                            electroplating_commission)

                    if (subtable_num == 2)
                        subtable_num = 0 //電鍍委外只有兩個選項所以回到預設值
                }

                "maintenance_bulletin" -> { //維修通報
                    table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                            maintenance_bulletin)

                    if (subtable_num > 2)
                        subtable_num = 0 //維修通報只有三個選項所以回到預設值
                }

                "maintenance_assistant" -> { //維修助手
                    table_spinner = ArrayAdapter(context, R.layout.table_simple_spinner_item,
                            maintenance_assistant)
                }
            }

            //重新載入使用者選擇的資料表spinner
            table_spinner.setDropDownViewResource(R.layout.table_dropdown_spinner_item)
            table_spinner.notifyDataSetChanged() //綁定更新
            m_table_S_table.adapter = table_spinner
            m_table_S_table.setSelection(subtable_num,true) //預設為台北
            m_table_S_table.onItemSelectedListener = tablechoice() //資料表選項監聽:記錄選擇第幾項
        }

        override fun onNothingSelected(parent: AdapterView<*>?){}
    }

    //資料表選項監聽
    private fun tablechoice(): AdapterView.OnItemSelectedListener? = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            subtable_num = position //記錄選擇第幾項
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    //選擇資料表查詢裡的確定按鈕
    fun tablecheckbtn(view: View){

        //開單一執行緒撈資料
        singleThreadExecutor.execute {
            var error = 0 //記錄是否發生錯誤

            if (sql_where != "")
                sql_where = m_table_E_sql_where.text.toString() //儲存SQLite_where語法

            if (sql_order != "")
                sql_order = m_table_E_sql_order.text.toString() //儲存SQLite_order語法

            var sql = "SELECT * FROM " //預設查詢全部資料
            val cp = arrayOf("Packing_scanContentProvider","Elec_comContentProvider",
                                           "Main_bullContentProvider","Main_atContentProvider") //內容提供者名稱
            val authority = "com.alice377.${table_id[table_num]}.providers.${cp[table_num]}" //內容提供者路徑
            var quri: Uri? = null

            when(table_num){
                0 -> { //打包區
                    quri = Uri.parse("content://$authority/${packing_scan_id[subtable_num]}") //預設打包區
                    sql += "${packing_scan_id[subtable_num]} WHERE $sql_where ORDER BY $sql_order" //組合整段SQLite語法
                }

                1 -> { //電鍍委外
                    quri = Uri.parse("content://$authority/${electroplating_commission_id[subtable_num]}")
                    sql += "${electroplating_commission_id[subtable_num]} WHERE $sql_where " +
                           "ORDER BY $sql_order" //組合整段SQLite語法
                }

                2 -> { //維修通報
                    quri = Uri.parse("content://$authority/${maintenance_bulletin_id[subtable_num]}")
                    sql += "${maintenance_bulletin_id[subtable_num]} WHERE $sql_where ORDER BY " +
                            sql_order //組合整段SQLite語法
                }

                3 -> { //維修助手
                    quri = Uri.parse("content://$authority/${maintenance_assistant_id[subtable_num]}")
                    sql += "${maintenance_assistant_id[subtable_num]} WHERE $sql_where ORDER BY " +
                            sql_order //組合整段SQLite語法
                }

            }

            mContRes = contentResolver
            myselection = sql_where //where條件
            myorder = sql_order //排序規則

            try {
                val cur = mContRes.query(quri,null, myselection,null,
                        myorder)
                data = cur.count

                if (data > 0){ //有資料,設定筆數
                    val ColName= cur.columnNames
                    cur.moveToFirst()

                    //設定listview內容
                    recSet = ArrayList() //重設recSet的值為空
                    val columnCount = cur.columnCount
                    mList = ArrayList() //重設mList的值為空

                    while (!cur.isAfterLast){
                        val fldSet = StringBuilder()

                        for (ii in 0..(columnCount - 1)){
                            fldSet.append(cur.getString(ii))

                            if (ii < (columnCount - 1))
                                fldSet.append("#")
                        }

                        recSet.add(fldSet.toString()) //存放到arraylist中
                        cur.moveToNext()
                    }

                    cur.close() //用完關掉

                    for (i in 0..(recSet.size - 1)){
                        val item = HashMap<String, Any>()
                        val fld= recSet[i].split("#").toTypedArray()
                        var str = ""
                        val j = fld.size

                        for (k in 1..j){
                            str += "${ColName[k - 1]}：${fld[k - 1]}"

                            if (k < j)
                                str += "\n"
                        }

                        item["textview"] = str
                        mList.add(item)
                    }

                }else{
                    cur.close() //用完關掉
                    mList = ArrayList() //重設mList的值為空
                }

            }catch (e: Exception){
                Log.d(TAG, "error$e")
                error = 1 //發生錯誤
            }

            //更新UI thread
            runOnUiThread {
                //===========設定Listview==========//
                val adapter = SimpleAdapter(
                        this,
                        mList,
                        R.layout.alice377_list_item,
                        arrayOf("textview"),
                        intArrayOf(R.id.alice377_list_item_T_data)
                        )
                //----------------------------------
                adapter.notifyDataSetChanged() //通知UI更新數據
                alice377_list_L_menu2.adapter = adapter
                alice377_list_L_menu2.isEnabled = true

                if (error == 1){
                    var msg = "語法輸入錯誤或無法查詢此條件資料"

                    if (langnum == 1) //簡體
                        msg = "语法输入错误或无法查询此条件资料"

                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                }else {
                    table_page = 1 //記錄顯示工程模式-資料表查詢頁面
                    alice377_list_E_sql_where.setText(sql_where) //顯示使用者輸入的SQL_where語法
                    alice377_list_E_sql_order.setText(sql_order) //顯示使用者輸入的SQL_order語法
                    alice377_list_R_rel.visibility = View.GONE //殺掉工程模式頁面
                    alice377_list_R_query.visibility = View.VISIBLE //顯示工程模式-資料表查詢頁面
                    textmsg2() //設定訊息框
                    alice377AlertDialog2.dismiss() //關閉dialog
                }

            }
        }
    }

    //設定資料表查詢訊息框
    private fun textmsg2() {
        var msg = "查詢到${data}筆資料"
        val msgbluestart = "查詢到"
        var msg2 = "資料已更新"

        if (langnum == 1){ //簡體
            msg = "查询到${data}笔资料"
            msg2 = "资料已更新"
        }

        val bluestart = msgbluestart.length
        val blueend = data.toString().length

        val msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                bluestart,bluestart + blueend,0) //藍色表示

        alice377_list_T_msg2.setText(msgcolor, TextView.BufferType.SPANNABLE)
        Toast.makeText(this, msg2, Toast.LENGTH_SHORT).show()
    }

    //工程模式-資料表查詢裡的查詢按鈕
    fun requerybtn(view: View){
        sql_where = alice377_list_E_sql_where.text.toString()
        sql_order = alice377_list_E_sql_order.text.toString()
        m_table_E_sql_where.setText(sql_where) //設定使用者輸入的SQLite_where語法
        m_table_E_sql_order.setText(sql_order) //設定使用者輸入的SQLite_order語法
        tablecheckbtn(view) //重新查詢
    }

    //SQLite_log資料清除
    @SuppressLint("InflateParams")
    fun logdelbtn(view: View) {
        alice377view = LayoutInflater.from(this).inflate(R.layout.alice377_list_delcheck,null) //自定義Layout:dialog
        alice377AlertDialog2 = Alice377AlertDialog(context)
        alice377AlertDialog2.setView(alice377view,0,0,0,
                0) //設定自定義layout
        alice377AlertDialog2.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alice377AlertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alice377AlertDialog2.window.attributes = params //dialog參數綁定

        alice377AlertDialog.dismiss() //關閉上一個dialog
    }


    //SQLite_log資料清除確認視窗：確定
    fun delyesbtn(view: View){
        var msg = "LOG資料刪除成功"
        var msg2 = "删除失败"
        val i= delete(context,"")

        if (langnum == 1){ //簡體
            msg = "LOG资料删除成功"
            msg2 = "删除失败"
        }

        if (i > 0){
            actionshow() //重載清單
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }else
            Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()

        alice377AlertDialog2.dismiss() //執行後關閉
    }

    //SQLite_log資料清除確認視窗：取消
    fun delcancelbtn(view: View){
        var msg = "取消清除LOG資料"

        if (langnum == 1) //簡體
            msg = "取消清除LOG资料"

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        alice377AlertDialog2.dismiss() //關閉dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        this.finish()
    }

    //監聽手機返回鍵
    @SuppressLint("InflateParams")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK){

            if (System.currentTimeMillis() - exitTime > 2000){
                exitTime = System.currentTimeMillis()
                var msg = "再按一次離開工程模式"
                var msg2 = "再按一次離開資料表查詢"

                if (langnum == 1){ //簡體
                    msg = "再按一次离开工程模式"
                    msg2 = "再按一次离开资料表查询"
                }

                if (alice377_list_R_query.visibility == View.VISIBLE) //工程模式-資料表查詢頁面開啟
                    Toast.makeText(this, msg2, Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }else {

                if (alice377_list_R_rel.visibility == View.VISIBLE){
                    alice377view = LayoutInflater.from(context).inflate(R.layout.alice377_list_quit,null) //自定義Layout:dialog

                    //設定選單選擇視窗
                    alice377AlertDialog = Alice377AlertDialog(context)
                    alice377AlertDialog.setView(alice377view,0,0,0,
                            0) //設定自定義layout
                    alice377AlertDialog.setCancelable(false) //dialog顯示時不能用其他方式關掉
                    alice377AlertDialog.show() //務必先show出來才能設定參數

                    //自定義Dialog視窗參數
                    val params = alice377AlertDialog.window.attributes //取得dialog參數對象
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
                    params.gravity = Gravity.CENTER //設置dialog重心

                }else if (alice377_list_R_query.visibility == View.VISIBLE){ //工程模式-資料表查詢頁面開啟
                    alice377_list_R_query.visibility = View.GONE //殺掉資料表查詢頁面
                    alice377_list_R_rel.visibility = View.VISIBLE //顯示工程模式

                }else {
                    alice377log("User click mobile_backbtn second to close this.",1) //寫log

                    if (langnum == 1) //簡體
                        Toast.makeText(this, "已离开工程模式", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "已離開工程模式", Toast.LENGTH_SHORT).show()

                    this.finish()
                }

            }

            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}