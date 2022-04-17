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
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days

class ButtonReceiver : BroadcastReceiver(){

    companion object {

        fun snoozeAlarm(context: Context, intent: Intent?) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val snoozeTime = sharedPreferences.getString("snooze_limit", "5")

            val notificationIntent = Intent(context, NotificationService::class.java)
            notificationIntent.putExtras(intent!!)
            notificationIntent.removeExtra("action")

            val snoozePendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MINUTE, snoozeTime.toString().toInt())
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
                medButtonPressed(intent,context, "Skipped")
                Toast.makeText(context,"Skip Button Pressed", Toast.LENGTH_SHORT).show()
                manager.cancel(NotificationService.notificationID)
            }
            action.equals("ACTION_CONFIRM") -> {
                medButtonPressed(intent,context, "Taken")
                Toast.makeText(context,"Confirm Button Pressed", Toast.LENGTH_SHORT).show()
                manager.cancel(NotificationService.notificationID)
            }
        }
    }


    private fun medButtonPressed(intent: Intent , context: Context,status: String){
        val userID = intent.getStringExtra("userID")
        val groupID = intent.getStringExtra("groupID")
        val medicineID = intent.getStringExtra("medicineID")
        val quantityDue = intent.getIntExtra("quantityDue", 1)

        if(status != "Skipped") {
            FirebaseDBManager.confirmMedTaken(userID!!, groupID!!, medicineID!!, context, quantityDue)
        }
        val confirmation = ConfirmationModel()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        confirmation.time = System.currentTimeMillis()
        confirmation.day = calendar.get(Calendar.DAY_OF_MONTH)
        confirmation.month = calendar.get(Calendar.MONTH) + 1
        confirmation.year = calendar.get(Calendar.YEAR)
        confirmation.medicineID = medicineID!!
        confirmation.groupID = groupID!!
        confirmation.status = status
        confirmation.medicineName = intent.getStringExtra("medName")!!
        confirmation.groupName = intent.getStringExtra("groupName")!!
        FirebaseDBManager.createConfirmation(userID!!, confirmation)
    }
}