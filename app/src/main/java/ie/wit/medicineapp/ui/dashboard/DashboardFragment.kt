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
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentDashboardBinding
import ie.wit.medicineapp.ui.auth.LoggedInViewModel

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
        if (loggedInViewModel.liveFirebaseUser.value!!.displayName != null)
            fragBinding.userNameHeader.text =  loggedInViewModel.liveFirebaseUser.value!!.displayName
        else
            fragBinding.userNameHeader.text =  loggedInViewModel.liveFirebaseUser.value!!.email
        return root
    }

    private fun render() {
        fragBinding.dashboardVM = dashboardViewModel
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                dashboardViewModel.liveFirebaseUser.value = firebaseUser
                dashboardViewModel.load()
            }
        })
    }

}