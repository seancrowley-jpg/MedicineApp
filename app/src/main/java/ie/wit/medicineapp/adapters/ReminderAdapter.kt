package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardReminderBinding
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.models.ReminderModel

interface ReminderListener{
    fun onReminderClick(reminder: ReminderModel)
    fun onReminderToggleBtnOff(reminder: ReminderModel)
    fun onReminderToggleBtnOn(reminder: ReminderModel)
}

class ReminderAdapter constructor(private var reminders: ArrayList<ReminderModel>, private val listener: ReminderListener) :
    RecyclerView.Adapter<ReminderAdapter.MainHolder>(), Filterable{

    private val remindersFiltered = reminders
    private val searchList = ArrayList<ReminderModel>(remindersFiltered)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardReminderBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val reminder = reminders[holder.adapterPosition]
        holder.bind(reminder, listener)
    }

    override fun getItemCount(): Int = reminders.size

    fun removeAt(position: Int) {
        reminders.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filteredList = ArrayList<ReminderModel>()
                val searchText = p0.toString()
                if(searchText.isBlank() or searchText.isEmpty()){
                    filteredList.addAll(searchList)
                }
                else{
                    searchList.forEach{
                        if(it.medName.lowercase().contains(searchText) or it.groupName.lowercase().contains(searchText)){
                            filteredList.add(it)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                remindersFiltered.clear()
                remindersFiltered.addAll(p1!!.values as ArrayList<ReminderModel>)
                notifyDataSetChanged()
            }

        }
    }

    class MainHolder(private val binding : CardReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val toggleButton = binding.toggleButton.toggleButton
        fun bind(reminder: ReminderModel, listener: ReminderListener) {
            binding.reminder = reminder
            binding.root.tag = reminder
            binding.root.setOnClickListener{ listener.onReminderClick(reminder)}
            toggleButton.isChecked = reminder.active
            toggleButton.setOnCheckedChangeListener {_, isChecked ->
                if(isChecked) listener.onReminderToggleBtnOn(reminder)
                else listener.onReminderToggleBtnOff(reminder)
            }
            binding.executePendingBindings()
        }
    }
}