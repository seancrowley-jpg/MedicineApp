package ie.wit.medicineapp.ui.scheduler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
        schedulerViewModel.observableDate.observe(viewLifecycleOwner, Observer { date ->
            date?.let { render() }
        })
        fragBinding.btnAddReminder.setOnClickListener(){
            if(fragBinding.selectedDate.text != ""){
                val action = SchedulerFragmentDirections.actionSchedulerFragmentToReminderFragment(fragBinding.selectedDate.text.toString())
                findNavController().navigate(action)
            }else{
                Toast.makeText(context, "Please Select a Date", Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    private fun render() {
        fragBinding.schedulerVM = schedulerViewModel
    }

    private fun createCalendar(){
        fragBinding.calendarView.setOnDateChangeListener{
                _, year, month, dayOfMonth ->
            val date = "" + dayOfMonth + "/" + (month + 1) + "/" + year
            fragBinding.selectedDate.text = date
            schedulerViewModel.setReminderDate(fragBinding.selectedDate.text.toString())
        }
    }

}