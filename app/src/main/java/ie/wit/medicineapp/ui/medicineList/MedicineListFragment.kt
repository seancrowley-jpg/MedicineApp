package ie.wit.medicineapp.ui.medicineList

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.GroupAdapter
import ie.wit.medicineapp.adapters.MedicineAdapter
import ie.wit.medicineapp.adapters.MedicineListener
import ie.wit.medicineapp.databinding.FragmentMedicineListBinding
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.group.GroupFragmentArgs
import ie.wit.medicineapp.ui.groupList.GroupListFragmentDirections

class MedicineListFragment : Fragment(), MedicineListener {

    private var _fragBinding: FragmentMedicineListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val medicineListViewModel: MedicineListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<GroupFragmentArgs>()
    lateinit var loader : AlertDialog
    private lateinit var adapter: MedicineAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentMedicineListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action = MedicineListFragmentDirections.actionMedicineListFragmentToMedicineFragment(args.uid)
            findNavController().navigate(action)
        }
        return root
    }

    private fun render(medicineList: ArrayList<MedicineModel>) {
        fragBinding.recyclerView.adapter = MedicineAdapter(medicineList, this)
        adapter = fragBinding.recyclerView.adapter as MedicineAdapter
        if (medicineList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.medicationNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.medicationNotFound.visibility = View.GONE
        }
    }

    override fun onMedicineClick(medicine: MedicineModel) {
    }

    override fun onDeleteMedicineClick(medicine: MedicineModel) {
    }

    override fun onEditMedicineClick(medicine: MedicineModel) {
    }

}