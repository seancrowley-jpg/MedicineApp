package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ie.wit.medicineapp.R
import ie.wit.medicineapp.models.ReminderModel
import java.util.*

class NotificationService : BroadcastReceiver() {

    companion object{
        val channelID = "channelID"
        var notificationID = Random().nextInt()
        val titleExtra = "Reminder Notification"
        val messageExtra = "A Reminder"
        val group = "Group Name"
        val highChannelId ="highChannelID"

        fun getIntent(context: Context, reminder: ReminderModel): PendingIntent? {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra(titleExtra, "Medicine Due!")
            intent.putExtra(messageExtra,
                reminder.medName + " " + reminder.medDosage + " " + reminder.requestCode
            )
            if (reminder.groupPriorityLevel == 2)
                intent.putExtra(channelID, "highChannelID")
            else
                intent.putExtra(channelID, channelID)
            notificationID = reminder.requestCode
            return PendingIntent.getBroadcast(context, reminder.requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startOnceOffAlarm(context: Context, reminder: ReminderModel){
            val pendingIntent = getIntent(context, reminder)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.time,
                pendingIntent
            )
        }

        fun cancelAlarm(context: Context, reminder: ReminderModel) {
            val pendingIntent = getIntent(context, reminder)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }

    }

    override fun onReceive(context: Context, intent: Intent?) {

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId(intent?.getStringExtra(channelID)!!)
            .setGroup(intent.getStringExtra(group))
            .build()



        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}