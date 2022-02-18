package ie.wit.medicineapp.ui.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import ie.wit.medicineapp.R
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.home.Home
import java.util.*


class NotificationService : BroadcastReceiver() {

    companion object {
        val channelID = "channelID"
        var notificationID = Random().nextInt()
        val titleExtra = "Reminder Notification"
        val messageExtra = "A Reminder"
        val group = "Group Name"
        val highChannelId = "highChannelID"
        val time = "time"
        //var repeatRequestCodes : MutableList<Int>? = ArrayList()

        private fun getIntent(context: Context, reminder: ReminderModel, userId: String): PendingIntent? {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra(titleExtra, "Medicine Due!")
            intent.putExtra(
                messageExtra,
                reminder.medName + " " + reminder.medDosage + " " + reminder.requestCode
            )
            intent.putExtra(time, reminder.time)
            intent.putExtra("reminderID", reminder.uid)
            intent.putExtra("userID", userId)
            intent.putExtra("groupID", reminder.groupID)
            intent.putExtra("medicineID", reminder.medicineID)
            if(reminder.repeatDays!!.size != 0){
                intent.putExtra("repeat", true)
            }
            if (reminder.groupPriorityLevel == 2)
                intent.putExtra(channelID, "highChannelID")
            else
                intent.putExtra(channelID, channelID)
            notificationID = reminder.requestCode
            intent.putExtra("notificationID", notificationID)
            return PendingIntent.getBroadcast(
                context, reminder.requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun getRepeatIntent(context: Context, reminder: ReminderModel, userId: String , requestCode: Int): PendingIntent? {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra(titleExtra, "Medicine Due!")
            intent.putExtra(
                messageExtra,
                reminder.medName + " " + reminder.medDosage + " " + reminder.requestCode
            )
            intent.putExtra(time, reminder.time)
            intent.putExtra("reminderID", reminder.uid)
            intent.putExtra("userID", userId)
            intent.putExtra("groupID", reminder.groupID)
            intent.putExtra("medicineID", reminder.medicineID)
            if(reminder.repeatDays!!.size != 0){
                intent.putExtra("repeat", true)
            }
            if (reminder.groupPriorityLevel == 2)
                intent.putExtra(channelID, "highChannelID")
            else
                intent.putExtra(channelID, channelID)
            intent.putExtra("notificationID", notificationID)
            return PendingIntent.getBroadcast(
                context, Random().nextInt(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun setOnceOffAlarm(context: Context, reminder: ReminderModel, userId: String) {
            val pendingIntent = getIntent(context, reminder, userId)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if(reminder.time < System.currentTimeMillis()) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = reminder.time
                    add(Calendar.DAY_OF_YEAR, 1);
                }
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Toast.makeText(context,"Alarm set for ${Date(calendar.timeInMillis)}",Toast.LENGTH_SHORT).show()
            }
            else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.time,
                    pendingIntent
                )
                Toast.makeText(context, "Alarm set for ${Date(reminder.time)}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        fun setRepeatingAlarm(context: Context, reminder: ReminderModel, userId: String) {
            val pendingIntent = getIntent(context, reminder,userId)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (reminder.repeatDays!!.size == 7) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, reminder.time,
                    AlarmManager.INTERVAL_DAY, pendingIntent
                )
            }
            if(reminder.repeatDays!!.size in 1..6){
                for (i in reminder.repeatDays!!.indices){
                    scheduleRepeatAlarm(reminder.repeatDays!![i],alarmManager,context,reminder, userId)
                }
            }
        }

        fun cancelAlarm(context: Context, reminder: ReminderModel, userId: String) {
            val pendingIntent = getIntent(context, reminder,userId)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            /*if (reminder.repeatDays!!.size > 0) {
                for (i in repeatRequestCodes!!.indices){
                    var repeatPendingIntent = getRepeatIntent(context, reminder, userId, repeatRequestCodes!![i])
                    alarmManager.cancel(repeatPendingIntent)
                    Toast.makeText(context,"Repeat alarms cancelled",Toast.LENGTH_SHORT).show()
                }
                repeatRequestCodes!!.clear()
            }*/
        }

        private fun scheduleRepeatAlarm(day: Int, alarmManager: AlarmManager,context: Context, reminder: ReminderModel, userId: String){
            var requestCode = Random().nextInt()
            //repeatRequestCodes!!.add(requestCode)
            val repeatIntent = getRepeatIntent(context, reminder, userId, requestCode)
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
                repeatIntent
            )
            Toast.makeText(context,"Alarm set for ${Date(calendar.timeInMillis)} DAY: $day",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onReceive(context: Context, intent: Intent?) {
        val tapIntent = NavDeepLinkBuilder(context)
            .setComponentName(Home::class.java)
            .setGraph(R.navigation.main_navigation)
            .setDestination(R.id.schedulerFragment)
            .createPendingIntent()

        val snoozeIntent = Intent(context, ButtonReceiver::class.java)
        snoozeIntent.putExtra("action", "ACTION_SNOOZE")
        snoozeIntent.putExtras(intent!!)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 0, snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipIntent = Intent(context, ButtonReceiver::class.java)
        skipIntent.putExtra("action", "ACTION_SKIP")
        skipIntent.putExtras(intent!!)
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 1, skipIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val repeat = intent.getBooleanExtra("repeat",false)
        val userID = intent.getStringExtra("userID")
        val reminderID = intent.getStringExtra("reminderID")
        if(!repeat) {
            FirebaseDBManager.skipReminder(userID!!, reminderID!!)
        }
        val confirmIntent = Intent(context, ButtonReceiver::class.java)
        confirmIntent.putExtra("action", "ACTION_CONFIRM")
        confirmIntent.putExtras(intent!!)
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context, 2, confirmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId(intent?.getStringExtra(channelID)!!)
            .setGroup(intent.getStringExtra(group))
            .setContentIntent(tapIntent)
            .addAction(R.drawable.ic_launcher_foreground,"Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground,"Skip", skipPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground,"Confirm",confirmPendingIntent)
            .build()


        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}