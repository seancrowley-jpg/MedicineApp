package ie.wit.medicineapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import ie.wit.medicineapp.databinding.ActivityLoginBinding
import ie.wit.medicineapp.ui.home.Home
import timber.log.Timber
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ie.wit.medicineapp.R
import ie.wit.medicineapp.ui.settings.ThemeProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var loginRegisterViewModel : LoginRegisterViewModel
    private lateinit var loginBinding : ActivityLoginBinding
    private lateinit var startForResult : ActivityResultLauncher<Intent>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        loginBinding.btnLogin.setOnClickListener {
            signIn(loginBinding.emailField.text.toString(),
                loginBinding.passwordField.text.toString())
        }
        loginBinding.btnSignup.setOnClickListener {
            Timber.i("TEST: ${loginBinding.emailField.text}")
            createAccount(loginBinding.emailField.text.toString(),
                loginBinding.passwordField.text.toString())
        }
        loginBinding.googleSignInButton.setSize(SignInButton.SIZE_WIDE)
        loginBinding.googleSignInButton.setColorScheme(2)
        loginBinding.googleSignInButton.setOnClickListener { googleSignIn() }
        //initialise theme from shared preferences
        val theme = ThemeProvider(this).getThemeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        loginRegisterViewModel = ViewModelProvider(this)[LoginRegisterViewModel::class.java]
        loginRegisterViewModel.liveFirebaseUser.observe(this, Observer
        { firebaseUser -> if (firebaseUser != null)
            startActivity(Intent(this, Home::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            ))
            finish()})

        loginRegisterViewModel.firebaseAuthManager.errorStatus.observe(this, Observer
        { status -> checkStatus(status) })
        setupGoogleSignInCallback()
    }

    //Required to exit app from Login Screen - must investigate this further
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(this,"Click again to Close App...",Toast.LENGTH_LONG).show()
        finish()
    }

    private fun createAccount(email: String, password: String) {
        Timber.d("createAccount:$email")
        if (!validateForm()) { return }

        loginRegisterViewModel.register(email,password)
    }

    private fun signIn(email: String, password: String) {
        Timber.d("signIn:$email")
        if (!validateForm()) { return }

        loginRegisterViewModel.login(email,password)
    }

    private fun checkStatus(error:Boolean) {
        if (error)
            Toast.makeText(this,
                getString(R.string.auth_failed),
                Toast.LENGTH_LONG).show()
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = loginBinding.emailField.text.toString()
        if (TextUtils.isEmpty(email)) {
            loginBinding.emailField.error = "Required."
            valid = false
        } else {
            loginBinding.emailField.error = null
        }

        val password = loginBinding.passwordField.text.toString()
        if (TextUtils.isEmpty(password)) {
            loginBinding.passwordField.error = "Required."
            valid = false
        } else {
            loginBinding.passwordField.error = null
        }
        return valid
    }

    private fun googleSignIn() {
        val signInIntent = loginRegisterViewModel.firebaseAuthManager
            .googleSignInClient.value!!.signInIntent

        startForResult.launch(signInIntent)
    }

    private fun setupGoogleSignInCallback() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)
                            loginRegisterViewModel.authWithGoogle(account!!)
                        } catch (e: ApiException) {
                            // Google Sign In failed
                            Timber.i( "Google sign in failed $e")
                            Snackbar.make(loginBinding.appBarLayout, "Authentication Failed.",
                                Snackbar.LENGTH_SHORT).show()
                        }
                        Timber.i("RecipesApp Google Result $result.data")
                    }
                    RESULT_CANCELED -> {

                    } else -> { }
                }
            }
    }
}