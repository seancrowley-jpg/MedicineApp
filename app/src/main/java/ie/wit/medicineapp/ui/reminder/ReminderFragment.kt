package ie.wit.medicineapp.ui.reminder

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentReminderBinding
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import ie.wit.medicineapp.ui.scheduler.SchedulerViewModel
import ie.wit.medicineapp.ui.utils.NotificationService
import timber.log.Timber
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
        reminderViewModel.observableReminder.observe(
            viewLifecycleOwner,
            Observer { reminder -> reminder?.let { render() } })
        if (args.edit) {
            reminderViewModel.getReminder(
                loggedInViewModel.liveFirebaseUser.value!!.uid,
                args.reminderId
            )
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Edit Reminder"
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
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
            val confirmBool = sharedPreferences.getBoolean("confirm_delete", true)
            if (confirmBool) {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("Delete Reminder?")
                alertDialog.setMessage("Are you sure you want to delete this Reminder? ")
                alertDialog.setNegativeButton("No") { _, _ -> }
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    schedulerViewModel.deleteReminder(reminderViewModel.observableReminder.value!!)
                    val intent = Intent(context, NotificationService::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, reminderViewModel.observableReminder.value!!.requestCode, intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val alarmManager =
                        context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        Toast.makeText(context, "ALARM CANCELLED", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "ALARM Not Found", Toast.LENGTH_SHORT).show()
                    }
                    NavigationUI.onNavDestinationSelected(
                        item,
                        requireView().findNavController()
                    ) || super.onOptionsItemSelected(item)
                }
                alertDialog.show()
            }
            else{
                schedulerViewModel.deleteReminder(reminderViewModel.observableReminder.value!!)
                val intent = Intent(context, NotificationService::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, reminderViewModel.observableReminder.value!!.requestCode, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager =
                    context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    Toast.makeText(context, "ALARM CANCELLED", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "ALARM Not Found", Toast.LENGTH_SHORT).show()
                }
                return NavigationUI.onNavDestinationSelected(
                    item,
                    requireView().findNavController()
                ) || super.onOptionsItemSelected(item)
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun setButtonListener(layout: FragmentReminderBinding) {
        if (args.edit) {
            layout.btnSetReminder.text = getString(R.string.btn_edit_reminder)
            layout.btnSetReminder.setOnClickListener() {
                if (validateForm()) {
                    updateReminder()
                    val action = ReminderFragmentDirections.actionReminderFragmentToSchedulerFragment()
                    findNavController().navigate(action)
                    Toast.makeText(context, "Reminder Updated", Toast.LENGTH_SHORT).show()
                }
            }
            layout.btnAddMed.text = getString(R.string.btn_change_medication)
            layout.btnAddMed.setOnClickListener() {
                val action = ReminderFragmentDirections.actionReminderFragmentToGroupListFragment(
                    reminder = true, reminderId = args.reminderId, edit = true
                )
                findNavController().navigate(action)
            }
        } else {
            layout.btnSetReminder.setOnClickListener() {
                if (validateForm()) {
                    scheduleReminder()
                    val action = ReminderFragmentDirections.actionReminderFragmentToSchedulerFragment()
                    findNavController().navigate(action)
                    Toast.makeText(context, "Reminder Created", Toast.LENGTH_SHORT).show()
                }
            }
            layout.btnAddMed.setOnClickListener() {
                val action = ReminderFragmentDirections.actionReminderFragmentToGroupListFragment(
                    reminder = true
                )
                findNavController().navigate(action)
            }
        }
        layout.btnRepeat.setOnClickListener() {
            showRepeatDialog()
        }
    }

    private fun render() {
        fragBinding.reminderVM = reminderViewModel
        if(args.edit) {
            fragBinding.timePicker.hour = reminderViewModel.observableReminder.value!!.hour
            fragBinding.timePicker.minute = reminderViewModel.observableReminder.value!!.minute
        }
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
        val time = getTime(hour, minute)

        reminder.uid = loggedInViewModel.liveFirebaseUser.value!!.uid
        reminder.medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!
        reminder.groupID = groupViewModel.observableGroup.value!!.uid!!
        reminder.groupName = groupViewModel.observableGroup.value!!.name
        reminder.time = time
        reminder.requestCode = Random().nextInt()
        reminder.minute = minute
        reminder.hour = hour
        reminder.medName = medicineDetailsViewModel.observableMedicine.value!!.name
        reminder.medDosage = medicineDetailsViewModel.observableMedicine.value!!.dosage!!
        reminder.groupPriorityLevel = groupViewModel.observableGroup.value!!.priorityLevel
        reminder.quantity = fragBinding.quantityInput.text.toString().toInt()
        reminder.unit = medicineDetailsViewModel.observableMedicine.value!!.unit

        reminderViewModel.addReminder(loggedInViewModel.liveFirebaseUser, reminder)
        showAlert(time)
    }

    private fun updateReminder() {
        val minute = fragBinding.timePicker.minute
        val hour = fragBinding.timePicker.hour
        val time = getTime(hour, minute)

        reminder.uid = reminderViewModel.observableReminder.value!!.uid
        reminder.medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!
        reminder.groupID = groupViewModel.observableGroup.value!!.uid!!
        reminder.groupName = groupViewModel.observableGroup.value!!.name
        reminder.time = time
        reminder.requestCode = reminderViewModel.observableReminder.value!!.requestCode
        reminder.minute = minute
        reminder.hour = hour
        reminder.medName = medicineDetailsViewModel.observableMedicine.value!!.name
        reminder.medDosage = medicineDetailsViewModel.observableMedicine.value!!.dosage!!
        reminder.groupPriorityLevel = groupViewModel.observableGroup.value!!.priorityLevel
        reminder.repeatDays = reminderViewModel.observableReminder.value!!.repeatDays
        reminder.quantity = reminderViewModel.observableReminder.value!!.quantity
        reminder.unit = medicineDetailsViewModel.observableMedicine.value!!.unit

        reminderViewModel.updateReminder(
            reminder,
            loggedInViewModel.liveFirebaseUser.value!!.uid,
            args.reminderId
        )
        if(reminderViewModel.observableReminder.value!!.active)
            NotificationService.cancelAlarm(context!!, reminder, loggedInViewModel.liveFirebaseUser.value!!.uid)
        showAlert(time)
    }

    private fun showAlert(time: Long) {
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(context)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
        var level = groupViewModel.observableGroup.value!!.priorityLevel.toString()
        level = if(level == "1"){
            "High"
        }else
            "Low"
        AlertDialog.Builder(context)
            .setTitle("Reminder Set")
            .setMessage(
                "Reminder Set For:" + dateFormat.format(time) + "\nAt: " + timeFormat.format(
                    time
                )
                        + "\nPriority: " + level
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun getTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, minute)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.SECOND,0)
        }
        if(calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.timeInMillis
    }

    private fun showRepeatDialog(){
        val selectedItems = ArrayList<Int>()
        val builder = AlertDialog.Builder(context)
        val checkedItems : BooleanArray = booleanArrayOf(false,false,false,false,false,false,false)
        if (args.edit){
            for(i in reminderViewModel.observableReminder.value!!.repeatDays!!){
                checkedItems[i -1] = true
            }
            selectedItems.addAll(reminderViewModel.observableReminder.value!!.repeatDays!!)
        }
        else{
            for(i in reminder.repeatDays!!){
                checkedItems[i -1] = true
            }
            selectedItems.addAll(reminder.repeatDays!!)
        }
        builder.setTitle("Repeat")
            .setMultiChoiceItems(R.array.days_of_week,checkedItems,
                DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
                    if (isChecked) {
                        selectedItems.add(which + 1)
                        checkedItems[which] = true
                    } else if (selectedItems.contains(which +1)) {
                        selectedItems.remove(which + 1)
                        checkedItems[which] = false
                    }
                })
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { _, _ ->
                    if(args.edit) {
                        reminderViewModel.observableReminder.value!!.repeatDays?.clear()
                        reminderViewModel.observableReminder.value!!.repeatDays?.addAll(
                            selectedItems
                        )
                    }
                    else {
                        reminder.repeatDays?.clear()
                        reminder.repeatDays?.addAll(selectedItems)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { _, _ ->

                })
        builder.create()
        builder.show()
    }

    private fun validateForm(): Boolean {
        var valid = true
        if(fragBinding.quantityInput.text.toString() == "")  fragBinding.quantityInput.setText("0")
        if (fragBinding.quantityInput.text.toString().toInt() <=0 ){
            fragBinding.quantityInput.requestFocus()
            fragBinding.quantityInput.error = "Please enter a quantity greater than 0"
            valid = false
        }
        if (fragBinding.medicineId.text.toString().isEmpty() || medicineDetailsViewModel.observableMedicine.value == null) {
            fragBinding.medicineId.requestFocus()
            Toast.makeText(context, "Please select a Medication", Toast.LENGTH_LONG).show()
            valid = false
        }
        return valid
    }

    override fun onDestroy() {
        super.onDestroy()
        //medicineDetailsViewModel.observableMedicine.value!!.name = ""
    }

}