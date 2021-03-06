package science.apolline.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.telephony.TelephonyManager
import android.widget.Toast
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*
import java.text.SimpleDateFormat


/**
 * Created by sparow on 22/12/2017.
 */


object CheckUtility : AnkoLogger {

    /**
     * To get device consuming netowork type is 2g,3g,4g
     *
     * @param context
     * @return "2g","3g","4g" as a String based on the network type
     */
    fun getNetworkType(context: Context): String {
        val mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = mTelephonyManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            else -> "Notfound"
        }
    }


    private fun checkNetworkStatus(context: Context): String {

        val networkStatus: String
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //Check Wifi
        val wifi = manager.activeNetworkInfo
        //Check for mobile data
        val mobile = manager.activeNetworkInfo

        if (wifi.type == ConnectivityManager.TYPE_WIFI) {
            networkStatus = "wifi"
        } else if (mobile.type == ConnectivityManager.TYPE_MOBILE) {
            networkStatus = "mobileData"
        } else {
            networkStatus = "noNetwork"
        }
        return networkStatus
    }

    /**
     * To check device has internet
     *
     * @param context
     * @return boolean as per status
     */
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo? = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }


    fun isWifiNetworkConnected(context: Context): Boolean {
        var wifiNetworkState = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo : NetworkInfo? = cm.activeNetworkInfo
        if (netInfo?.type == ConnectivityManager.TYPE_WIFI)
            wifiNetworkState = true
        return netInfo != null && wifiNetworkState
    }


    fun checkFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkReandPhoneStatePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun checkWriteToExternalStoragePermissionPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun canGetLocation(context: Context): Boolean {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {

        }
        return gpsEnabled || networkEnabled
    }

    fun requestLocation(context: Context): AlertDialog {
        val alertDialog = AlertDialog.Builder(context).create()
        if (!canGetLocation(context)) {
            alertDialog.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { _, _ ->
                Toasty.warning(context, "Your GPS is disabled", Toast.LENGTH_SHORT, true).show()
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            alertDialog.show()
        }
        return alertDialog
    }

    @SuppressLint("BatteryLife")
    fun requestDozeMode(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = context.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                context.startActivity(intent)
            }
        }
    }

    fun requestPartialWakeUp(context: Context, timeout: Long): PowerManager.WakeLock {
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, packageName)
        wl.acquire(timeout)
        info("Partial wake up is: " + wl.isHeld)
        return wl
    }

    fun requestWifiFullMode(context: Context): WifiManager.WifiLock {
        val packageName = context.packageName
        val pm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wf = pm.createWifiLock(WifiManager.WIFI_MODE_FULL, packageName)
        wf.acquire()
        info("WiFi full mode is: " + wf.isHeld)
        return wf
    }


    fun newDate() : String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.FRANCE)
        return df.format(c)
    }

    fun dateParser(timestamp: Long): String {
        val c = Date(timestamp / 1000000)
        val df = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.FRANCE)
        return df.format(c)
    }




}