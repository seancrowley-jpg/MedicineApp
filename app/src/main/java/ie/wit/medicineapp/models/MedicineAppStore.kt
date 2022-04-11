package ie.wit.medicineapp.models

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface MedicineAppStore {
    fun findAllGroups(userid:String,
                groupsList:
                MutableLiveData<List<GroupModel>>
    )
    fun findGroupById(userid:String, groupId: String,
                 group: MutableLiveData<GroupModel>)
    fun createGroup(firebaseUser: MutableLiveData<FirebaseUser>, group: GroupModel)
    fun deleteGroup(userid:String, groupId: String)
    fun updateGroup(userid:String, groupId: String, group: GroupModel)
    fun findGroupMedication(userid: String, groupId: String, medicineList: MutableLiveData<List<MedicineModel>>)
    fun deleteAllGroups(userid: String)
    fun findMedicineById(userid: String, groupId: String, medicineId: String, medicine: MutableLiveData<MedicineModel>)
    fun createMedicine(firebaseUser: MutableLiveData<FirebaseUser>, medicine: MedicineModel, groupId: String)
    fun updateMedicine(userid: String, groupId: String, medicineId: String, medicine: MedicineModel)
    fun deleteMedicine(userid:String, groupId: String, medicineId: String)
    fun deleteAllMedicine(userid: String, groupId: String)
    fun createReminder(firebaseUser: MutableLiveData<FirebaseUser>, reminder: ReminderModel)
    fun findReminders(userid:String, reminderList: MutableLiveData<List<ReminderModel>>)
    fun deleteReminder(userid:String, reminderId: String)
    fun deleteAllReminders(userid:String)
    fun findReminderById(userid:String, reminderId: String, reminder: MutableLiveData<ReminderModel>)
    fun updateReminder(userid:String, reminderId: String, reminder: ReminderModel)
    fun onceOffReminderTriggered(userid: String, reminderId: String)
    fun confirmMedTaken(userid: String,groupId: String,medicineId: String, context: Context, quantityDue: Int)
    fun createConfirmation(userid: String, confirmation: ConfirmationModel)
    fun findHistory(
        userid: String,
        historyList: MutableLiveData<List<ConfirmationModel>>,
        day: Int,
        month: Int,
        year: Int
    )
    fun deleteConfirmationHistory(userid: String, confirmationId: String)
    fun deleteAllConfirmationHistory(userid: String)
    fun getStats(userid: String, groupCount: MutableLiveData<Int>, medCount: MutableLiveData<Int>,
                 historyCount: MutableLiveData<Int>, reminderCount: MutableLiveData<Int>)
    fun deleteAllData(userid: String)
    fun findConfirmationById(userid: String, confirmationId: String, confirmation: MutableLiveData<ConfirmationModel>)
    fun updateConfirmation(userid: String, confirmationId: String, confirmation: ConfirmationModel)
}