package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceManager
import ie.wit.medicineapp.firebase.FirebaseDBManager
import java.util.*

class ButtonReceiver : BroadcastReceiver(){

    companion object {

        fun snoozeAlarm(context: Context, intent: Intent?) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val snoozeTime = sharedPreferences.getString("snooze_limit", "")
            var minute = snoozeTime?.toInt()

            val notificationIntent = Intent(context, NotificationService::class.java)
            notificationIntent.putExtras(intent!!)
            notificationIntent.removeExtra("action")

            val snoozePendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if(minute == 0)
            {
                minute = 1
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MINUTE, minute!!)
            }


            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                snoozePendingIntent
            )
        }

    }


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when {
            action.equals("ACTION_SNOOZE") -> {
                snoozeAlarm(context, intent)
                Toast.makeText(context,"SNOOZE Button Pressed", Toast.LENGTH_SHORT).show()
                manager.cancel(NotificationService.notificationID)
            }
            action.equals("ACTION_SKIP") -> {
                Toast.makeText(context,"Skip Button Pressed", Toast.LENGTH_SHORT).show()
                manager.cancel(NotificationService.notificationID)
            }
            action.equals("ACTION_CONFIRM") -> {
                confirmMedTaken(intent,context)
                Toast.makeText(context,"Confirm Button Pressed", Toast.LENGTH_SHORT).show()
                manager.cancel(NotificationService.notificationID)
            }
        }
    }


    private fun confirmMedTaken(intent: Intent , context: Context){
        val userID = intent.getStringExtra("userID")
        val groupId = intent.getStringExtra("groupID")
        val medicineID = intent.getStringExtra("medicineID")
        FirebaseDBManager.confirmMedTaken(userID!!,groupId!!,medicineID!!, context)
    }
}