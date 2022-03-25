package ie.wit.medicineapp.main

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("MedicineApp started")
        Firebase.database.setPersistenceEnabled(true)
    }
}