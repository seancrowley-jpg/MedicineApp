package ie.wit.medicineapp.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.databinding.FragmentMedicineBinding
import ie.wit.medicineapp.ui.group.GroupFragmentArgs

class MedicineFragment : Fragment() {

    private val medicineViewModel: MedicineViewModel by activityViewModels()
    private var _fragBinding: FragmentMedicineBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val args by navArgs<GroupFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentMedicineBinding.inflate(inflater, container, false)
        val root: View = fragBinding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}