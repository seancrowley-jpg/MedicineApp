package ie.wit.medicineapp.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.R
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
        if(args.edit){
            medicineViewModel.getMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.groupId, args.medicineId)
            medicineViewModel.observableMedicine.observe(viewLifecycleOwner, Observer {
                    medicine -> medicine?.let { render() }
            })
        }
        setButtonListener(fragBinding)
        return root
    }

    private fun setButtonListener(layout: FragmentMedicineBinding) {
        if (args.edit) {
            layout.addMedicineButton.text = getString(R.string.btn_edit_medication)
            layout.addMedicineButton.setOnClickListener() {
                medicine.uid = args.medicineId
                medicine.name = layout.medicineName.text.toString()
                if (layout.medicineQuantity.text!!.isNotEmpty()) {
                    medicine.quantity = layout.medicineQuantity.text.toString().toInt()
                }
                if (layout.medicineReminderLimit.text!!.isNotEmpty()) {
                    medicine.reminderLimit = layout.medicineReminderLimit.text.toString().toInt()
                }
                medicine.usageDir = layout.medicineUserDir.text.toString()
                medicine.dosage = layout.medicineDosage.text.toString()
                medicineViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                    args.groupId, args.medicineId, medicine
                )
            }
        }
        else {
            layout.addMedicineButton.setOnClickListener() {
                medicine.name = layout.medicineName.text.toString()
                if (layout.medicineQuantity.text!!.isNotEmpty()) {
                    medicine.quantity = layout.medicineQuantity.text.toString().toInt()
                }
                if (layout.medicineReminderLimit.text!!.isNotEmpty()) {
                    medicine.reminderLimit = layout.medicineReminderLimit.text.toString().toInt()
                }
                medicine.usageDir = layout.medicineUserDir.text.toString()
                medicine.dosage = layout.medicineDosage.text.toString()
                medicineViewModel.addMedicine(
                    loggedInViewModel.liveFirebaseUser,
                    medicine,
                    args.groupId
                )
            }
        }
    }

    private fun render() {
        fragBinding.medicinevm = medicineViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}