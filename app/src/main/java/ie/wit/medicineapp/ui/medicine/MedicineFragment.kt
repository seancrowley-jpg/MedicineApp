package ie.wit.medicineapp.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.databinding.FragmentMedicineBinding
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import timber.log.Timber

class MedicineFragment : Fragment() {

    private val medicineViewModel: MedicineViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentMedicineBinding? = null
    private val fragBinding get() = _fragBinding!!
    var medicine = MedicineModel()
    private val args by navArgs<MedicineFragmentArgs>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Timber.i("GROUP ID: ${args.groupId}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentMedicineBinding.inflate(inflater, container, false)
        val root: View = fragBinding.root
        setButtonListener(fragBinding)
        return root
    }

    private fun setButtonListener(layout: FragmentMedicineBinding) {
        layout.addMedicineButton.setOnClickListener() {
            medicine.name = layout.medicineName.text.toString()
            if (layout.medicineQuantity.text!!.isNotEmpty()) {
                medicine.quantity = layout.medicineQuantity.text.toString().toInt()
            }
            if (layout.medicineReminderLimit.text!!.isNotEmpty()) {
                medicine.reminderLimit = layout.medicineReminderLimit.text.toString().toInt()
            }
            medicine.usageDir = layout.medicineUserDir.text.toString()
            medicineViewModel.addMedicine(loggedInViewModel.liveFirebaseUser,medicine, args.groupId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}