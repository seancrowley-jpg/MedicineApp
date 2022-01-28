package ie.wit.medicineapp.ui.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ie.wit.medicineapp.adapters.ReminderAdapter
import ie.wit.medicineapp.adapters.ReminderListener
import ie.wit.medicineapp.databinding.FragmentSchedulerBinding
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.NotificationService
import timber.log.Timber
import java.time.LocalDate
import java.util.*

class SchedulerFragment : Fragment(), ReminderListener {

    private var _fragBinding: FragmentSchedulerBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val schedulerViewModel : SchedulerViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

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
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        createCalendar()
        schedulerViewModel.observableDate.observe(viewLifecycleOwner, Observer { date ->
            date?.let { render() }
        })
        schedulerViewModel.observableRemindersList.observe(viewLifecycleOwner, Observer {
                reminders -> reminders?.let {
            renderReminders(reminders as ArrayList<ReminderModel>)
        }
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

    private fun renderReminders(reminders: ArrayList<ReminderModel>) {
        fragBinding.recyclerView.adapter = ReminderAdapter(reminders, this)
    }

    private fun render() {
        fragBinding.schedulerVM = schedulerViewModel
    }

    private fun createCalendar(){
        fragBinding.calendarView.setOnDateChangeListener{
                _, year, month, dayOfMonth ->
            //val date = "" + dayOfMonth + "-" + (month + 1) + "-" + year
            //val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val selectedDate = LocalDate.of(year, (month + 1), dayOfMonth)
            //val formattedDate = selectedDate.format(formatter)
            fragBinding.selectedDate.text = selectedDate.toString()
            schedulerViewModel.setReminderDate(selectedDate)
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                schedulerViewModel.liveFirebaseUser.value = firebaseUser
                schedulerViewModel.load()
            }
        })
    }

    override fun onReminderBtnClick(reminder: ReminderModel) {
        val intent = Intent(context, NotificationService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.requestCode , intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (pendingIntent != null){
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context,"ALARM CANCELLED", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "ALARM Not Found", Toast.LENGTH_SHORT).show()
        }
        schedulerViewModel.deleteReminder(reminder)
        schedulerViewModel.load()
    }

}