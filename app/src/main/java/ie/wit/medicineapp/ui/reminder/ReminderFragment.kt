package ie.wit.medicineapp.ui.reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val schedulerViewModel : SchedulerViewModel by activityViewModels()
    private val args by navArgs<ReminderFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentReminderBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        schedulerViewModel.observableDate.observe(viewLifecycleOwner, Observer { date ->
            date?.let { render() }
        })
        groupViewModel.getGroup(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.groupId)
        groupViewModel.observableGroup.observe(viewLifecycleOwner, Observer { group ->
            group?.let { renderGroup() }
        })
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
        fragBinding.btnAddMed.setOnClickListener(){
            val action = ReminderFragmentDirections.actionReminderFragmentToGroupListFragment(
                reminder = true)
            findNavController().navigate(action)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            val action = ReminderFragmentDirections.actionReminderFragmentToSchedulerFragment()
            findNavController().navigate(action)
        }
        val picker = fragBinding.timePicker
        picker.setOnTimeChangedListener { _, hour, min ->
            Toast.makeText(context,"Time: $hour:$min", Toast.LENGTH_SHORT).show()
        }
        setButtonListener(fragBinding)
        return root
    }

    private fun setButtonListener(layout: FragmentReminderBinding){
        layout.btnSetReminder.setOnClickListener(){
            if(layout.medicineId.text != ""){
                scheduleNotification()
            }else{
                Toast.makeText(context, "Please Select a Medication", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun render() {
        fragBinding.schedulerVM = schedulerViewModel
    }

    private fun renderGroup() {
        fragBinding.groupVM = groupViewModel

    }

    private fun renderMedicine() {
        fragBinding.medicineVM = medicineDetailsViewModel
    }

    private fun scheduleNotification(){
        val intent = Intent(context, NotificationService::class.java)
        intent.putExtra(NotificationService.titleExtra, "Medicine Due!")
        intent.putExtra(
            NotificationService.messageExtra,
            medicineDetailsViewModel.observableMedicine.value!!.name + " " + medicineDetailsViewModel.observableMedicine.value!!.dosage
        )
        intent.putExtra(NotificationService.group, groupViewModel.observableGroup.value!!.name)
        if(groupViewModel.observableGroup.value!!.priorityLevel == 2)
            intent.putExtra(NotificationService.channelID, "highChannelID")
        else
            intent.putExtra(NotificationService.channelID, NotificationService.channelID)

        val pendingIntent = PendingIntent.getBroadcast(
            context, NotificationService.notificationID , intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent)
        val reminder = ReminderModel(uid = loggedInViewModel.liveFirebaseUser.value!!.uid,
            medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!,
            groupID = groupViewModel.observableGroup.value!!.uid!!,
            time = time, requestCode = NotificationService.notificationID)
        reminderViewModel.addReminder(loggedInViewModel.liveFirebaseUser,reminder)
        showAlert(time)
    }

    private fun showAlert(time: Long){
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(context)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
        AlertDialog.Builder(context)
            .setTitle("Reminder Set")
            .setMessage("Reminder Set For:" + dateFormat.format(date) +"\nAt: "+ timeFormat.format(date)
            + "\nPriority: " + groupViewModel.observableGroup.value!!.priorityLevel)
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    private fun getTime(): Long {
        val date = schedulerViewModel.observableDate.value
        val minute  = fragBinding.timePicker.minute
        val hour  = fragBinding.timePicker.hour
        val day = date?.dayOfMonth
        val month = (date?.monthValue?.minus(1))
        val year = date?.year

        val calendar = Calendar.getInstance()
        calendar.set(year!!, month!! ,day!!,hour,minute)
        return calendar.timeInMillis
    }

}