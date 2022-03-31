package ie.wit.medicineapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import timber.log.Timber
import java.lang.Exception

class DashboardViewModel : ViewModel() {
    private val groupCount = MutableLiveData<Int>()
    private val medCount = MutableLiveData<Int>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    private val historyCount = MutableLiveData<Int>()
    private val reminderCount = MutableLiveData<Int>()


    val observableGroupCount: LiveData<Int>
        get() = groupCount

    val observableMedCount: LiveData<Int>
        get() = medCount

    val observableHistoryCount: LiveData<Int>
        get() = historyCount

    val observableReminderCount: LiveData<Int>
        get() = reminderCount


    fun load() {
        try {
            FirebaseDBManager.getStats(liveFirebaseUser.value?.uid!!,
                groupCount, medCount, historyCount, reminderCount)
            Timber.i("Load Success : GroupCount${groupCount.value.toString()} MedCount${medCount.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }
}