package ie.wit.medicineapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupModel(
    var uid: String? = "",
    var name: String = "",
    var priorityLevel: Long = 0,
    var medication: MutableList<String?> = ArrayList()
) : Parcelable
