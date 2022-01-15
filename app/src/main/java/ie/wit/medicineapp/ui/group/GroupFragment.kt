package ie.wit.medicineapp.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentGroupBinding
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel

class GroupFragment : Fragment() {

    private var _fragBinding: FragmentGroupBinding? = null
    private val fragBinding get() = _fragBinding!!
    var group = GroupModel()
    private lateinit var groupViewModel: GroupViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGroupBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        groupViewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        groupViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
            status -> status?.let { render(status) }
        })

        fragBinding.priorityLevelPicker.minValue = 0
        fragBinding.priorityLevelPicker.maxValue= 2
        fragBinding.priorityLevelPicker.value = 0

        fragBinding.priorityLevelPicker.setOnValueChangedListener{ _, _, newVal ->
            fragBinding.groupPriorityText.text = "$newVal"
        }

        setButtonListener(fragBinding)
        return root
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //findNavController().popBackStack()
                }
            }
            false -> Toast.makeText(context,getString(R.string.group_error), Toast.LENGTH_LONG).show()
        }
    }

    fun setButtonListener(layout: FragmentGroupBinding){
        layout.addGroupButton.setOnClickListener(){
            group.name = layout.groupName.text.toString()
            if(layout.groupPriorityText.text.isNotEmpty()) {
                group.priorityLevel = layout.groupPriorityText.text.toString().toInt()
            }
            groupViewModel.addGroup(loggedInViewModel.liveFirebaseUser, group)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}