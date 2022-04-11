package ie.wit.medicineapp.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.FragmentGroupBinding
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel

class GroupFragment : Fragment() {

    private var _fragBinding: FragmentGroupBinding? = null
    private val fragBinding get() = _fragBinding!!
    var group = GroupModel()
    private val groupViewModel: GroupViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<GroupFragmentArgs>()

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


        groupViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
            status -> status?.let { render(status) }
        })

        if(args.edit){
            groupViewModel.getGroup(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.uid)
            groupViewModel.observableGroup.observe(viewLifecycleOwner, Observer {
                group -> group?.let { render() }
            })
            (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.btn_edit_group)
        }

        fragBinding.priorityLevelGroup.setOnCheckedChangeListener { radioGroup, i ->
            if(radioGroup.checkedRadioButtonId == R.id.highPriorityRadio)
                fragBinding.priorityHelperText.text = getString(R.string.high_priority_helper_text)
            else
                fragBinding.priorityHelperText.text = getString(R.string.low_priority_helper_text)
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

    private fun render() {
        fragBinding.groupvm = groupViewModel
        if (args.edit) {
            if (groupViewModel.observableGroup.value!!.priorityLevel == 0)
                fragBinding.priorityLevelGroup.check(R.id.lowPriority)
            else
                fragBinding.priorityLevelGroup.check(R.id.highPriorityRadio)
        }
    }

    private fun setButtonListener(layout: FragmentGroupBinding){
        if (args.edit) {
            layout.addGroupButton.text = getString(R.string.btn_edit_group)
            layout.addGroupButton.setOnClickListener() {
                if (validateForm()) {
                    group.name = layout.groupName.text.toString()
                    group.uid = args.uid
                    group.priorityLevel = if(layout.priorityLevelGroup.checkedRadioButtonId == R.id.highPriorityRadio) 1 else 0
                    groupViewModel.updateGroup(group,loggedInViewModel.liveFirebaseUser.value?.uid!!,args.uid)
                    val action = GroupFragmentDirections.actionGroupFragmentToGroupListFragment()
                    findNavController().navigate(action)
                }
                else
                    Toast.makeText(context, "Please Enter a Group Name", Toast.LENGTH_LONG).show()
            }
        }
        else{
            layout.addGroupButton.setOnClickListener() {
                if (validateForm()) {
                    group.name = layout.groupName.text.toString()
                    group.priorityLevel = if(layout.priorityLevelGroup.checkedRadioButtonId == R.id.highPriorityRadio) 1 else 0
                    groupViewModel.addGroup(loggedInViewModel.liveFirebaseUser, group)
                    val action = GroupFragmentDirections.actionGroupFragmentToGroupListFragment()
                    findNavController().navigate(action)
                }
                else
                    Toast.makeText(context, "Please Enter a Group Name", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    private fun validateForm(): Boolean {
        var valid = true
        if(fragBinding.groupName.text!!.isEmpty()){
            fragBinding.groupName.requestFocus()
            fragBinding.groupName.error = "Please enter a name for your group"
            valid = false
        }
        if(fragBinding.groupName.text!!.length >= 50){
            fragBinding.groupName.requestFocus()
            fragBinding.groupName.error = "Group name too long (50 characters)"
            valid = false
        }
        return valid
    }
}