package ie.wit.medicineapp.ui.scheduler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.util.*

class SchedulerViewModel : ViewModel() {
    private var date = MutableLiveData<LocalDate>()

    var observableDate: LiveData<LocalDate>
        get() = date
        set(value) {date.value = value.value}

    fun setReminderDate(reminderDate:LocalDate){
        date.value = reminderDate
    }
}