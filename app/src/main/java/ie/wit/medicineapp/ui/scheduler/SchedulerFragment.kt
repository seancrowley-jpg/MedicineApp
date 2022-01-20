package ie.wit.medicineapp.ui.scheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import ie.wit.medicineapp.databinding.FragmentSchedulerBinding

class SchedulerFragment : Fragment() {

    private var _fragBinding: FragmentSchedulerBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val schedulerViewModel : SchedulerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentSchedulerBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        createCalendar()
        return root
    }

    private fun createCalendar(){
        fragBinding.calendarView.setOnDateChangeListener{
                _, year, month, dayOfMonth ->
            val date = "" + dayOfMonth + "/" + (month + 1) + "/" + year
            fragBinding.selectedDate.text = date
        }
    }

}