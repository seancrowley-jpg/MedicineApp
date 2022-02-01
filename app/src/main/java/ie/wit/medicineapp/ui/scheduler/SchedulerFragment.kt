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
import ie.wit.medicineapp.adapters.MedicineAdapter
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
    private lateinit var adapter: ReminderAdapter

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
        schedulerViewModel.observableRemindersList.observe(viewLifecycleOwner, Observer {
                reminders -> reminders?.let {
            renderReminders(reminders as ArrayList<ReminderModel>)
        }
        })
        fragBinding.fab.setOnClickListener() {
            val action = SchedulerFragmentDirections.actionSchedulerFragmentToReminderFragment()
            findNavController().navigate(action)
        }

        return root
    }

    private fun renderReminders(reminders: ArrayList<ReminderModel>) {
        fragBinding.recyclerView.adapter = ReminderAdapter(reminders, this)
        adapter = fragBinding.recyclerView.adapter as ReminderAdapter
        if (reminders.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.remindersNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.remindersNotFound.visibility = View.GONE
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

    override fun onReminderDeleteClick(reminder: ReminderModel) {
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

    override fun onReminderClick(reminder: ReminderModel) {
        val action = SchedulerFragmentDirections.actionSchedulerFragmentToReminderFragment(reminder.medicineID, reminder.groupID, edit = true, reminder.uid)
        findNavController().navigate(action)
    }

}