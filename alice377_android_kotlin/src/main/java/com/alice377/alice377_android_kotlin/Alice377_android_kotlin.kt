package com.alice377.alice377_android_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.alice377.alice377_android_kotlin.prividers.AppLogDb
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_action
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_date
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.action_view
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.insert
import com.alice377.alice377_android_kotlin.prividers.AppLogDb.Companion.insert_date
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DUPLICATE_LABEL_IN_WHEN", "DEPRECATION")
class Alice377_android_kotlin {

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    companion object {
        val TAG: String = "alice377=>" //不可變更的Log標籤

        //*app應用類----------------------------------------------------------------------------------*//
        var langnum: Int = 0 //標記使用的語言：0=預設繁體,1=簡體,2=英文
        val locale_TW: Locale = Locale.TRADITIONAL_CHINESE //多國語言：繁體
        val locale_CN: Locale = Locale.SIMPLIFIED_CHINESE //多國語言：簡體
        val locale_EN: Locale = Locale.ENGLISH //多國語言：英文
        var locale_save: String = "" //儲存value語系

        //起始語言配置
        fun langstart(context: Context) {

            //儲存語言設定值在app裡
            val textsetdata:SharedPreferences = context.getSharedPreferences("TEXT_SET",0)
            locale_save = textsetdata.getString("locale_set", locale_TW.toString())

            //變更設定檔
            var resources:Resources = context.resources
            val config:Configuration = resources.configuration

            //依照版本不同執行：JELLY_BEAN_MR1 = 4.3
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val wrapper: ContextThemeWrapper = ContextThemeWrapper()

                when(locale_save){ //locale_save為簡體時設定簡體中文以此類推，預設為繁體中文
                    locale_CN.toString() -> {
                        config.setLocale(locale_CN) //設定為簡體中文
                        wrapper.applyOverrideConfiguration(config) //改變配置訊息即時套用
                        langnum = 1 //設為使用簡體
                    }

                    locale_EN.toString() -> {
                        config.setLocale(locale_EN) //設定為英文
                        wrapper.applyOverrideConfiguration(config) //改變配置訊息即時套用
                        langnum = 2 //設為使用英文
                    }

                    else -> {
                        textsetdata.edit().putString("locale_set", locale_save).apply() //記錄語言設定值
                        langnum = 0 //設為預設繁體
                    }
                }

            }else {
                resources = context.applicationContext.resources
                val dm:DisplayMetrics = resources.displayMetrics

                when(locale_save){
                    locale_CN.toString() -> {
                        config.locale = locale_CN //設定為簡體中文
                        resources.updateConfiguration(config, dm) //改變配置訊息即時套用
                        langnum = 1 //設為使用簡體
                    }

                    locale_EN.toString() -> {
                        config.locale = locale_EN //設定為英文
                        resources.updateConfiguration(config, dm) //改變配置訊息即時套用
                        langnum = 2 //設為使用英文
                    }

                    else -> {
                        textsetdata.edit().putString("locale_set", locale_save).apply() //記錄語言設定值
                        langnum = 0 //設為預設繁體
                    }
                }

            }

        }

        //軟體鍵盤隱藏
        fun hidekeyboard(view:View){
            val imm:InputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)
        }

        //檢查軟體鍵盤使用狀態:開啟軟鍵盤
        fun toggleSoftInput(view: View){
            val imm:InputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.toggleSoftInput(0,0)
        }

        //常用的Toast寫成method:getcontext=app的context,getmsg=訊息
        fun toast(getcontext: Context, getmsg: String, getstyle: String){

            when(getstyle.toUpperCase()){ //轉成大寫
                "LONG" -> Toast.makeText(getcontext, getmsg, Toast.LENGTH_LONG).show()
                "SHORT" -> Toast.makeText(getcontext, getmsg, Toast.LENGTH_SHORT).show()
            }
        }

        //toast長訊息
        fun longtoast(getcontext: Context, getmsg: String){
            Toast.makeText(getcontext, getmsg, Toast.LENGTH_LONG).show()
        }

        //toast短訊息
        fun shorttoast(getcontext: Context, getmsg: String){
            Toast.makeText(getcontext, getmsg, Toast.LENGTH_SHORT).show()
        }
        //*------------------------------------------------------------------------------------------*//


        //*網路連線類---------------------------------------------------------------------------------*//
        var wifilink:Int = 0 //記錄使用wifi連線:1=使用wifi

        //監測網路連線
        fun checklink(view: String, mContext: Context){
            val context = mContext.applicationContext
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            var msg: String //記錄訊息

            //獲取NetcorkInfo對象
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            wifilink = 0 //偵測前先重置

            //判斷當前網路狀態是否為連接狀態且連接哪種網路
            when(networkInfo?.type){
                ConnectivityManager.TYPE_WIFI -> { //使用wifi
                    msg = "已偵測到網路連線：WIFI網路已連接"

                    if (langnum == 1) //簡體
                        msg = "已侦测到网路连线：WIFI网路已连接"
                    else if (langnum == 2) //英文
                        msg = "Internet connection detected: WIFI network connected"

                    wifilink = 1 //記錄使用wifi
                }

                ConnectivityManager.TYPE_MOBILE -> { //使用行動網路
                    val nSubtype:Int = networkInfo.subtype //獲得mobile網路的類型
                    Log.d(TAG,"nSubType=$nSubtype")
                    var netmsg = ""
                    val telephonyManager = context.getSystemService(Context
                            .TELEPHONY_SERVICE) as TelephonyManager

                    //行動網路類型
                    when(nSubtype){
                        NETWORK_TYPE_LTE -> {

                            if (!telephonyManager.isNetworkRoaming){
                                netmsg = "4G行動網路"

                                if (langnum == 1) //簡體
                                    netmsg = "4G行动网路"
                                else if (langnum == 2) //英文
                                    netmsg = "4G mobile network"
                            }

                        }

                        NETWORK_TYPE_UMTS or NETWORK_TYPE_EHRPD or NETWORK_TYPE_HSDPA or
                                NETWORK_TYPE_HSPA or NETWORK_TYPE_EVDO_0 or NETWORK_TYPE_EVDO_A or
                                NETWORK_TYPE_EVDO_B or NETWORK_TYPE_HSPAP -> { //NETWORK_TYPE_HSPAP:台灣之星3G

                            if (!telephonyManager.isNetworkRoaming){
                                netmsg = "3G行動網路"

                                if (langnum == 1) //簡體
                                    netmsg = "3G行动网路"
                                else if (langnum == 2) //英文
                                    netmsg = "3G mobile network"
                            }

                        }

                        NETWORK_TYPE_GPRS or NETWORK_TYPE_EDGE or NETWORK_TYPE_CDMA or NETWORK_TYPE_IDEN
                                or NETWORK_TYPE_1xRTT -> {

                            if (!telephonyManager.isNetworkRoaming){
                                netmsg = "2G行動網路"

                                if (langnum == 1) //簡體
                                    netmsg = "2G行动网路"
                                else if (langnum == 2) //英文
                                    netmsg = "2G mobile network"
                            }

                        }

                    }

                    if (netmsg == ""){
                        msg = "行動網路訊號不佳，請使用WIFI網路確保資料正常下載"

                        if (langnum == 1) //簡體
                            msg = "行动网路讯号不佳，请使用WIFI网路确保资料正常下载"
                        else if (langnum == 2) //英文
                            msg = "Mobile network signal is not good, please use WIFI network to " +
                                  "ensure the normal download of data"
                    }else{
                        msg = "已偵測使用$netmsg，請使用WIFI網路確保資料正常下載"

                        if (langnum == 1) //簡體
                            msg = "已侦测使用$netmsg，请使用WIFI网路确保资料正常下载"
                        else if (langnum == 2) //英文
                            msg = "Detected use" + netmsg + ", please use WIFI network to ensure normal" +
                                  " download of data"
                    }

                    shorttoast(context, msg)
                }

                else -> {
                    wifilink = 0 //記錄斷網
                    msg = "尚未連接網路，請確認手機已連接WIFI網路"

                    if (langnum == 1)  //簡體
                        msg = "尚未连接网路，请确认手机已连接WIFI网路"
                    else if (langnum == 2)  //英文
                        msg = "Not connected to the Internet yet, please make sure the phone is " +
                              "connected to WIFI network"

                    shorttoast(context, msg)
                    alice377log(view,"[自動]未連接網路",0, context) //寫log
                }
            }

        }
        //*------------------------------------------------------------------------------------------*//


        //*資料庫處理類-------------------------------------------------------------------------------*//

        //抓資料庫執行緒的資料萬用法：寫在Create裡
        fun catchdb(){
            StrictMode.setThreadPolicy(
                    StrictMode.
                            ThreadPolicy.
                            Builder().
                            detectDiskReads().
                            detectDiskWrites().
                            detectNetwork().
                            penaltyLog().
                            build()
            )
            StrictMode.setVmPolicy(
                    StrictMode.
                            VmPolicy.
                            Builder().
                            detectLeakedSqlLiteObjects().
                            penaltyLog().
                            penaltyDeath().
                            build()
            )
        }

        //app寫log
        fun alice377log(view: String, action: String, status: Int, context: Context){
            action_view = view
            action_action = action
            action_date = mobiletoday("yyyy/MM/dd HH:mm:ss")
            AppLogDb.status = status //0=失敗,1=成功
            insert_date = mobiletoday("yyyy/MM/dd")
            insert(context) //寫log
        }

        //*------------------------------------------------------------------------------------------*//


        //*apk文件訊息處理類---------------------------------------------------------------------------*//
        var packagename: String = "" //記錄包名
        var appname: String = "" //記錄app名稱
        var versionname: String = "" //記錄版本名稱
        var versioncode: Int = 0 //記錄版本號碼

        //傳遞apk版本名稱及版號：用於私有雲apk更新或工程模式
        fun apkname(context: Context): Int{
            var apkinfo: Int = 0 //記錄有無回傳訊息

            //取得目前的apk文件訊息
            val pm: PackageManager = context.packageManager
            val pi: PackageInfo = pm.getPackageInfo(context.packageName,0)
            val ai: ApplicationInfo = pi.applicationInfo

            packagename = pi.packageName //取得app包名
            appname = pm.getApplicationLabel(ai).toString() //取得app名稱
            versionname = pi.versionName //取得app版本名稱
            versioncode = pi.versionCode //取得app版本號碼
            Log.d(TAG, "APP包名:$packagename, APP名稱:$appname, 版本名稱:$versionname, " +
                    "版本號碼:$versioncode")

            if (packagename != "" && appname != "" && versionname != "" && versioncode != 0){
                apkinfo = 1 //表示都有值
            }

            return apkinfo
        }
        //*------------------------------------------------------------------------------------------*//


        @SuppressLint("SimpleDateFormat")
        //*時間處理類---------------------------------------------------------------------------------*//
        /*傳入時間格式抓取手機時間：傳入的簡易字串格式 yyyy/M/d HH:mm:ss
          SimpleDateFormat函數語法：
          G 年代標誌符
          y 年
          M 月
          d 日
          h 時 在上午或下午 (1~12)
          H 時 在一天中 (0~23)
          m 分
          s 秒
          S 毫秒
          E 星期
          D 一年中的第幾天
          F 一月中第幾個星期幾
          w 一年中第幾個星期
          W 一月中第幾個星期
          a 上午 / 下午 標誌符
          k 時 在一天中 (1~24)
          K 時 在上午或下午 (0~11)
          z 時區
        */
        fun mobiletoday(str: String): String{
            val formatter = SimpleDateFormat(str)
            return formatter.format(Date())
        }

        @SuppressLint("SimpleDateFormat")
        //取得現在時間，此寫法網路傳遞無須轉碼
        fun nowtime(time_format: String): String{
            val cal = Calendar.getInstance()
            val currentLocalTime = cal.time
            val date = SimpleDateFormat(time_format)
            date.timeZone = TimeZone.getTimeZone("Asia/Taipei") //台北時間

            return date.format(currentLocalTime)
        }

        //日期計算：今天日期回推45天的日期
//        fun calculateday(today: String): String{
//            var yearsp: Int = 0 //記錄是否為閏年
//            val bmonth: IntArray = intArrayOf(1,3,5,7,8,10,12) //大月31天
//            var big_month: Int = 0 //記錄是否為大月(三月不標記)
//            var num: Int //記錄跨月的天數
//
//            val year: String = today.substring(0,4) //提取字元範圍:substring(start,end)
//            val month: String = today.substring(5,7)
//            val day: String = today.substring(8) //end沒寫就是執行到最後一位
//            val start_day: StringBuilder = StringBuilder() //記錄計算後的start_day
//
//            //先判斷是否為閏年:規則= 1.可被4整除但不被100整除 2.可被400整除
//            val iyear: Int = Integer.parseInt(year) //轉成整數
//
//            if (iyear % 4 == 0 && iyear % 100 != 0 || iyear % 400 == 0){
//                yearsp = 1
//            }
//
//            //判斷是否為大月
//            val imonth: Int = Integer.parseInt(month)
//            val iday: Int = Integer.parseInt(day)
//
//            for (m in 0..bmonth.size){
//
//                if (bmonth[m] == imonth){
//                    big_month = 1 //標記大月
//
//                    if (imonth == 1 || imonth == 8){
//
//                        if (iday < 14){
//                            num = 14 - iday //跨上上個月幾天
//                            start_day.append(iyear - 1)
//
//                            if (imonth == 1)
//                                start_day.append("/11/").append(30 - num + 1)
//                            else
//                                start_day.append("/").append(imonth - 2).append("/").append(30 - num + 1)
//
//                        }else{
//                            num = iday - 14
//
//                            if (imonth == 1)
//                                start_day.append(iyear - 1).append("/12/").append(1 + num)
//                            else
//                                start_day.append(year).append("/").append(imonth - 1).append("/").append(1 + num)
//                        }
//
//                    }else if (imonth == 3){
//                        start_day.append(year).append("/")
//
//                        if (yearsp == 0){ //不是閏年
//
//                            if (iday < 17){
//                                num = 17 - iday //跨上上個月幾天
//                                start_day.append(imonth - 2).append("/").append(31 - num +1)
//
//                            }else{
//                                num = iday -17
//                                start_day.append(imonth - 1).append("/").append(1 + num)
//                            }
//
//                        }else{
//
//                            if (iday < 16){
//                                num = 16 - iday //跨上上個月幾天
//                                start_day.append(imonth - 2).append("/").append(31 - num + 1)
//
//                            }else{
//                                num = iday - 16
//                                start_day.append(imonth - 1).append("/").append(1 + num)
//                            }
//
//                        }
//
//                    }else{
//                        start_day.append(year).append("/")
//
//                        if (iday < 15){
//                            num = 15 -iday //跨上上個月幾天
//                            start_day.append(imonth - 2).append("/").append(31 - num + 1)
//                        }else{
//                            num = iday - 15
//                            start_day.append(imonth - 1).append("/").append(1 + num)
//                        }
//
//                    }
//
//                    break
//                }
//
//            }
//
//            //判斷是否為小月
//            if (big_month == 0){
//
//                if (imonth == 2 || imonth == 9){ //2月或9月
//
//                    if (iday < 14){
//                        num = 14 - iday //跨上上個月幾天
//
//                        if (imonth == 2)
//                            start_day.append(iyear - 1).append("/12/").append(31 - num + 1)
//                        else
//                            start_day.append(year).append("/").append(imonth - 2).append("/")
//                                    .append(1 + num)
//                    }else{
//                        num = iday - 14
//                        start_day.append(year).append("/").append(imonth - 1).append("/").append(1 + num)
//                    }
//
//                }else if (imonth == 4){ //4月
//                    start_day.append(year).append("/")
//
//                    if (iday < 14){
//                        num = 14 - iday //跨上上個月幾天
//                        start_day.append(imonth - 2).append("/")
//
//                        if (yearsp == 0) //不是閏年
//                            start_day.append(28 - num + 1)
//                        else
//                            start_day.append(29 - num + 1)
//
//                    }else{
//                        num = iday - 14
//                        start_day.append(imonth - 1).append("/").append(1 + num)
//                    }
//
//                }else{
//                    start_day.append(year).append("/")
//
//                    if (iday < 14){
//                        num = 14 - iday //跨上上個月幾天
//                        start_day.append(imonth - 2).append("/").append(31 - num + 1)
//
//                    }else{
//                        num = iday - 14
//                        start_day.append(imonth - 1).append("/").append(1 + num)
//
//                    }
//
//                }
//
//            }
//
//            return start_day.toString()
//        }
        //*------------------------------------------------------------------------------------------*//


        //*圖片處理類---------------------------------------------------------------------------------*//
        //處理drawable圖片生成bitmap，解決設定圖片時產生的Out of Memory(OOM)
        fun readBitMap(context: Context, resId: Int, size: Int): Bitmap{
            val opt: BitmapFactory.Options = BitmapFactory.Options()
            opt.inPreferredConfig = Bitmap.Config.RGB_565
            opt.inPurgeable = true //java系統記憶體不時先行回收部分的記憶體
            opt.inInputShareable = true
            opt.inSampleSize = size //原始圖片幾分之一的大小

            //獲取資源圖片
            val Is: InputStream = context.resources.openRawResource(resId)

            return BitmapFactory.decodeStream(Is, null, opt)
        }
        //*------------------------------------------------------------------------------------------*//


        //*工程模式登入驗證----------------------------------------------------------------------------*//
        //account驗證
        fun alice377_acget(str: String): Boolean{
            val getabc = str.toCharArray() //字串轉字元陣列
            val getabc_int = IntArray(getabc.size) //設定數字陣列長度
            var getabc_Str = StringBuilder() //設定存放英文字元
            val abc = arrayOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n",
                                           "o","p","q","r","s", "t","u","v","w","x","y","z")
            val check = intArrayOf(0,1,2,3,4,5,6,7,8,9) //數字陣列
            var ischeck = false //記錄是否通過驗證

            //使用者輸入的字串處理------------------------------------------------------------------------
            for (i in 0..(str.length - 1)){ //字串轉換分別存入數字及英文陣列

                if (getabc[i].toInt() in 48..57){ //數字0~9

                    for (j: Int in 0..i){

                        if (j == i)
                            getabc_int[j] = Integer.parseInt(getabc[i].toString())
                    }
                    Log.d(TAG, "getabc_int=$getabc_int")

                }else if (getabc[i].toInt() in 97..122){ //英文小寫字母

                    for (k: Int in 0..i){

                        if (k == i){
                            val save_Str: String = getabc_Str.substring(0, k)
                            getabc_Str = StringBuilder(save_Str + getabc[i].toString())

                        }else
                            getabc_Str.append("z")
                    }
                    Log.d(TAG,"getabc_Str=$getabc_Str")

                }

            }
            Log.d(TAG,"getabc_int=$getabc_int,getabc_Str=$getabc_Str")
            //------------------------------------------------------------------------------------------
            val char0= getabc[0].toInt() - 97 //ASCII第一位計算
            val char1= getabc[1].toInt() - 97 //ASCII第二位計算
            val sc0= Integer.toString(char0).substring(0,1)
            val sc1= Integer.toString(char1).substring(1,2)
            val isc0= sc0.toInt()
            val isc1= sc1.toInt()

            //驗證公式：先字母後數字
            if ((getabc_Str.substring(char1, (char1 + 1)) == abc[char0]) &&
                    (getabc_Str.substring((char0 * char1 + 1), (char0 * char1 + 2)) == abc[char1])){

                if ((isc0 * isc1 - (isc1 - isc0) - isc0) == char1)
                    ischeck = true
            }

            return ischeck
        }

        //password驗證
        fun alice377_pwget(str: String): Boolean{
            val getabc = str.toCharArray() //字串轉字元陣列
            val getabc_int: IntArray = IntArray(getabc.size) //設定數字陣列長度
            var getabc_Str: StringBuilder = StringBuilder() //設定存放英文字元
            val abc = "abcdefghijklmnopqrstuvwxyz".toCharArray() //26個字母字元陣列
            var ischeck: Boolean = false //記錄是否通過驗證

            //使用者輸入的字串處理------------------------------------------------------------------------
            for (i in 0..str.length){ //字串轉換分別存入數字及英文陣列

                if (getabc[i].toInt() in 48..57){ //數字0~9

                    for (j in 0..i){

                        if (j == i)
                            getabc_int[j] = getabc[i].toString().toInt()
                    }
                    Log.d(TAG,"getabc_int=$getabc_int")

                }else if (getabc[i].toInt() in 97..122){ //英文小寫字母

                    for (k in 0..i){

                        if (k == i){
                            val save_Str = getabc_Str.substring(0,k)
                            getabc_Str = StringBuilder(save_Str + getabc[i].toString())

                        }else
                            getabc_Str.append("z")

                    }
                    Log.d(TAG,"getabc_Str=$getabc_Str")
                }

            }
            Log.d(TAG,"getabc_int=$getabc_int,getabc_Str=$getabc_Str")
            //------------------------------------------------------------------------------------------
            val char0 = getabc[0].toInt() - 97 //ASCII第一位計算
            val char1 = getabc[3].toInt() - getabc[2].toInt() //ASCII第三位與第二位的差
            val char2 = getabc[4].toInt() - getabc[5].toInt() //ASCII第四位與第五位的差
            val char3 = getabc[6].toInt() - getabc[7].toInt() //ASCII第六位與第七位的差

            //驗證公式:先字母後數字
            if ((str.substring(0,1) == abc[char0].toString()) && (str.substring(1,2) == abc[char1].toString())){

                if (((char3 - char2 - 6) == char1) && ((char0 - 3 * 5) == 0))
                    ischeck = true
            }

            return ischeck
        }
        //*------------------------------------------------------------------------------------------*//

    }

}