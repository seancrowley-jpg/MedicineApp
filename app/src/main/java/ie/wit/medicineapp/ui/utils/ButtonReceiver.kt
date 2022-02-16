package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ie.wit.medicineapp.firebase.FirebaseDBManager
import java.util.*

class ButtonReceiver : BroadcastReceiver(){

    companion object {

    }


    override fun onReceive(context: Context, intent: Intent) {
        val actionSnooze = intent.getStringExtra("snooze")
        val actionSkip = intent.getStringExtra("skip")
        val actionConfirm = intent.getStringExtra("confirm")

        if (actionSnooze.equals("ACTION_SNOOZE")){
            snoozeAlarm(context, intent)
            Toast.makeText(context,"SNOOZE Button Pressed", Toast.LENGTH_SHORT).show()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NotificationService.notificationID)
        }
        if(actionSkip.equals("ACTION_SKIP")){
            Toast.makeText(context,"Skip Button Pressed", Toast.LENGTH_SHORT).show()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NotificationService.notificationID)
        }
        if(actionConfirm.equals("ACTION_CONFIRM")){
            confirmMedTaken(intent,context)
            Toast.makeText(context,"Confirm Button Pressed", Toast.LENGTH_SHORT).show()
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

    private fun confirmMedTaken(intent: Intent , context: Context){
        val userID = intent.getStringExtra("userID")
        val groupId = intent.getStringExtra("groupID")
        val medicineID = intent.getStringExtra("medicineID")
        FirebaseDBManager.confirmMedTaken(userID!!,groupId!!,medicineID!!)
    }
}