package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardGroupBinding
import ie.wit.medicineapp.databinding.CardMedicineBinding
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.MedicineModel

interface MedicineListener{
    fun onMedicineClick(medicine: MedicineModel)
    fun onDeleteMedicineClick(medicine: MedicineModel)
    fun onEditMedicineClick(medicine: MedicineModel)
}

class MedicineAdapter constructor(private var medicine: ArrayList<MedicineModel>, private val listener: MedicineListener, private val reminder: Boolean) :
    RecyclerView.Adapter<MedicineAdapter.MainHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMedicineBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding, reminder)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val medicine = medicine[holder.adapterPosition]
        holder.bind(medicine, listener)
    }

    override fun getItemCount(): Int = medicine.size

    fun removeAt(position: Int) {
        medicine.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder(private val binding : CardMedicineBinding,private val reminder: Boolean) :
        RecyclerView.ViewHolder(binding.root) {
        val reminderRow = reminder
        fun bind(medicine: MedicineModel, listener: MedicineListener) {
            binding.medicine = medicine
            binding.root.tag = medicine
            binding.root.setOnClickListener{listener.onMedicineClick(medicine)}
        }
    }
}