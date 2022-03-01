package ie.wit.medicineapp.ui.medicineDetails

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import ie.wit.medicineapp.R
import ie.wit.medicineapp.adapters.SideEffectAdapter
import ie.wit.medicineapp.adapters.SideEffectListener
import ie.wit.medicineapp.databinding.FragmentMedicineDetailsBinding
import ie.wit.medicineapp.models.MedicineModel
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MedicineDetailsFragment : Fragment(), SideEffectListener {

    private val medicineDetailsViewModel: MedicineDetailsViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<MedicineDetailsFragmentArgs>()
    private var _fragBinding: FragmentMedicineDetailsBinding? = null
    private val fragBinding get() = _fragBinding!!
    var medicine = MedicineModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentMedicineDetailsBinding.inflate(inflater, container, false)
        val root: View = fragBinding.root
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        medicineDetailsViewModel.getMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.groupId, args.medicineId)
        medicineDetailsViewModel.observableMedicine.observe(viewLifecycleOwner, Observer {
                medicine -> medicine?.let { render() }
        })
        setButtonListener(fragBinding)
        return root
    }

    private fun render() {
        fragBinding.medicineVM = medicineDetailsViewModel
        fragBinding.recyclerView.adapter = SideEffectAdapter(fragBinding.medicineVM?.observableMedicine!!.value!!.sideEffects, this)
    }

    fun setButtonListener(layout: FragmentMedicineDetailsBinding) {
        layout.btnAddSideEffect.setOnClickListener() {
            medicineDetailsViewModel.observableMedicine.value!!.sideEffects.add(layout.sideEffectText.text.toString())
            medicine = medicineDetailsViewModel.observableMedicine.value!!
            medicineDetailsViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.groupId, args.medicineId, medicine)
            render()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_medicine_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveAsPdf) {
            saveAsPdf(medicineDetailsViewModel.observableMedicine.value!!)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSideEffectBtnClick(sideEffect: String?) {
        medicineDetailsViewModel.observableMedicine.value!!.sideEffects.remove(sideEffect)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
        medicine = medicineDetailsViewModel.observableMedicine.value!!
        medicineDetailsViewModel.updateMedicine(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.groupId, args.medicineId, medicine)
    }

    private fun saveAsPdf(medicine: MedicineModel){
        val doc = Document()
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val filePath = getFile(fileName)
        try{
            PdfWriter.getInstance(doc,FileOutputStream(filePath))
            doc.open()
            doc.add(Paragraph(medicine.name))
            doc.close()
            Toast.makeText(context,"PDF Saved to $filePath",Toast.LENGTH_LONG).show()
        }
        catch (e: Exception){
            Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFile(fileName: String): File {
        val documentsDirectory = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return File.createTempFile(fileName, ".pdf", documentsDirectory)
    }

}