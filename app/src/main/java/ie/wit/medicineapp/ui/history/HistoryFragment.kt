package ie.wit.medicineapp.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.HistoryAdapter
import ie.wit.medicineapp.databinding.FragmentHistoryBinding
import ie.wit.medicineapp.databinding.FragmentMedicineListBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.GroupSwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.SwipeToDeleteCallback
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : Fragment() {


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
        fragBinding.calendarView.setOnDateChangeListener { _, year, month, day ->
            historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,day, month+1, year)
            calendar.set(year,month,day)
        }
        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
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
        return root
    }

    private fun render(historyList: ArrayList<ConfirmationModel>) {
        fragBinding.recyclerView.adapter = HistoryAdapter(historyList)
        adapter = fragBinding.recyclerView.adapter as HistoryAdapter
        if (historyList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
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



}