package ie.wit.medicineapp.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ie.wit.medicineapp.adapters.HistoryAdapter
import ie.wit.medicineapp.databinding.FragmentHistoryBinding
import ie.wit.medicineapp.databinding.FragmentMedicineListBinding
import ie.wit.medicineapp.models.ConfirmationModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : Fragment() {


    private val historyViewModel : HistoryViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentHistoryBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var adapter: HistoryAdapter
    private val calendar: Calendar = Calendar.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        historyViewModel.observableHistoryList.observe(viewLifecycleOwner, Observer {
            historyList -> historyList?.let{
                render(historyList as ArrayList<ConfirmationModel>)
        }
        })
        fragBinding.calendarView.setOnDateChangeListener { _, year, month, day ->
            historyViewModel.load(loggedInViewModel.liveFirebaseUser.value!!.uid,day, month+1, year)
        }
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

    override fun onResume() {
        super.onResume()
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



}