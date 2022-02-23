package ie.wit.medicineapp.ui.confirmation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentConfirmationBinding
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.home.Home
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import ie.wit.medicineapp.ui.reminder.ReminderFragmentDirections
import ie.wit.medicineapp.ui.reminder.ReminderViewModel
import ie.wit.medicineapp.ui.utils.ButtonReceiver

class ConfirmationFragment : Fragment() {

    private val confirmationViewModel: ConfirmationViewModel by activityViewModels()
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val args by navArgs<ConfirmationFragmentArgs>()
    private var _fragBinding: FragmentConfirmationBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentConfirmationBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        
        groupViewModel.getGroup(args.userId, args.groupId)
        medicineDetailsViewModel.getMedicine(args.userId, args.groupId, args.medicineId)
        reminderViewModel.getReminder(args.userId,args.reminderId)

        groupViewModel.observableGroup.observe(
            viewLifecycleOwner,
            Observer { group ->
                group?.let { renderGroup() }
            })
        medicineDetailsViewModel.observableMedicine.observe(
            viewLifecycleOwner,
            Observer { medicine ->
                medicine?.let { renderMedicine() }
            })
        reminderViewModel.observableReminder.observe(
            viewLifecycleOwner, Observer { reminder ->
                reminder?.let { renderReminder() }
            }
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val action = ConfirmationFragmentDirections.actionConfirmationFragmentToLoginActivity()
            findNavController().navigate(action)
        }
        setButtonListener(fragBinding)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val actionBar: ActionBar? = (activity as Home).supportActionBar
        if(actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setButtonListener(layout: FragmentConfirmationBinding){
        val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val bundle = arguments
        val requestCode = bundle!!.getInt("notificationID")
        layout.btnConfirm.setOnClickListener(){
            confirmationViewModel.confirmMed(args.userId,args.groupId,args.medicineId,context!!)
            manager.cancel(requestCode)
            Toast.makeText(context, "Confirmed", Toast.LENGTH_SHORT).show()
            val action = ConfirmationFragmentDirections.actionConfirmationFragmentToLoginActivity()
            findNavController().navigate(action)
        }
        layout.btnSnooze.setOnClickListener(){
            val snoozeIntent = Intent(context, ButtonReceiver::class.java)
            snoozeIntent.putExtras(arguments!!)
            snoozeIntent.putExtra("action", "ACTION_SNOOZE")
            ButtonReceiver.snoozeAlarm(context!!,snoozeIntent)
            manager.cancel(requestCode)
            Toast.makeText(context, "Snoozing", Toast.LENGTH_SHORT).show()
            val action = ConfirmationFragmentDirections.actionConfirmationFragmentToLoginActivity()
            findNavController().navigate(action)
        }
    }


    private fun renderGroup() {
        fragBinding.groupVM = groupViewModel

    }

    private fun renderMedicine() {
        fragBinding.medicineVM = medicineDetailsViewModel
    }

    private fun renderReminder() {
        fragBinding.reminderVM = reminderViewModel
    }

}