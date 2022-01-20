package ie.wit.medicineapp.ui.reminder

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentReminderBinding
import ie.wit.medicineapp.databinding.FragmentSchedulerBinding

class ReminderFragment : Fragment() {

    private var _fragBinding: FragmentReminderBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var reminderViewModel: ReminderViewModel
    private val args by navArgs<ReminderFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentReminderBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.date.text = args.date
        return root
    }

}