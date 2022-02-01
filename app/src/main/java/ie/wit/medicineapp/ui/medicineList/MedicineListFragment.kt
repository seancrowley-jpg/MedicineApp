package ie.wit.medicineapp.ui.medicineList

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.medicineapp.adapters.MedicineAdapter
import ie.wit.medicineapp.adapters.MedicineListener
import ie.wit.medicineapp.databinding.FragmentMedicineListBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.groupList.GroupListFragmentDirections
import ie.wit.medicineapp.ui.utils.MedSwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.MedSwipeToEditCallback
import ie.wit.medicineapp.ui.utils.SwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.SwipeToEditCallback

class MedicineListFragment : Fragment(), MedicineListener {

    private var _fragBinding: FragmentMedicineListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val medicineListViewModel: MedicineListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<MedicineListFragmentArgs>()
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
        loader = createLoader(requireActivity())
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        showLoader(loader,"Loading Medication")
        medicineListViewModel.observableMedicationList.observe(viewLifecycleOwner, Observer {
                medication -> medication?.let {
            render(medication as ArrayList<MedicineModel>)
            hideLoader(loader)
            checkSwipeRefresh()
        }
        })
        setSwipeRefresh()
        if(args.reminder) {
            fragBinding.fab.visibility = View.GONE
        }
        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action =
                MedicineListFragmentDirections.actionMedicineListFragmentToMedicineFragment(args.groupId)
            findNavController().navigate(action)
        }
        val swipeEditHandler = object : MedSwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditMedicineClick(viewHolder.itemView.tag as MedicineModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
        val swipeDeleteHandler = object : MedSwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeAt(viewHolder.adapterPosition)
                medicineListViewModel.deleteMedicine(viewHolder.itemView.tag as MedicineModel, args.groupId)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)
        return root
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"Loading...")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                medicineListViewModel.liveFirebaseUser.value = firebaseUser
                medicineListViewModel.load(args.groupId)
            }
        })
    }

    private fun render(medicineList: ArrayList<MedicineModel>) {
        fragBinding.recyclerView.adapter = MedicineAdapter(medicineList, this, args.reminder)
        adapter = fragBinding.recyclerView.adapter as MedicineAdapter
        if (medicineList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.medicationNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.medicationNotFound.visibility = View.GONE
        }
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Loading..")
            medicineListViewModel.load(args.groupId)
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onMedicineClick(medicine: MedicineModel) {
        if (args.reminder) {
            val actionReminder = MedicineListFragmentDirections.actionMedicineListFragmentToReminderFragment(medicineId = medicine.uid!!, groupId = args.groupId, reminderId = args.reminderId, edit = args.edit)
            findNavController().navigate(actionReminder)
        }
        else {
            val action = MedicineListFragmentDirections.actionMedicineListFragmentToMedicineDetails(
                medicineId = medicine.uid!!, groupId = args.groupId
            )
            findNavController().navigate(action)
        }
    }

    override fun onDeleteMedicineClick(medicine: MedicineModel) {
        medicineListViewModel.deleteMedicine(medicine, args.groupId)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onEditMedicineClick(medicine: MedicineModel) {
        val action = MedicineListFragmentDirections.actionMedicineListFragmentToMedicineFragment(edit = true, medicineId = medicine.uid!!, groupId = args.groupId
        )
        findNavController().navigate(action)
    }

}