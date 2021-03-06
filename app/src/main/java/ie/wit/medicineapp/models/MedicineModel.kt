package ie.wit.medicineapp.models
import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicineModel(
    var uid: String? = "",
    var name: String = "",
    var quantity: Int = 0,
    var usageDir: String? = "",
    var reminderLimit: Int = 0,
    var dosage: String? = "",
    var sideEffects: MutableList<String?> = ArrayList(),
    var type: Int = -1,
    var unit: String = ""
):Parcelable{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "quantity" to quantity,
            "usageDir" to usageDir,
            "reminderLimit" to reminderLimit,
            "dosage" to dosage,
            "sideEffects" to sideEffects,
            "type" to type,
            "unit" to unit
            )
    }
}
