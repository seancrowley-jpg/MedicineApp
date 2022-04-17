package ie.wit.medicineapp.ui.medicineDetails

import PrintAdapter
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.SideEffectAdapter
import ie.wit.medicineapp.adapters.SideEffectListener
import ie.wit.medicineapp.databinding.FragmentMedicineDetailsBinding
import ie.wit.medicineapp.helpers.createPdf
import ie.wit.medicineapp.helpers.saveAsPdf
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import timber.log.Timber

class MedicineDetailsFragment : Fragment(), SideEffectListener {

    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<MedicineDetailsFragmentArgs>()
    private var _fragBinding: FragmentMedicineDetailsBinding? = null
    private val fragBinding get() = _fragBinding!!
    var medicine = MedicineModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentMedicineDetailsBinding.inflate(inflater, container, false)
        val root: View = fragBinding.root
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        if(args.userId == "0") {
            medicineDetailsViewModel.getMedicine(
                loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.groupId,
                args.medicineId
            )
        }
        else
            medicineDetailsViewModel.getMedicine(
                args.userId,
                args.groupId,
                args.medicineId
            )
        medicineDetailsViewModel.observableMedicine.observe(viewLifecycleOwner, Observer {
                medicine -> medicine?.let { render() }
        })
        setButtonListener(fragBinding)
        return root
    }

    private fun render() {
        fragBinding.medicineVM = medicineDetailsViewModel
        fragBinding.recyclerView.adapter = SideEffectAdapter(fragBinding.medicineVM?.observableMedicine!!.value!!.sideEffects, this)
    }

    fun setButtonListener(layout: FragmentMedicineDetailsBinding) {
        layout.btnAddSideEffect.setOnClickListener() {
            if (validateForm()) {
                medicineDetailsViewModel.observableMedicine.value!!.sideEffects.add(layout.sideEffectText.text.toString())
                medicine = medicineDetailsViewModel.observableMedicine.value!!
                medicineDetailsViewModel.updateMedicine(
                    loggedInViewModel.liveFirebaseUser.value?.uid!!,
                    args.groupId, args.medicineId, medicine
                )
                render()
            }
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        if(fragBinding.sideEffectText.text.toString().length >=50){
            fragBinding.sideEffectText.requestFocus()
            fragBinding.sideEffectText.error = "Side Effect character limit reached (50 characters)"
            valid = false
        }
        if(fragBinding.sideEffectText.text!!.isEmpty()){
            fragBinding.sideEffectText.requestFocus()
            fragBinding.sideEffectText.error = "Side effect field empty"
            valid = false
        }
        return valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_medicine_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveAsPdf) {
            if (loggedInViewModel.liveFirebaseUser.value!!.displayName != null) {
                saveAsPdf(
                    medicineDetailsViewModel.observableMedicine.value!!, context!!,
                    loggedInViewModel.liveFirebaseUser.value!!.displayName!!
                )
            } else {
                saveAsPdf(
                    medicineDetailsViewModel.observableMedicine.value!!, context!!,
                    loggedInViewModel.liveFirebaseUser.value!!.email!!
                )
            }
        }
        if (item.itemId == R.id.print) {
            val printManager: PrintManager =
                requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
            if (loggedInViewModel.liveFirebaseUser.value!!.displayName != null) {
                try {
                    val file = createPdf(
                        medicineDetailsViewModel.observableMedicine.value!!,
                        context!!,
                        loggedInViewModel.liveFirebaseUser.value!!.displayName!!
                    )
                    val printAdapter = PrintAdapter(file.absolutePath)
                    printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
            else{
                try {
                    val file = createPdf(
                        medicineDetailsViewModel.observableMedicine.value!!,
                        context!!,
                        loggedInViewModel.liveFirebaseUser.value!!.email!!
                    )
                    val printAdapter = PrintAdapter(file.absolutePath)
                    printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSideEffectBtnClick(sideEffect: String?) {
        medicineDetailsViewModel.observableMedicine.value!!.sideEffects.remove(sideEffect)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
        medicine = medicineDetailsViewModel.observableMedicine.value!!
        medicineDetailsViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.groupId, args.medicineId, medicine)
    }


}