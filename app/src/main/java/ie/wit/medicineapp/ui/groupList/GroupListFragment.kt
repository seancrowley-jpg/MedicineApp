package ie.wit.medicineapp.ui.groupList

import android.app.AlertDialog
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.medicineapp.adapters.GroupAdapter
import ie.wit.medicineapp.adapters.GroupListener
import ie.wit.medicineapp.databinding.FragmentGroupListBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.SwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.SwipeToEditCallback

class GroupListFragment : Fragment(), GroupListener {

    private var _fragBinding: FragmentGroupListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val groupListViewModel: GroupListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private lateinit var adapter: GroupAdapter
    lateinit var loader : AlertDialog
    private val args by navArgs<GroupListFragmentArgs>()

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
        loader = createLoader(requireActivity())
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        showLoader(loader,"Loading Groups")
        groupListViewModel.observableGroupsList.observe(viewLifecycleOwner, Observer {
                groups -> groups?.let {
            render(groups as ArrayList<GroupModel>)
            hideLoader(loader)
            checkSwipeRefresh()
                }
        })
        setSwipeRefresh()
        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment()
            findNavController().navigate(action)
        }
        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeAt(viewHolder.adapterPosition)
                groupListViewModel.deleteGroup(viewHolder.itemView.tag as GroupModel)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditGroupClick(viewHolder.itemView.tag as GroupModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
        return root
    }

    private fun render(groupList: ArrayList<GroupModel>) {
        fragBinding.recyclerView.adapter = GroupAdapter(groupList, this)
        adapter = fragBinding.recyclerView.adapter as GroupAdapter
        if (groupList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.groupsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.groupsNotFound.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"Loading...")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                groupListViewModel.liveFirebaseUser.value = firebaseUser
                groupListViewModel.load()
            }
        })
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Loading..")
            groupListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onGroupClick(group: GroupModel) {
        if (args.reminder){
            val action = GroupListFragmentDirections.actionGroupListFragmentToMedicineListFragment(group.uid!!, reminder = true)
            findNavController().navigate(action)
        }
        else {
            val action = GroupListFragmentDirections.actionGroupListFragmentToMedicineListFragment(group.uid!!)
            findNavController().navigate(action)
        }
    }

    override fun onDeleteGroupClick(group: GroupModel) {
        groupListViewModel.deleteGroup(group)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()

    }

    override fun onEditGroupClick(group: GroupModel) {
        val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment(edit = true, group.uid!!)
        findNavController().navigate(action)
    }
}