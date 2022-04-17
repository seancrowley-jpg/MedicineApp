package ie.wit.medicineapp.ui.medicineList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.MedicineModel
import timber.log.Timber
import java.lang.Exception

class MedicineListViewModel : ViewModel() {
    private val medicationList = MutableLiveData<List<MedicineModel>>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    val observableMedicationList: LiveData<List<MedicineModel>>
        get() = medicationList


    fun load(groupId: String) {
        try {
            Timber.i("Firebase User Id: ${liveFirebaseUser.value?.uid!!}")
            FirebaseDBManager.findGroupMedication(liveFirebaseUser.value?.uid!!,
                groupId, medicationList)
            Timber.i("Load Success : ${medicationList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun deleteMedicine(medicine: MedicineModel, groupId: String){
        try {
            FirebaseDBManager.deleteMedicine(liveFirebaseUser.value?.uid!!, groupId, medicine.uid!!)
            Timber.i("Medicine Deleted")
        }
        catch (e: Exception) {
            Timber.i("Delete Error : ${e.message}")
        }
    }

    fun deleteAllMeds(groupId: String){
        try{
            FirebaseDBManager.deleteAllMedicine(liveFirebaseUser.value?.uid!!, groupId)
            Timber.i("All Meds deleted for Group ID: $groupId")
        }
        catch (e: Exception){
            Timber.i("Delete Error : ${e.message}")
        }
    }


}