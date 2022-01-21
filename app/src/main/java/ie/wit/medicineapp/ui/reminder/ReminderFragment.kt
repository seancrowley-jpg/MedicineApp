package ie.wit.medicineapp.ui.reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.databinding.FragmentReminderBinding
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import ie.wit.medicineapp.ui.scheduler.SchedulerViewModel

class ReminderFragment : Fragment() {

    private var _fragBinding: FragmentReminderBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val schedulerViewModel : SchedulerViewModel by activityViewModels()
    private val args by navArgs<ReminderFragmentArgs>()

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
        return root
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

}