package ie.wit.medicineapp.helpers

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import ie.wit.medicineapp.models.MedicineModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


fun saveAsPdf(medicine: MedicineModel, context:Context,userName:String){
    val doc = Document()
    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val filePath = getFile(fileName, context)
    try{
        PdfWriter.getInstance(doc, FileOutputStream(filePath))
        doc.open()
        doc.addTitle("Medication")
        val title = Chunk("Medication for $userName")
        doc.add(Paragraph(title))
        doc.add(Paragraph(" "))
        doc.add(Paragraph("Name: " +medicine.name))
        doc.add(Paragraph("Dosage: " +medicine.dosage))
        doc.add(Paragraph("Usage Directions: " +medicine.usageDir))
        doc.add(Paragraph("Remaining Quantity: " +medicine.quantity+ " " + medicine.unit))
        doc.close()
        Toast.makeText(context,"PDF Saved to $filePath", Toast.LENGTH_LONG).show()
    }
    catch (e: Exception){
        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
    }
}

fun saveListAsPdf(medicineList: ArrayList<MedicineModel>, context:Context, userName:String){
    val doc = Document()
    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val filePath = getFile(fileName, context)
    try{
        PdfWriter.getInstance(doc, FileOutputStream(filePath))
        doc.open()
        doc.addTitle("Medication")
        val title = Chunk("Medication for $userName")
        doc.add(Paragraph(title))
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0, 0, 0, 68)
        for (medicine in medicineList){
            doc.add(Paragraph(" "))
            doc.add(Paragraph("Name: " +medicine.name))
            doc.add(Paragraph("Dosage: " +medicine.dosage))
            doc.add(Paragraph("Usage Directions: " +medicine.usageDir))
            doc.add(Paragraph("Remaining Quantity: " +medicine.quantity+ " " + medicine.unit))
            doc.add(Paragraph(" "))
            doc.add(lineSeparator)
        }
        doc.close()
        Toast.makeText(context,"PDF Saved to $filePath", Toast.LENGTH_LONG).show()
    }
    catch (e: Exception){
        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
    }
}


fun getFile(fileName: String, context:Context): File {
    val documentsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return File.createTempFile(fileName, ".pdf", documentsDirectory)
}

fun createPdf(medicine: MedicineModel, context:Context,userName:String) : File{
    val doc = Document()
    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val filePath = File.createTempFile(fileName, ".pdf")
    try{
        PdfWriter.getInstance(doc, FileOutputStream(filePath))
        doc.open()
        doc.addTitle("Medication")
        val title = Chunk("Medication for $userName")
        doc.add(Paragraph(title))
        doc.add(Paragraph(" "))
        doc.add(Paragraph("Name: " +medicine.name))
        doc.add(Paragraph("Dosage: " +medicine.dosage))
        doc.add(Paragraph("Usage Directions: " +medicine.usageDir))
        doc.add(Paragraph("Remaining Quantity: " +medicine.quantity+ " " + medicine.unit))
        doc.close()

    }
    catch (e: Exception){
        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
    }
    return filePath
}

fun createListPdf(medicineList: ArrayList<MedicineModel>, context:Context, userName:String): File{
    val doc = Document()
    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val filePath = File.createTempFile(fileName, ".pdf")

    try{
        PdfWriter.getInstance(doc, FileOutputStream(filePath))
        doc.open()
        doc.addTitle("Medication")
        val title = Chunk("Medication for $userName")
        doc.add(Paragraph(title))
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0, 0, 0, 68)
        for (medicine in medicineList){
            doc.add(Paragraph(" "))
            doc.add(Paragraph("Name: " +medicine.name))
            doc.add(Paragraph("Dosage: " +medicine.dosage))
            doc.add(Paragraph("Usage Directions: " +medicine.usageDir))
            doc.add(Paragraph("Remaining Quantity: " +medicine.quantity + " " + medicine.unit))
            doc.add(Paragraph(" "))
            doc.add(lineSeparator)
        }
        doc.close()
    }
    catch (e: Exception){
        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
    }
    return filePath
}