package ie.wit.medicineapp.main

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ie.wit.medicineapp.ui.settings.ThemeProvider
import ie.wit.medicineapp.ui.utils.NotificationService
import timber.log.Timber

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("MedicineApp started")
        Firebase.database.setPersistenceEnabled(true)
        //initialise theme from shared preferences
        val theme = ThemeProvider(this).getThemeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(theme)
        createNotificationChannel()
        createHighPriorityNotificationChannel()
    }

    private fun createHighPriorityNotificationChannel() {
        val name = "High Priority Reminder Channel"
        val description = "Channel for High Priority Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NotificationService.highChannelId, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 1000, 500, 100, 500, 100, 500, 100, 500, 100, 500)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        channel.lightColor = Color.CYAN
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationChannel() {
        val name = "Low Priority Reminder Channel"
        val description = "Channel for Low Priority Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NotificationService.channelID, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 250, 250, 250)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        channel.lightColor = Color.CYAN
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}