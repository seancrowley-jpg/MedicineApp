package ie.wit.medicineapp.ui.groupList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.medicineapp.adapters.GroupAdapter
import ie.wit.medicineapp.adapters.GroupListener
import ie.wit.medicineapp.databinding.FragmentGroupListBinding
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel

class GroupListFragment : Fragment(), GroupListener {

    private var _fragBinding: FragmentGroupListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val groupListViewModel: GroupListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGroupListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        groupListViewModel.observableRecipesList.observe(viewLifecycleOwner, Observer {
                groups -> groups?.let {
            render(groups as ArrayList<GroupModel>)
                }
        })
        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment()
            findNavController().navigate(action)
        }
        return root
    }

    private fun render(groupList: ArrayList<GroupModel>) {
        fragBinding.recyclerView.adapter = GroupAdapter(groupList, this)
        adapter = fragBinding.recyclerView.adapter as GroupAdapter
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                groupListViewModel.liveFirebaseUser.value = firebaseUser
                groupListViewModel.load()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onGroupClick(group: GroupModel) {
        val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment(edit = true, group.uid!!)
        findNavController().navigate(action)
    }
}