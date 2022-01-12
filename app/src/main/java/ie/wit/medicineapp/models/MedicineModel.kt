package ie.wit.medicineapp.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicineModel(
    var uid: String? = "",
    var name: String = "",
    var quantity: Long = 0,
    var usageDir: String? = "",
    var reminderLimit: Long? = 0,
):Parcelable
