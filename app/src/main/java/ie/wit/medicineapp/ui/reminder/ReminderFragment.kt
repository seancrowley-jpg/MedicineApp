package ie.wit.medicineapp.ui.reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentReminderBinding
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import ie.wit.medicineapp.ui.scheduler.SchedulerViewModel
import ie.wit.medicineapp.ui.utils.NotificationService
import java.util.*

class ReminderFragment : Fragment() {

    private var _fragBinding: FragmentReminderBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val schedulerViewModel: SchedulerViewModel by activityViewModels()
    private val args by navArgs<ReminderFragmentArgs>()
    private var alarmManager: AlarmManager? = null
    private lateinit var pendingIntent: PendingIntent
    var reminder = ReminderModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentReminderBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        groupViewModel.getGroup(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.groupId)

        medicineDetailsViewModel.getMedicine(
            loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.groupId,
            args.medicineId
        )
        medicineDetailsViewModel.observableMedicine.observe(
            viewLifecycleOwner,
            Observer { medicine ->
                medicine?.let { renderMedicine() }
            })

        fragBinding.btnAddMed.setOnClickListener() {
            val action = ReminderFragmentDirections.actionReminderFragmentToGroupListFragment(
                reminder = true
            )
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val action = ReminderFragmentDirections.actionReminderFragmentToSchedulerFragment()
            findNavController().navigate(action)
        }

        if (args.edit) {
            reminderViewModel.getReminder(
                loggedInViewModel.liveFirebaseUser.value!!.uid,
                args.reminderId
            )
            reminderViewModel.observableReminder.observe(
                viewLifecycleOwner,
                Observer { reminder -> reminder?.let { render() } })

            fragBinding.btnAddMed.text = getString(R.string.btn_change_medication)
            fragBinding.btnAddMed.setOnClickListener() {
                val action = ReminderFragmentDirections.actionReminderFragmentToGroupListFragment(
                    reminder = true, reminderId = args.reminderId, edit = true
                )
                findNavController().navigate(action)
            }
        }

        setButtonListener(fragBinding)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.edit) {
            inflater.inflate(R.menu.menu_reminder, menu)
            super.onCreateOptionsMenu(menu, inflater)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.schedulerFragment) {
            schedulerViewModel.deleteReminder(reminderViewModel.observableReminder.value!!)
            val intent = Intent(context, NotificationService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, reminderViewModel.observableReminder.value!!.requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Toast.makeText(context, "ALARM CANCELLED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ALARM Not Found", Toast.LENGTH_SHORT).show()
            }
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)

    }


    private fun setButtonListener(layout: FragmentReminderBinding) {
        if (args.edit) {
            layout.btnSetReminder.text = getString(R.string.btn_edit_reminder)
            layout.btnSetReminder.setOnClickListener() {
                if (layout.medicineId.text != "") {
                    updateReminder()
                    Toast.makeText(context, "Reminder Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please Select a Medication", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            layout.btnSetReminder.setOnClickListener() {
                if (layout.medicineId.text != "") {
                    scheduleReminder()
                    Toast.makeText(context, "Reminder Created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please Select a Medication", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun render() {
        fragBinding.reminderVM = reminderViewModel
    }

    private fun renderGroup() {
        fragBinding.groupVM = groupViewModel

    }

    private fun renderMedicine() {
        fragBinding.medicineVM = medicineDetailsViewModel
    }

    private fun scheduleReminder() {
        val minute = fragBinding.timePicker.minute
        val hour = fragBinding.timePicker.hour
        val day = fragBinding.datePicker.dayOfMonth
        val month = fragBinding.datePicker.month
        val year = fragBinding.datePicker.year
        val time = getTime(year, month, day, hour, minute)
        reminder = ReminderModel(
            uid = loggedInViewModel.liveFirebaseUser.value!!.uid,
            medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!,
            groupID = groupViewModel.observableGroup.value!!.uid!!,
            time = time, requestCode = Random().nextInt(),
            minute = minute, hour = hour, day = day, month = month, year = year,
            medName = medicineDetailsViewModel.observableMedicine.value!!.name,
            medDosage = medicineDetailsViewModel.observableMedicine.value!!.dosage!!,
            groupPriorityLevel = groupViewModel.observableGroup.value!!.priorityLevel
        )
        reminderViewModel.addReminder(loggedInViewModel.liveFirebaseUser, reminder)
        showAlert(time)
    }

    private fun updateReminder() {
        val minute = fragBinding.timePicker.minute
        val hour = fragBinding.timePicker.hour
        val day = fragBinding.datePicker.dayOfMonth
        val month = fragBinding.datePicker.month
        val year = fragBinding.datePicker.year
        val time = getTime(year, month, day, hour, minute)
        reminder = ReminderModel(
            uid = args.reminderId,
            medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!,
            groupID = groupViewModel.observableGroup.value!!.uid!!,
            time = time, requestCode = reminderViewModel.observableReminder.value!!.requestCode,
            minute = minute, hour = hour, day = day, month = month, year = year,
            medName = medicineDetailsViewModel.observableMedicine.value!!.name,
            medDosage = medicineDetailsViewModel.observableMedicine.value!!.dosage!!,
            groupPriorityLevel = groupViewModel.observableGroup.value!!.priorityLevel
        )
        reminderViewModel.updateReminder(
            reminder,
            loggedInViewModel.liveFirebaseUser.value!!.uid,
            args.reminderId
        )
        showAlert(time)
    }

    private fun showAlert(time: Long) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(context)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
        AlertDialog.Builder(context)
            .setTitle("Reminder Set")
            .setMessage(
                "Reminder Set For:" + dateFormat.format(date) + "\nAt: " + timeFormat.format(
                    date
                )
                        + "\nPriority: " + groupViewModel.observableGroup.value!!.priorityLevel
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun getTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }
}