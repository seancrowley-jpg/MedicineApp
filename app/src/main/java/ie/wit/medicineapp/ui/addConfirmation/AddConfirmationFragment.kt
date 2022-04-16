package ie.wit.medicineapp.ui.addConfirmation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentAddConfirmationBinding
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.confirmation.ConfirmationViewModel
import ie.wit.medicineapp.ui.group.GroupViewModel
import ie.wit.medicineapp.ui.medicineDetails.MedicineDetailsViewModel
import timber.log.Timber
import java.util.*

class AddConfirmationFragment : Fragment() {

    private val addConfirmationViewModel: AddConfirmationViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val confirmationViewModel: ConfirmationViewModel by activityViewModels()
    private var _fragBinding: FragmentAddConfirmationBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val args by navArgs<AddConfirmationFragmentArgs>()
    var confirmation = ConfirmationModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentAddConfirmationBinding.inflate(inflater, container, false)
        setButtonListener(fragBinding)
        val root = fragBinding.root
        groupViewModel.getGroup(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.groupId)

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

        addConfirmationViewModel.observableConfirmation.observe(
            viewLifecycleOwner,
            Observer { confirmation ->
                confirmation?.let{ render()}
            }
        )
        if (args.edit) {
            addConfirmationViewModel.getConfirmation(
                loggedInViewModel.liveFirebaseUser.value!!.uid,
                args.confirmationId
            )
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Edit Confirmation"
        }
        Timber.i("ARGS:  $args")
        return root
    }

    private fun setButtonListener(layout: FragmentAddConfirmationBinding) {
        layout.quantityInput.transformationMethod = null
        layout.statusGroup.setOnCheckedChangeListener { radioGroup, _ ->
            if(radioGroup.checkedRadioButtonId == R.id.skippedRadio) {
                fragBinding.quantity.visibility = View.GONE
                fragBinding.quantityInputLayout.visibility = View.GONE
                fragBinding.updateMedGroup.visibility = View.GONE
                fragBinding.updateMedHeader.visibility = View.GONE
            }
            else{
                fragBinding.quantity.visibility = View.VISIBLE
                fragBinding.quantityInputLayout.visibility = View.VISIBLE
                fragBinding.updateMedGroup.visibility = View.VISIBLE
                fragBinding.updateMedHeader.visibility = View.VISIBLE
            }
        }
        layout.updateMedGroup.setOnCheckedChangeListener { radioGroup, _ ->
            if(radioGroup.checkedRadioButtonId == R.id.noRadio){
                fragBinding.quantity.visibility = View.GONE
                fragBinding.quantityInputLayout.visibility = View.GONE
            }
            else{
                fragBinding.quantity.visibility = View.VISIBLE
                fragBinding.quantityInputLayout.visibility = View.VISIBLE
            }
        }
        if (args.edit) {
            layout.btnAddConfirmation.text = getString(R.string.btn_edit_confirmation)
            layout.btnAddConfirmation.setOnClickListener() {
                if (validateForm()) {
                    confirmation.uid = args.confirmationId
                    confirmation.time = getTime(fragBinding.timePicker.hour, fragBinding.timePicker.minute)
                    confirmation.day = args.day
                    confirmation.month = args.month
                    confirmation.year = args.year
                    confirmation.medicineID = medicineDetailsViewModel.observableMedicine.value!!.uid!!
                    confirmation.groupID = groupViewModel.observableGroup.value!!.uid!!
                    confirmation.medicineName = medicineDetailsViewModel.observableMedicine.value!!.name
                    confirmation.groupName = groupViewModel.observableGroup.value!!.name
                    confirmation.status = if(layout.statusGroup.checkedRadioButtonId == R.id.takenRadio) "Taken" else "Skipped"
                    addConfirmationViewModel.updateConfirmation(confirmation, loggedInViewModel.liveFirebaseUser.value!!.uid, args.confirmationId)
                    if (layout.statusGroup.checkedRadioButtonId == R.id.takenRadio && layout.updateMedGroup.checkedRadioButtonId == R.id.yesRadio) {
                        confirmationViewModel.confirmMed(
                            loggedInViewModel.liveFirebaseUser.value!!.uid,
                            groupViewModel.observableGroup.value!!.uid!!,
                            medicineDetailsViewModel.observableMedicine.value!!.uid!!,
                            context!!,
                            fragBinding.quantityInput.text.toString().toInt()
                        )
                    }
                    findNavController().popBackStack()
                    Toast.makeText(context, "Confirmation Updated", Toast.LENGTH_SHORT).show()
                }
            }
            layout.btnAddMed.text = getString(R.string.btn_change_medication)
            layout.btnAddMed.setOnClickListener() {
                val action = AddConfirmationFragmentDirections.actionAddConfirmationFragmentToGroupListFragment(
                    confirmation = true, confirmationId = args.confirmationId, edit = true, day = args.day,
                    month = args.month,
                    year = args.year
                )
                findNavController().navigate(action)
            }
        } else {
            layout.btnAddConfirmation.setOnClickListener() {
                if (validateForm()) {
                    confirmation.time =
                        getTime(fragBinding.timePicker.hour, fragBinding.timePicker.minute)
                    confirmation.day = args.day
                    confirmation.month = args.month
                    confirmation.year = args.year
                    confirmation.medicineID =
                        medicineDetailsViewModel.observableMedicine.value!!.uid!!
                    confirmation.groupID = groupViewModel.observableGroup.value!!.uid!!
                    confirmation.medicineName =
                        medicineDetailsViewModel.observableMedicine.value!!.name
                    confirmation.groupName = groupViewModel.observableGroup.value!!.name
                    confirmation.status =
                        if (layout.statusGroup.checkedRadioButtonId == R.id.takenRadio) "Taken" else "Skipped"
                    addConfirmationViewModel.createConfirmation(
                        loggedInViewModel.liveFirebaseUser.value!!.uid,
                        confirmation
                    )
                    if (layout.statusGroup.checkedRadioButtonId == R.id.takenRadio && layout.updateMedGroup.checkedRadioButtonId == R.id.yesRadio) {
                        confirmationViewModel.confirmMed(
                            loggedInViewModel.liveFirebaseUser.value!!.uid,
                            groupViewModel.observableGroup.value!!.uid!!,
                            medicineDetailsViewModel.observableMedicine.value!!.uid!!,
                            context!!,
                            fragBinding.quantityInput.text.toString().toInt()
                        )
                    }
                    findNavController().popBackStack()
                    Toast.makeText(context, "Reminder Created", Toast.LENGTH_SHORT).show()
                }
            }
            layout.btnAddMed.setOnClickListener() {
                val action = AddConfirmationFragmentDirections.actionAddConfirmationFragmentToGroupListFragment(
                    confirmation = true, day = args.day, month = args.month, year = args.year
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun render() {
        fragBinding.confirmationVm = addConfirmationViewModel
        if (args.edit) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = addConfirmationViewModel.observableConfirmation.value!!.time
            fragBinding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            fragBinding.timePicker.minute = calendar.get(Calendar.MINUTE)
            if(addConfirmationViewModel.observableConfirmation.value!!.status == "Taken")
                fragBinding.statusGroup.check(R.id.takenRadio)
            else
                fragBinding.statusGroup.check(R.id.skippedRadio)
        }
    }

    private fun renderMedicine() {
        fragBinding.medicineVM = medicineDetailsViewModel
    }

    private fun getTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, args.day)
            set(Calendar.YEAR, args.year)
            set(Calendar.MINUTE, minute)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.SECOND,0)
        }
        calendar.add(Calendar.MONTH, args.month - 1)
        Timber.i("Day: ${calendar.get(Calendar.DAY_OF_YEAR)}, Month: ${calendar.get(Calendar.MONTH)}, Year: ${calendar.get(Calendar.YEAR)}")
        return calendar.timeInMillis
    }


    private fun validateForm(): Boolean {
        var valid = true
        if(fragBinding.quantityInput.text.toString() == "")  fragBinding.quantityInput.setText("0")
        if (fragBinding.statusGroup.checkedRadioButtonId == R.id.takenRadio && fragBinding.updateMedGroup.checkedRadioButtonId == R.id.yesRadio) {
            if(fragBinding.quantityInput.text.toString() == "")  fragBinding.quantityInput.setText("0")
            if (fragBinding.quantityInput.text.toString().toInt() <= 0) {
                fragBinding.quantityInput.requestFocus()
                fragBinding.quantityInput.error = "Please enter a quantity greater than 0"
                valid = false
            }
        }
        if (fragBinding.medicineId.text.toString()
                .isEmpty() || medicineDetailsViewModel.observableMedicine.value == null
        ) {
            fragBinding.medicineId.requestFocus()
            Toast.makeText(context, "Please select a Medication", Toast.LENGTH_LONG).show()
            valid = false
        }
        return valid
    }
}