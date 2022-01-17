package ie.wit.medicineapp.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicineModel(
    var uid: String? = "",
    var name: String = "",
    var quantity: Int = 0,
    var usageDir: String? = "",
    var reminderLimit: Int? = 0,
):Parcelable
