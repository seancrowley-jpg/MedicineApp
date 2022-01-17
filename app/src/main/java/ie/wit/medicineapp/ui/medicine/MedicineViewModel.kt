package ie.wit.medicineapp.ui.medicine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.MedicineModel

class MedicineViewModel : ViewModel() {

    private val medicine = MutableLiveData<MedicineModel>()

    var observableMedicine: LiveData<MedicineModel>
        get() = medicine
        set(value) {medicine.value = value.value}
}