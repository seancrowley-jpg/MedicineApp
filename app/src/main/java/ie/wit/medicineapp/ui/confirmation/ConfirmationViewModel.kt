package ie.wit.medicineapp.ui.confirmation

import android.content.Context
import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.firebase.FirebaseDBManager
import timber.log.Timber

class ConfirmationViewModel : ViewModel() {

    fun confirmMed(userId: String, groupId: String, medicineId: String, context: Context){
        try {
            FirebaseDBManager.confirmMedTaken(userId, groupId, medicineId, context)
            Timber.i("Success Medication taken confirmed")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }
}