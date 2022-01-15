package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardGroupBinding
import ie.wit.medicineapp.models.GroupModel

interface GroupListener{
    fun onGroupClick(group: GroupModel)
}

class GroupAdapter constructor(private var groups: ArrayList<GroupModel>, private val listener: GroupListener) :
    RecyclerView.Adapter<GroupAdapter.MainHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardGroupBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val group = groups[holder.adapterPosition]
        holder.bind(group, listener)
    }

    override fun getItemCount(): Int = groups.size

    class MainHolder(private val binding : CardGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: GroupModel, listener: GroupListener) {
            binding.groupName.text = group.name
            binding.priorityLevel.text = group.priorityLevel.toString()
            binding.root.setOnClickListener{ listener.onGroupClick(group)}
            binding.executePendingBindings()
        }
    }
}