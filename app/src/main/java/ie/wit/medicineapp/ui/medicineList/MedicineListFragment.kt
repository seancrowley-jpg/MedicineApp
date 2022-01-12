package ie.wit.medicineapp.ui.medicineList

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ie.wit.medicineapp.R

class MedicineListFragment : Fragment() {

    companion object {
        fun newInstance() = MedicineListFragment()
    }

    private lateinit var viewModel: MedicineListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medicine_list, container, false)
    }

}