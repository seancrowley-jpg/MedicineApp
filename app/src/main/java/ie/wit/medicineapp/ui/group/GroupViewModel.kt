package ie.wit.medicineapp.ui.group

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.GroupModel
import timber.log.Timber

class GroupViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()
    private val group = MutableLiveData<GroupModel>()

    val observableStatus: LiveData<Boolean>
        get() = status

    var observableGroup: LiveData<GroupModel>
        get() = group
        set(value) {group.value = value.value}

    fun addGroup(firebaseUser: MutableLiveData<FirebaseUser>, group: GroupModel) {
        status.value = try {
            FirebaseDBManager.createGroup(firebaseUser,group)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getGroup(userid:String, id: String) {
        try {
            FirebaseDBManager.findGroupById(userid, id, group)
            Timber.i("Success got group info : ${group.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

    fun updateGroup(group: GroupModel,userid:String, id: String) {
        try {
            FirebaseDBManager.updateGroup(userid, id, group)
            Timber.i("Success updated Group : $group")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }
}