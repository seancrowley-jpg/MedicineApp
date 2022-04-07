package ie.wit.medicineapp.ui.settings

import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.firebase.FirebaseDBManager
import timber.log.Timber

class SettingsViewModel : ViewModel() {

    fun deleteAccount(userId: String){
        try {
            FirebaseDBManager.deleteAllData(userId)
        }catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

}