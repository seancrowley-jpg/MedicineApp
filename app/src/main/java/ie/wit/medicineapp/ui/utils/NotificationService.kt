package ie.wit.medicineapp.ui.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ie.wit.medicineapp.R

class NotificationService : BroadcastReceiver() {

    companion object{
        val channelID = "channelID"
        val notificationID = 1
        val titleExtra = "Reminder Notification"
        val messageExtra = "A Reminder"
    }


    override fun onReceive(context: Context, intent: Intent?) {

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}