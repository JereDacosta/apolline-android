package science.apolline

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import science.apolline.di.KodeinConfInjector
import science.apolline.service.synchronisation.SyncJobManager
import java.util.*

/**
 * Created by sparow on 05/02/18.
 */
class ApollineApplication : Application(), KodeinAware {

    override val kodein: Kodein = KodeinConfInjector(this).kodein

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private lateinit var mPrefs: SharedPreferences


    override fun onCreate() {
        super.onCreate()

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
//        mPrefs = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        val existingUuid = mPrefs.getString("device_uuid", DEFAULT_UUID)

        if (existingUuid == DEFAULT_UUID) {
            val editor = mPrefs.edit()
            with(editor) {
                putString("device_uuid", UUID.randomUUID().toString())
                apply()
            }
        }

        Logger.addLogAdapter(AndroidLogAdapter())

        if (BuildConfig.ENABLE_CRASHLYTICS) {
            Fabric.with(this, Crashlytics())
        }


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        LeakCanary.install(this)
        SyncJobManager.getJobManager(this)
    }

    companion object {
        private const val DEFAULT_UUID = "ffffffff-ffff-ffff-ffff-ffffffffffff"
//        private const val PREF_NAME = "ApollinePref"
    }
}
