package ie.wit.medicineapp.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface GroupStore {
    fun findAllGroups(userid:String,
                groupsList:
                MutableLiveData<List<GroupModel>>
    )
    fun findGroupById(userid:String, groupId: String,
                 group: MutableLiveData<GroupModel>)
    fun createGroup(firebaseUser: MutableLiveData<FirebaseUser>, group: GroupModel)
    fun deleteGroup(userid:String, groupId: String)
    fun updateGroup(userid:String, groupId: String, group: GroupModel)
    fun findGroupMedication(userid: String, groupId: String, medicineList: MutableLiveData<List<MedicineModel>>,)
    fun findMedicineById(userid: String, groupId: String, medicineId: String, medicine: MutableLiveData<MedicineModel>)
    fun createMedicine(firebaseUser: MutableLiveData<FirebaseUser>, medicine: MedicineModel, groupId: String)
    fun updateMedicine(userid: String, groupId: String, medicineId: String, medicine: MedicineModel)
    fun deleteMedicine(userid:String, groupId: String, medicineId: String)
}