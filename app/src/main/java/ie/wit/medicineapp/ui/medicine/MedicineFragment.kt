package ie.wit.medicineapp.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
        medicineViewModel.observableMedicine.observe(viewLifecycleOwner, Observer {
                medicine -> medicine?.let { render() }
        })
        if(args.edit){
            medicineViewModel.getMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.groupId, args.medicineId)
            (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.menu_medicine_edit)
        }
        setButtonListener(fragBinding)
        setUpPicker(fragBinding)
        return root
    }

    private fun setButtonListener(layout: FragmentMedicineBinding) {
        layout.medicineQuantity.transformationMethod = null
        layout.medicineReminderLimit.transformationMethod = null
        val units: Array<String> = resources.getStringArray(R.array.unit)
        if (args.edit) {
            layout.addMedicineButton.text = getString(R.string.btn_edit_medication)
            layout.addMedicineButton.setOnClickListener() {
                if (validateForm()) {
                    medicine.uid = args.medicineId
                    medicine.name = layout.medicineName.text.toString().trim()
                    medicine.quantity = layout.medicineQuantity.text.toString().toInt()
                    medicine.reminderLimit = layout.medicineReminderLimit.text.toString().toInt()
                    medicine.usageDir = layout.medicineUserDir.text.toString().trim()
                    medicine.dosage = layout.medicineDosage.text.toString().trim()
                    medicine.sideEffects = medicineViewModel.observableMedicine.value!!.sideEffects
                    medicine.type = medicineViewModel.observableMedicine.value!!.type
                    medicine.unit = units[medicineViewModel.observableMedicine.value!!.type]
                    medicineViewModel.updateMedicine(
                        loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        args.groupId, args.medicineId, medicine
                    )
                    findNavController().popBackStack()
                }
            }
        }
        else {
            layout.addMedicineButton.setOnClickListener() {
                if(validateForm()) {
                    medicine.name = layout.medicineName.text.toString().trim()
                    medicine.quantity = layout.medicineQuantity.text.toString().toInt()
                    medicine.reminderLimit = layout.medicineReminderLimit.text.toString().toInt()
                    medicine.usageDir = layout.medicineUserDir.text.toString().trim()
                    medicine.dosage = layout.medicineDosage.text.toString().trim()
                    medicine.type = layout.typePicker.value
                    medicine.unit = units[layout.typePicker.value]
                    medicineViewModel.addMedicine(
                        loggedInViewModel.liveFirebaseUser,
                        medicine,
                        args.groupId
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun render() {
        fragBinding.medicinevm = medicineViewModel
    }

    private fun validateForm(): Boolean {
        var valid = true
        if(fragBinding.medicineQuantity.text.toString() == "")  fragBinding.medicineQuantity.setText("0")
        if(fragBinding.medicineReminderLimit.text.toString() == "")  fragBinding.medicineReminderLimit.setText("0")
        if (fragBinding.medicineName.text!!.isEmpty()){
            fragBinding.medicineName.requestFocus()
            fragBinding.medicineName.error = "Please enter medication name"
            valid = false
        }
        if (fragBinding.medicineName.text!!.length >= 50){
            fragBinding.medicineName.requestFocus()
            fragBinding.medicineName.error = "Medicine name too long (50 characters)"
            valid = false
        }
        if (fragBinding.medicineUserDir.text!!.length >= 500){
            fragBinding.medicineUserDir.requestFocus()
            fragBinding.medicineUserDir.error = "Usage Directions too long (500 characters)"
            valid = false
        }
        if (fragBinding.medicineQuantity.text.toString().toInt() <=0 ){
            fragBinding.medicineQuantity.requestFocus()
            fragBinding.medicineQuantity.error = "Please enter a quantity greater than 0"
            valid = false
        }
        if (fragBinding.medicineReminderLimit.text.toString().toInt() <=0){
            fragBinding.medicineReminderLimit.requestFocus()
            fragBinding.medicineReminderLimit.error = "Please enter a reminder limit greater than 0"
            valid = false
        }
        return valid
    }

    private fun setUpPicker(layout: FragmentMedicineBinding){
        val types = resources.getStringArray(R.array.type)
        layout.typePicker.displayedValues = types
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}