package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
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
    RecyclerView.Adapter<MedicineAdapter.MainHolder>(), Filterable{

    private val medsFiltered = medicine
    private val searchList = ArrayList<MedicineModel>(medsFiltered)


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

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filteredList = ArrayList<MedicineModel>()
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
                medsFiltered.clear()
                medsFiltered.addAll(p1!!.values as ArrayList<MedicineModel>)
                notifyDataSetChanged()
            }

        }
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