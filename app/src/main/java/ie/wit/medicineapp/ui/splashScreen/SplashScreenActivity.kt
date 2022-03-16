package ie.wit.medicineapp.ui.splashScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ie.wit.medicineapp.R
import ie.wit.medicineapp.ui.auth.LoginActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)


        Handler(Looper.getMainLooper()).postDelayed({
             Firebase.database.setPersistenceEnabled(true)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}