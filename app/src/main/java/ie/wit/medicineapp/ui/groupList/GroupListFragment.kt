package ie.wit.medicineapp.ui.groupList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.GroupAdapter
import ie.wit.medicineapp.adapters.GroupListener
import ie.wit.medicineapp.databinding.FragmentGroupListBinding
import ie.wit.medicineapp.helpers.createLoader
import ie.wit.medicineapp.helpers.hideLoader
import ie.wit.medicineapp.helpers.showLoader
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.utils.GroupSwipeToDeleteCallback
import ie.wit.medicineapp.ui.utils.GroupSwipeToEditCallback

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
        if(args.reminder || args.confirmation) {
            fragBinding.fab.visibility = View.GONE
        }

        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment()
            findNavController().navigate(action)
        }

        val swipeDeleteHandler = object : GroupSwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
                val confirmBool = sharedPreferences.getBoolean("confirm_delete", true)
                adapter.removeAt(viewHolder.adapterPosition)
                if (confirmBool) {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Delete Group?")
                    alertDialog.setMessage("Are you sure you want to delete this Group? " +
                            "\nAll medication in this group will also be deleted.")
                    alertDialog.setNegativeButton("No") { _, _ ->
                        groupListViewModel.load()
                    }
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        createDeleteSnackBar(fragBinding, viewHolder)
                    }
                    //alertDialog.setOnDismissListener {groupListViewModel.load()}
                    alertDialog.show()
                }
                else{
                    //groupListViewModel.deleteGroup(viewHolder.itemView.tag as GroupModel)
                    createDeleteSnackBar(fragBinding, viewHolder)
                    hideLoader(loader)
                }
            }
        }

        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : GroupSwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditGroupClick(viewHolder.itemView.tag as GroupModel)
            }
        }

        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_group_list, menu)
        val searchItem = menu.findItem(R.id.item_search_groups)
        val searchView = searchItem?.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                adapter.filter.filter(p0)
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.delete_all_groups) {
            if (groupListViewModel.observableGroupsList.value!!.isNotEmpty()) {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setTitle("Delete Groups?")
                alertDialog.setMessage(
                    "Are you sure you want to delete all Groups? " +
                            "\nAll medication will also be deleted."
                )
                alertDialog.setNegativeButton("No") { _, _ ->
                    groupListViewModel.load()
                }
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    createDeleteAllSnackBar(fragBinding)
                }
                //alertDialog.setOnDismissListener { groupListViewModel.load() }
                alertDialog.show()
            }
            else{
                Toast.makeText(context, "No Groups To Delete Found", Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun render(groupList: ArrayList<GroupModel>) {
        fragBinding.recyclerView.adapter = GroupAdapter(groupList, this, args.reminder, args.confirmation)
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
        if (args.reminder) {
            val action = GroupListFragmentDirections.actionGroupListFragmentToMedicineListFragment(
                group.uid!!,
                reminder = true,
                reminderId = args.reminderId,
                edit = args.edit
            )
            findNavController().navigate(action)
        }
        if (args.confirmation) {
            val action = GroupListFragmentDirections.actionGroupListFragmentToMedicineListFragment(
                group.uid!!,
                confirmation = true,
                confiramtionId = args.confirmationId,
                edit = args.edit,
                day = args.day,
                month = args.month,
                year = args.year
            )
            findNavController().navigate(action)
        } else {
            val action =
                GroupListFragmentDirections.actionGroupListFragmentToMedicineListFragment(group.uid!!)
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

    private fun createDeleteAllSnackBar(layout: FragmentGroupListBinding) {
        layout.recyclerView.visibility = View.GONE
        layout.swiperefresh.isEnabled = false
        Snackbar.make(
            layout.recyclerView,
            "All Groups Deleted",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Undo", View.OnClickListener {
                Toast.makeText(context, "Delete Undone", Toast.LENGTH_LONG).show()
            })
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        groupListViewModel.load()
                        layout.swiperefresh.isEnabled = true
                        layout.recyclerView.visibility = View.VISIBLE
                    } else {
                        groupListViewModel.deleteAllGroups()
                        groupListViewModel.load()
                        layout.swiperefresh.isEnabled = true
                        layout.recyclerView.visibility = View.VISIBLE
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    private fun createDeleteSnackBar(
        layout: FragmentGroupListBinding,
        viewHolder: RecyclerView.ViewHolder
    ) {
        layout.swiperefresh.isEnabled = false
        Snackbar.make(
            layout.recyclerView,
            "Group Deleted",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Undo", View.OnClickListener {
                groupListViewModel.load()
                Toast.makeText(context, "Delete Undone", Toast.LENGTH_LONG).show()
            })
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        //groupListViewModel.load()
                        layout.swiperefresh.isEnabled = true
                    } else {
                        groupListViewModel.deleteGroup(viewHolder.itemView.tag as GroupModel)
                        //groupListViewModel.load()
                        layout.swiperefresh.isEnabled = true
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }
}