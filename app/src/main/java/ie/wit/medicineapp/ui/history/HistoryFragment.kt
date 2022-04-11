package ie.wit.medicineapp.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.HistoryAdapter
import ie.wit.medicineapp.adapters.HistoryListener
import ie.wit.medicineapp.databinding.FragmentHistoryBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.HistorySwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.HistorySwipeToEditCallback
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : Fragment(), HistoryListener {


    private val historyViewModel : HistoryViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentHistoryBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var adapter: HistoryAdapter
    private val calendar: Calendar = Calendar.getInstance()
    lateinit var loader : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        loader = createLoader(requireActivity())
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        showLoader(loader,"Loading History")
        historyViewModel.observableHistoryList.observe(viewLifecycleOwner, Observer {
            historyList -> historyList?.let{
            render(historyList as ArrayList<ConfirmationModel>)
            hideLoader(loader)
            checkSwipeRefresh()
        }
        })
        setSwipeRefresh()
        setButtonListener(fragBinding)
        swipeToDeleteCallback()
        swipeToEditCallback()
        fragBinding.calendarView.maxDate = System.currentTimeMillis()
        return root
    }

    private fun render(historyList: ArrayList<ConfirmationModel>) {
        fragBinding.recyclerView.adapter = HistoryAdapter(historyList, this)
        adapter = fragBinding.recyclerView.adapter as HistoryAdapter
        if (historyList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.historyNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.historyNotFound.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_all_confirmation) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Delete All History?")
            alertDialog.setMessage("Are you sure you want to delete all your confirmation history?")
            alertDialog.setNegativeButton("No") { _, _ ->
                historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                    calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.YEAR))
            }
            alertDialog.setPositiveButton("Yes") { _, _ ->
                historyViewModel.deleteAllConfirmations(loggedInViewModel.liveFirebaseUser.value!!.uid)
                historyViewModel.load(
                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR)
                )
                hideLoader(loader)
            }
            alertDialog.setOnDismissListener {
                historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                    calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.YEAR))}
            alertDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setButtonListener(layout: FragmentHistoryBinding) {
        layout.btnAddConfirmation.setOnClickListener {
            val action = HistoryFragmentDirections.actionHistoryFragmentToAddConfirmationFragment(
                day = calendar.get(Calendar.DAY_OF_MONTH), month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR))
            findNavController().navigate(action)
        }
        layout.calendarView.setOnDateChangeListener { _, year, month, day ->
            historyViewModel.load(
                loggedInViewModel.liveFirebaseUser.value!!.uid,
                day,
                month + 1,
                year
            )
            calendar.set(year, month, day)
        }
    }

    private fun swipeToDeleteCallback(){
        val swipeDeleteHandler = object : HistorySwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
                val confirmBool = sharedPreferences.getBoolean("confirm_delete", true)
                if (confirmBool) {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Delete?")
                    alertDialog.setMessage("Are you sure you want to delete this Confirmation from your history?")
                    alertDialog.setNegativeButton("No") { _, _ ->
                        historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                            calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                            calendar.get(Calendar.YEAR))
                    }
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        adapter.removeAt(viewHolder.adapterPosition)
                        historyViewModel.deleteConfirmation(viewHolder.itemView.tag as ConfirmationModel)
                        hideLoader(loader)
                    }
                    alertDialog.setOnDismissListener {
                        historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                            calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                            calendar.get(Calendar.YEAR))}
                    alertDialog.show()
                }
                else{
                    adapter.removeAt(viewHolder.adapterPosition)
                    historyViewModel.deleteConfirmation(viewHolder.itemView.tag as ConfirmationModel)
                    hideLoader(loader)
                }
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)
    }

    private fun swipeToEditCallback(){
        val swipeEditHandler = object : HistorySwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditHistoryClick(viewHolder.itemView.tag as ConfirmationModel)
            }
        }

        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"Loading...")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                historyViewModel.liveFirebaseUser.value = firebaseUser
                calendar.timeInMillis = System.currentTimeMillis()
                historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                    calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.YEAR))
            }
        })
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Loading..")
            historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,
                calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.YEAR))
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onEditHistoryClick(confirmation: ConfirmationModel) {
        val action = HistoryFragmentDirections.actionHistoryFragmentToAddConfirmationFragment(
            day = confirmation.day,
            month = confirmation.month,
            year = confirmation.year,
            confirmationId = confirmation.uid,
            edit = true,
            groupId = confirmation.groupID,
            medicineId = confirmation.medicineID
        )
        findNavController().navigate(action)
    }


}