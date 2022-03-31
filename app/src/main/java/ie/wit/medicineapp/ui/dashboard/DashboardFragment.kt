package ie.wit.medicineapp.ui.dashboard

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentDashboardBinding
import ie.wit.medicineapp.databinding.FragmentMedicineBinding
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import timber.log.Timber
import java.util.*

class DashboardFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentDashboardBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = fragBinding.root
        dashboardViewModel.observableGroupCount.observe(viewLifecycleOwner, Observer {
                groupCount -> groupCount?.let { render() }
        })
        dashboardViewModel.observableMedCount.observe(viewLifecycleOwner, Observer {
                medCount -> medCount?.let { render() }
        })
        dashboardViewModel.observableHistoryCount.observe(viewLifecycleOwner, Observer {
                historyCount -> historyCount?.let { render() }
        })
        dashboardViewModel.observableReminderCount.observe(viewLifecycleOwner, Observer {
                reminderCount -> reminderCount?.let { render() }
        })
        setButtonListener(fragBinding)
        welcomeMessage(fragBinding)
        return root
    }

    private fun render() {
        fragBinding.dashboardVM = dashboardViewModel
    }

    private fun setButtonListener(layout: FragmentDashboardBinding) {
        layout.btnGroups.setOnClickListener(){
            val action = DashboardFragmentDirections.actionDashboardFragmentToGroupListFragment()
            findNavController().navigate(action)
        }
        layout.btnScheduler.setOnClickListener(){
            val action = DashboardFragmentDirections.actionDashboardFragmentToSchedulerFragment()
            findNavController().navigate(action)
        }
        layout.btnHistory.setOnClickListener(){
            val action = DashboardFragmentDirections.actionDashboardFragmentToHistoryFragment()
            findNavController().navigate(action)
        }
    }

    private fun welcomeMessage(layout: FragmentDashboardBinding){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        Timber.i("$hour")
        when (hour) {
            in 12..16 -> {
                layout.welcomeHeader.text = getString(R.string.dashboard_afternoon)
            }
            in 17..20 -> {
                layout.welcomeHeader.text = getString(R.string.dashboard_evening)
            }
            in 21..23 -> {
                layout.welcomeHeader.text = getString(R.string.dashboard_night)
            }
            else -> {
                layout.welcomeHeader.text = getString(R.string.dashboard_morning)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                dashboardViewModel.liveFirebaseUser.value = firebaseUser
                if (dashboardViewModel.liveFirebaseUser.value!!.displayName != null)
                    fragBinding.userNameHeader.text =  dashboardViewModel.liveFirebaseUser.value!!.displayName
                else
                fragBinding.userNameHeader.text =  dashboardViewModel.liveFirebaseUser.value!!.email

                dashboardViewModel.load()
            }
        })
    }

}