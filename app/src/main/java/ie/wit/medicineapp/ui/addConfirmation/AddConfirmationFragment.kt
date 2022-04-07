package ie.wit.medicineapp.ui.addConfirmation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import ie.wit.medicineapp.databinding.FragmentAddConfirmationBinding
import ie.wit.medicineapp.ui.auth.LoggedInViewModel

class AddConfirmationFragment : Fragment() {

    private val addConfirmationViewModel: AddConfirmationViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentAddConfirmationBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentAddConfirmationBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        return root
    }

}