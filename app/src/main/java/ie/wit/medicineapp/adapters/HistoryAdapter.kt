package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardHistoryBinding
import ie.wit.medicineapp.models.ConfirmationModel

class HistoryAdapter constructor(private var history: ArrayList<ConfirmationModel>) :
    RecyclerView.Adapter<HistoryAdapter.MainHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardHistoryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val confirmation = history[holder.adapterPosition]
        holder.bind(confirmation)
    }

    override fun getItemCount(): Int = history.size

    fun removeAt(position: Int) {
        history.removeAt(position)
        notifyItemRemoved(position)
    }


    class MainHolder(private val binding : CardHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(confirmation: ConfirmationModel) {
            binding.confirmation = confirmation
            binding.root.tag = confirmation
            binding.executePendingBindings()
        }
    }
}