package ie.wit.medicineapp.models
import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmationModel(
    var uid: String = "",
    var time: Long = 0,
    var day: Int = 0,
    var month: Int = 0,
    var year: Int = 0,
    var medicineID: String = "",
    var groupID: String = "",
    var medicineName: String = "",
    var groupName: String = "",
    var status:String = ""
) : Parcelable{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "time" to time,
            "day" to day,
            "month" to month,
            "year" to year,
            "medicineID" to medicineID,
            "groupID" to groupID,
            "medicineName" to medicineName,
            "groupName" to groupName,
            "status" to status,
        )
    }
}
