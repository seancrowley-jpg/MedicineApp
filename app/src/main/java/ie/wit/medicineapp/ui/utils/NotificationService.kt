package ie.wit.medicineapp.ui.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ie.wit.medicineapp.R
import java.util.*

class NotificationService : BroadcastReceiver() {

    companion object{
        val channelID = "channelID"
        var notificationID = 1
        val titleExtra = "Reminder Notification"
        val messageExtra = "A Reminder"
        val group = "Group Name"
        val highChannelId ="highChannelID"
    }


    override fun onReceive(context: Context, intent: Intent?) {

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId(intent?.getStringExtra(channelID)!!)
            .setGroup(intent?.getStringExtra(group))
            .build()

        notificationID = generateRandomInt()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

    private fun generateRandomInt() : Int {
        return Random().nextInt()
    }

}