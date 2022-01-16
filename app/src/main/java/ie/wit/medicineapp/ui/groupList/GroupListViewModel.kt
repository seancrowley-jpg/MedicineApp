package ie.wit.medicineapp.ui.groupList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.GroupModel
import timber.log.Timber
import java.lang.Exception

class GroupListViewModel : ViewModel() {
    private val groupList = MutableLiveData<List<GroupModel>>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    val observableRecipesList: LiveData<List<GroupModel>>
        get() = groupList

    init {
        load()
    }

    fun load() {
        try {
            Timber.i("Firebase User Id: ${liveFirebaseUser.value?.uid!!}")
            FirebaseDBManager.findAllGroups(liveFirebaseUser.value?.uid!!,
                groupList)
            Timber.i("Load Success : ${groupList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun deleteGroup(group: GroupModel){
        try{
            FirebaseDBManager.deleteGroup(liveFirebaseUser.value?.uid!!, group.uid!!)
            Timber.i("Group Deleted")
        }
        catch (e: Exception) {
            Timber.i("Delete Error : ${e.message}")
        }
    }

}