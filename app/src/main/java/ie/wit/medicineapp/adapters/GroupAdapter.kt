package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardGroupBinding
import ie.wit.medicineapp.models.GroupModel

interface GroupListener{
    fun onGroupClick(group: GroupModel)
    fun onDeleteGroupClick(group: GroupModel)
    fun onEditGroupClick(group: GroupModel)
}

class GroupAdapter constructor(private var groups: ArrayList<GroupModel>, private val listener: GroupListener, private val reminder: Boolean) :
    RecyclerView.Adapter<GroupAdapter.MainHolder>(), Filterable{

    private val groupsFiltered = groups
    private val searchList = ArrayList<GroupModel>(groupsFiltered)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardGroupBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding, reminder)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val group = groups[holder.adapterPosition]
        holder.bind(group, listener)
    }

    override fun getItemCount(): Int = groups.size

    fun removeAt(position: Int) {
        groups.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filteredList = ArrayList<GroupModel>()
                val searchText = p0.toString()
                if(searchText.isBlank() or searchText.isEmpty()){
                    filteredList.addAll(searchList)
                }
                else{
                    searchList.forEach{
                        if(it.name.lowercase().contains(searchText)){
                            filteredList.add(it)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                groupsFiltered.clear()
                groupsFiltered.addAll(p1!!.values as ArrayList<GroupModel>)
                notifyDataSetChanged()
            }

        }
    }

    class MainHolder(private val binding : CardGroupBinding, private val reminder: Boolean) :
        RecyclerView.ViewHolder(binding.root) {
        val reminderRow = reminder
        fun bind(group: GroupModel, listener: GroupListener) {
            binding.group = group
            binding.root.tag = group
            binding.root.setOnClickListener{ listener.onGroupClick(group)}
            binding.executePendingBindings()
        }
    }
}