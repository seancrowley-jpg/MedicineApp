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


    val observableGroupCount: LiveData<Int>
        get() = groupCount

    val observableMedCount: LiveData<Int>
        get() = medCount


    fun load() {
        try {
            FirebaseDBManager.getStats(liveFirebaseUser.value?.uid!!,
                groupCount, medCount)
            Timber.i("Load Success : GroupCount${groupCount.value.toString()} MedCount${medCount.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }
}