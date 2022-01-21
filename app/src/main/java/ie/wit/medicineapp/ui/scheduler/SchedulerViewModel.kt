package ie.wit.medicineapp.ui.scheduler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SchedulerViewModel : ViewModel() {
    private var date = MutableLiveData<String>()

    var observableDate: LiveData<String>
        get() = date
        set(value) {date.value = value.value}

    fun setReminderDate(reminderDate:String){
        date.value = reminderDate
    }
}