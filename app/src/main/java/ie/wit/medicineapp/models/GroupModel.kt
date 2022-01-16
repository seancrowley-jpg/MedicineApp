package ie.wit.medicineapp.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupModel(
    var uid: String? = "",
    var name: String = "",
    var priorityLevel: Int = 0,
    var medication: MutableList<String?> = ArrayList()
) : Parcelable{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "priorityLevel" to priorityLevel,
            "medication" to medication,
        )
    }
}
