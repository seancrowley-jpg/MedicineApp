package ie.wit.medicineapp.ui.medicineDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.MedicineModel
import timber.log.Timber

class MedicineDetailsViewModel : ViewModel() {
    private val medicine = MutableLiveData<MedicineModel>()

    var observableMedicine: LiveData<MedicineModel>
        get() = medicine
        set(value) {medicine.value = value.value}

    fun getMedicine(userid:String, groupId: String, medicineId: String) {
        try {
            FirebaseDBManager.findMedicineById(userid, groupId, medicineId, medicine)
            Timber.i("Success got medicine info : ${medicine.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

    fun updateMedicine(userid:String, groupId: String, medicineId: String, medicine: MedicineModel){
        try {
            FirebaseDBManager.updateMedicine(userid, groupId, medicineId, medicine)
            Timber.i("Success updated Medication : $medicine")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }
}