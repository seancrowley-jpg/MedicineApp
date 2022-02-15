package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.*

class ButtonReceiver : BroadcastReceiver(){

    companion object {

    }


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("snooze")

        if (action.equals(NotificationService.snooze)){
            snoozeAlarm(context, intent)
            Toast.makeText(context,"SNOOZE Button Pressed", Toast.LENGTH_SHORT).show()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NotificationService.notificationID)
        }
    }

    private fun snoozeAlarm(context: Context, intent: Intent?){

        val notificationIntent = Intent(context, NotificationService::class.java)
        notificationIntent.putExtras(intent!!)

        val snoozePendingIntent =  PendingIntent.getBroadcast(
            context, NotificationService.notificationID, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 5)
        }


        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            snoozePendingIntent
        )
    }
}