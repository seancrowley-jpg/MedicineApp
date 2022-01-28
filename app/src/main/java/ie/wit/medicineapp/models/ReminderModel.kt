package ie.wit.medicineapp.models

import android.app.PendingIntent
import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReminderModel(
    var uid: String = "",
    var medicineID: String = "",
    var groupID: String = "",
    var time: Long = 0,
    var requestCode: Int = 0,
): Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "medicineID" to medicineID,
            "groupID" to groupID,
            "time" to time,
            "requestCode" to requestCode,
            )
    }
}