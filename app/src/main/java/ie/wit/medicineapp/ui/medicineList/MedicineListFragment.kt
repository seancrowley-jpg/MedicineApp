package ie.wit.medicineapp.ui.medicineList

import PrintAdapter
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.MedicineAdapter
import ie.wit.medicineapp.adapters.MedicineListener
import ie.wit.medicineapp.databinding.FragmentMedicineListBinding
import ie.wit.medicineapp.helpers.*
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.MedSwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.MedSwipeToEditCallback
import timber.log.Timber

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
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
                val confirmBool = sharedPreferences.getBoolean("confirm_delete", true)
                if (confirmBool) {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Delete Medicine?")
                    alertDialog.setMessage("Are you sure you want to delete this Medication?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        adapter.removeAt(viewHolder.adapterPosition)
                        medicineListViewModel.deleteMedicine(
                            viewHolder.itemView.tag as MedicineModel,
                            args.groupId
                        )
                        hideLoader(loader)
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                        medicineListViewModel.load(args.groupId)
                    }
                    alertDialog.setOnDismissListener {medicineListViewModel.load(args.groupId)}
                    alertDialog.show()
                }
                else{
                    adapter.removeAt(viewHolder.adapterPosition)
                    medicineListViewModel.deleteMedicine(
                        viewHolder.itemView.tag as MedicineModel,
                        args.groupId
                    )
                    hideLoader(loader)
                }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_medicine_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveAsPdf) {
            if (loggedInViewModel.liveFirebaseUser.value!!.displayName != null) {
                saveListAsPdf(
                    medicineListViewModel.observableMedicationList.value!! as ArrayList<MedicineModel>,
                    context!!, loggedInViewModel.liveFirebaseUser.value!!.displayName!!
                )
            }
            else{
                saveListAsPdf(
                    medicineListViewModel.observableMedicationList.value!! as ArrayList<MedicineModel>,
                    context!!, loggedInViewModel.liveFirebaseUser.value!!.email!!
                )
            }
        }
        if (item.itemId == R.id.print) {
            val printManager: PrintManager =
                requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
            try {
                val file = createListPdf(
                    medicineListViewModel.observableMedicationList.value!! as ArrayList<MedicineModel>,
                    context!!,
                    "userName"
                )
                val printAdapter = PrintAdapter(file.absolutePath)
                printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return super.onOptionsItemSelected(item)
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
            val actionReminder = MedicineListFragmentDirections.actionMedicineListFragmentToReminderFragment(
                medicineId = medicine.uid!!, groupId = args.groupId,
                reminderId = args.reminderId, edit = args.edit)
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