package ie.wit.medicineapp.ui.reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.models.ReminderModel
import timber.log.Timber

class ReminderViewModel : ViewModel() {

    private val reminder = MutableLiveData<ReminderModel>()

    var observableReminder: LiveData<ReminderModel>
        get() = reminder
        set(value) {reminder.value = value.value}

    fun addReminder (firebaseUser: MutableLiveData<FirebaseUser>, reminder: ReminderModel) {
        try {
            FirebaseDBManager.createReminder(firebaseUser, reminder)
            Timber.i("Success added reminder : $reminder")
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}