package ie.wit.medicineapp.ui.group

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.GroupModel

class GroupViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addGroup(firebaseUser: MutableLiveData<FirebaseUser>, group: GroupModel) {
        status.value = try {
            FirebaseDBManager.createGroup(firebaseUser,group)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}