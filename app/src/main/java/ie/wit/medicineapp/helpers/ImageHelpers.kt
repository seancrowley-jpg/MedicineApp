package ie.wit.medicineapp.helpers

import android.graphics.Color
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Transformation

fun customTransformation() : Transformation =
    RoundedTransformationBuilder()
        .borderColor(Color.WHITE)
        .borderWidthDp(2F)
        .cornerRadiusDp(35F)
        .oval(false)
        .build()