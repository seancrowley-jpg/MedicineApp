package ie.wit.medicineapp.ui.scheduler

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.ReminderAdapter
import ie.wit.medicineapp.adapters.ReminderListener
import ie.wit.medicineapp.databinding.FragmentSchedulerBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.ReminderModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import ie.wit.medicineapp.ui.reminder.ReminderViewModel
import ie.wit.medicineapp.ui.utils.NotificationService
import ie.wit.medicineapp.ui.utils.SwipeToDeleteCallback
import java.util.*

class SchedulerFragment : Fragment(), ReminderListener {

    private var _fragBinding: FragmentSchedulerBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val schedulerViewModel : SchedulerViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var adapter: ReminderAdapter
    private var alarmManager: AlarmManager? = null
    private lateinit var pendingIntent: PendingIntent
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    lateinit var loader : AlertDialog


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
        loader = createLoader(requireActivity())
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        showLoader(loader,"Loading Reminders")
        schedulerViewModel.observableRemindersList.observe(viewLifecycleOwner, Observer {
                reminders -> reminders?.let {
            renderReminders(reminders as ArrayList<ReminderModel>)
            hideLoader(loader)
            checkSwipeRefresh()
        }
        })
        setSwipeRefresh()
        fragBinding.fab.setOnClickListener() {
            val action = SchedulerFragmentDirections.actionSchedulerFragmentToReminderFragment()
            findNavController().navigate(action)
        }

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
                val confirmBool = sharedPreferences.getBoolean("confirm_delete", true)
                if (confirmBool) {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Delete Reminder?")
                    alertDialog.setMessage("Are you sure you want to delete this Reminder? ")
                    alertDialog.setNegativeButton("No") { _, _ ->
                        schedulerViewModel.load()
                    }
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        adapter.removeAt(viewHolder.adapterPosition)
                        val reminder = viewHolder.itemView.tag as ReminderModel
                        schedulerViewModel.deleteReminder(reminder)
                        val intent = Intent(context, NotificationService::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, reminder.requestCode, intent,
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
                        hideLoader(loader)
                    }
                    alertDialog.setOnDismissListener {schedulerViewModel.load()}
                    alertDialog.show()
                }
                else{
                    adapter.removeAt(viewHolder.adapterPosition)
                    val reminder = viewHolder.itemView.tag as ReminderModel
                    schedulerViewModel.deleteReminder(reminder)
                    val intent = Intent(context, NotificationService::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, reminder.requestCode, intent,
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
                    hideLoader(loader)
                }
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_scheduler, menu)
        val searchItem = menu.findItem(R.id.item_search_reminders)
        val searchView = searchItem?.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                adapter.filter.filter(p0)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_all_reminders) {
            if (schedulerViewModel.observableRemindersList.value!!.isNotEmpty()) {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("Delete All Reminders")
                alertDialog.setMessage("Are you sure you want to delete all your reminders?")
                alertDialog.setNegativeButton("No") { _, _ -> }
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    schedulerViewModel.observableRemindersList.value!!.forEach { reminder ->
                        NotificationService.cancelAlarm(
                            context!!,
                            reminder,
                            loggedInViewModel.liveFirebaseUser.value!!.uid
                        )
                    }
                    schedulerViewModel.deleteAllReminders(loggedInViewModel.liveFirebaseUser.value!!.uid)
                    schedulerViewModel.load()
                }
                alertDialog.show()
            }
            else{
                Toast.makeText(context, "No Reminders To Delete Found", Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
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
        showLoader(loader,"Loading...")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                schedulerViewModel.liveFirebaseUser.value = firebaseUser
                schedulerViewModel.load()
            }
        })
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Loading..")
            schedulerViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }


    override fun onReminderClick(reminder: ReminderModel) {
        val action = SchedulerFragmentDirections.actionSchedulerFragmentToReminderFragment(reminder.medicineID, reminder.groupID, edit = true, reminder.uid)
        findNavController().navigate(action)
    }

    override fun onReminderToggleBtnOff(reminder: ReminderModel) {
        NotificationService.cancelAlarm(context!!, reminder,loggedInViewModel.liveFirebaseUser.value!!.uid)
        Toast.makeText(context,"ALARM CANCELLED", Toast.LENGTH_SHORT).show()
        reminder.active = false
        reminderViewModel.updateReminder(reminder, loggedInViewModel.liveFirebaseUser.value!!.uid, reminder.uid)
    }

    override fun onReminderToggleBtnOn(reminder: ReminderModel) {
        if (reminder.repeatDays!!.size == 0) {
            NotificationService.setOnceOffAlarm(context!!, reminder,loggedInViewModel.liveFirebaseUser.value!!.uid)
            //Toast.makeText(context, "ALARM ON ${Date(reminder.time)}", Toast.LENGTH_SHORT).show()
        }
        else
        {
            NotificationService.setRepeatingAlarm(context!!, reminder,loggedInViewModel.liveFirebaseUser.value!!.uid)
            Toast.makeText(context, "Repeating ALARM ON", Toast.LENGTH_SHORT).show()
        }
        reminder.active = true
        reminderViewModel.updateReminder(reminder, loggedInViewModel.liveFirebaseUser.value!!.uid, reminder.uid)
    }

    /*override fun onReminderToggleBtnOff(reminder: ReminderModel) {
        val intent = Intent(context, NotificationService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.requestCode , intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (pendingIntent != null){
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context,"ALARM CANCELLED", Toast.LENGTH_SHORT).show()
            reminder.active = false
            reminderViewModel.updateReminder(reminder, loggedInViewModel.liveFirebaseUser.value!!.uid, reminder.uid)
        }
        else {
            Toast.makeText(context, "ALARM Not Found", Toast.LENGTH_SHORT).show()
        }
    }*/

    /*override fun onReminderToggleBtnOn(reminder: ReminderModel) {
        val intent = Intent(context, NotificationService::class.java)
        intent.putExtra(NotificationService.titleExtra, "Medicine Due!")
        intent.putExtra(
            NotificationService.messageExtra,
            reminder.medName + " " + reminder.medDosage + " " + reminder.requestCode
        )
        if (reminder.groupPriorityLevel == 2)
            intent.putExtra(NotificationService.channelID, "highChannelID")
        else
            intent.putExtra(NotificationService.channelID, NotificationService.channelID)
        NotificationService.notificationID = reminder.requestCode
        pendingIntent = PendingIntent.getBroadcast(
            context, NotificationService.notificationID, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager!!.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.time,
            pendingIntent
        )
        reminder.active = true
        reminderViewModel.updateReminder(reminder, loggedInViewModel.liveFirebaseUser.value!!.uid, reminder.uid)
        Toast.makeText(context, "ALARM ON", Toast.LENGTH_SHORT).show()
    }*/

}