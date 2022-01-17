package ie.wit.medicineapp.ui.medicineList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.MedicineModel
import timber.log.Timber

class MedicineListViewModel : ViewModel() {
    private val medicineList = MutableLiveData<List<MedicineModel>>()
    private val group = MutableLiveData<GroupModel>()

    var observableGroup: LiveData<GroupModel>
        get() = group
        set(value) {group.value = value.value}


    fun getGroup(userid:String, id: String) {
        try {
            FirebaseDBManager.findGroupById(userid, id, group)
            Timber.i("Success got recipe info : ${group.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

    fun getMedication(userid: String, groupId: String){

    }
}