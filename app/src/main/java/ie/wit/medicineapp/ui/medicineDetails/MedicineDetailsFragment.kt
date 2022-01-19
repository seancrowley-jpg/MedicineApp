package ie.wit.medicineapp.ui.medicineDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ie.wit.medicineapp.adapters.SideEffectAdapter
import ie.wit.medicineapp.adapters.SideEffectListener
import ie.wit.medicineapp.databinding.FragmentMedicineDetailsBinding
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
        medicineDetailsViewModel.getMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.groupId, args.medicineId)
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
            medicineDetailsViewModel.observableMedicine.value!!.sideEffects.add(layout.sideEffectText.text.toString())
            medicine = medicineDetailsViewModel.observableMedicine.value!!
            medicineDetailsViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.groupId, args.medicineId, medicine)
            render()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onSideEffectBtnClick(sideEffect: String?) {
        medicineDetailsViewModel.observableMedicine.value!!.sideEffects.remove(sideEffect)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
        medicine = medicineDetailsViewModel.observableMedicine.value!!
        medicineDetailsViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.groupId, args.medicineId, medicine)
    }

}