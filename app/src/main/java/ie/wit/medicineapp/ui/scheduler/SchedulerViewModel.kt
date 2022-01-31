package ie.wit.medicineapp.ui.scheduler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.ReminderModel
import timber.log.Timber
import java.lang.Exception
import java.time.LocalDate
import java.util.*

class SchedulerViewModel : ViewModel() {
    private val reminderList = MutableLiveData<List<ReminderModel>>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    val observableRemindersList: LiveData<List<ReminderModel>>
        get() = reminderList

    init {
        load()
    }

    fun load() {
        try {
            Timber.i("Firebase User Id: ${liveFirebaseUser.value?.uid!!}")
            FirebaseDBManager.findReminders(liveFirebaseUser.value?.uid!!,
                reminderList)
            Timber.i("Load Success : ${reminderList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun deleteReminder(reminder: ReminderModel){
        try {
            FirebaseDBManager.deleteReminder(liveFirebaseUser.value?.uid!!, reminder.uid)
            Timber.i("Success")
        }catch (e: Exception) {
            Timber.i("Error Deleting Reminder : $e.message")
        }
    }
}