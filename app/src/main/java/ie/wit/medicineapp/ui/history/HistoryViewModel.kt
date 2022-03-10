package ie.wit.medicineapp.ui.history

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.models.ReminderModel
import timber.log.Timber
import java.lang.Exception

class HistoryViewModel : ViewModel() {
    private val historyList = MutableLiveData<List<ConfirmationModel>>()
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    val observableHistoryList: LiveData<List<ConfirmationModel>>
        get() = historyList


    fun load(userId: String, day: Int, month: Int, year: Int) {
        try {
            FirebaseDBManager.findHistory(userId, historyList,
                day, month, year)
            Timber.i("Load Success : ${historyList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun deleteConfirmation(confirmation: ConfirmationModel){
        try {
            FirebaseDBManager.deleteConfirmationHistory(liveFirebaseUser.value?.uid!!, confirmation.uid)
            Timber.i("Success")
        }catch (e: Exception) {
            Timber.i("Error Deleting Confirmation : $e.message")
        }
    }
}