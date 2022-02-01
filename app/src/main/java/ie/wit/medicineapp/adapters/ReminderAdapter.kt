package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardReminderBinding
import ie.wit.medicineapp.models.ReminderModel

interface ReminderListener{
    fun onReminderDeleteClick(reminder: ReminderModel)
    fun onReminderClick(reminder: ReminderModel)
}

class ReminderAdapter constructor(private var reminders: ArrayList<ReminderModel>, private val listener: ReminderListener) :
    RecyclerView.Adapter<ReminderAdapter.MainHolder>(){


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

    class MainHolder(private val binding : CardReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: ReminderModel, listener: ReminderListener) {
            binding.reminder = reminder
            binding.root.tag = reminder
            binding.btnDeleteReminder.setOnClickListener{ listener.onReminderDeleteClick(reminder)}
            binding.root.setOnClickListener{ listener.onReminderClick(reminder)}
            binding.executePendingBindings()
        }
    }
}