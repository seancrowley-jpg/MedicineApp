package ie.wit.medicineapp.ui.addConfirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.wit.medicineapp.firebase.FirebaseDBManager
import ie.wit.medicineapp.models.ConfirmationModel
import timber.log.Timber

class AddConfirmationViewModel : ViewModel() {
    private val confirmation = MutableLiveData<ConfirmationModel>()

    var observableConfirmation: LiveData<ConfirmationModel>
        get() = confirmation
        set(value) {confirmation.value = value.value}

    fun getConfirmation(userid:String, confirmationId: String) {
        try {
            FirebaseDBManager.findConfirmationById(userid, confirmationId, confirmation)
            Timber.i("Success got group info : ${confirmation.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

    fun updateConfirmation(confirmation: ConfirmationModel, userid:String, confirmationId: String) {
        try {
            FirebaseDBManager.updateConfirmation(userid, confirmationId, confirmation)
            Timber.i("Success updated Confirmation : $confirmation")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")
        }
    }

    fun createConfirmation(userid: String, confirmation: ConfirmationModel){
        try {
            FirebaseDBManager.createConfirmation(userid, confirmation)
            Timber.i("Success created Confirmation : $confirmation")
        } catch (e: Exception) {
            Timber.i("Error : $e.message")

        }
    }
}