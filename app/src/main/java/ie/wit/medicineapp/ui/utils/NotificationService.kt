package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.R
import ie.wit.medicineapp.firebase.FirebaseAuthManager
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.auth.LoginActivity
import ie.wit.medicineapp.ui.home.Home
import java.util.*
import androidx.core.content.ContextCompat.getSystemService




class NotificationService : BroadcastReceiver() {

    companion object {
        val channelID = "channelID"
        var notificationID = Random().nextInt()
        val titleExtra = "Reminder Notification"
        val messageExtra = "A Reminder"
        val group = "Group Name"
        val highChannelId = "highChannelID"

        private fun getIntent(context: Context, reminder: ReminderModel): PendingIntent? {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra(titleExtra, "Medicine Due!")
            intent.putExtra(
                messageExtra,
                reminder.medName + " " + reminder.medDosage + " " + reminder.requestCode
            )
            if (reminder.groupPriorityLevel == 2)
                intent.putExtra(channelID, "highChannelID")
            else
                intent.putExtra(channelID, channelID)
            notificationID = reminder.requestCode
            return PendingIntent.getBroadcast(
                context, reminder.requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun setOnceOffAlarm(context: Context, reminder: ReminderModel) {
            val pendingIntent = getIntent(context, reminder)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.time,
                pendingIntent
            )
            Toast.makeText(context,"Alarm set for ${Date(reminder.time)}",Toast.LENGTH_SHORT).show()
        }

        fun setRepeatingAlarm(context: Context, reminder: ReminderModel) {
            val pendingIntent = getIntent(context, reminder)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (reminder.repeatDays!!.size == 7) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, reminder.time,
                    AlarmManager.INTERVAL_DAY, pendingIntent
                )
            }
            if(reminder.repeatDays!!.size in 1..6){
                for (i in reminder.repeatDays!!.indices){
                    scheduleRepeatAlarm(reminder.repeatDays!![i],alarmManager,pendingIntent!!,context,reminder)
                }
            }
        }

        fun cancelAlarm(context: Context, reminder: ReminderModel) {
            val pendingIntent = getIntent(context, reminder)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }

        private fun scheduleRepeatAlarm(day: Int, alarmManager: AlarmManager, pendingIntent: PendingIntent,context: Context, reminder: ReminderModel){
            val calendar = Calendar.getInstance()
            val d = day +1
            calendar.timeInMillis = reminder.time
            calendar.set(Calendar.DAY_OF_WEEK,d)

            if(calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
            Toast.makeText(context,"Alarm set for ${Date(calendar.timeInMillis)} DAY: $day",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(Home::class.java)
            .setGraph(R.navigation.main_navigation)
            .setDestination(R.id.schedulerFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId(intent?.getStringExtra(channelID)!!)
            .setGroup(intent.getStringExtra(group))
            .setContentIntent(pendingIntent)
            .build()


        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}