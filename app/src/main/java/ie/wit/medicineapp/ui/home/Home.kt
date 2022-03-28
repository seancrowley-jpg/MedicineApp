package ie.wit.medicineapp.ui.home

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import ie.wit.medicineapp.R
import ie.wit.medicineapp.databinding.HomeBinding
import ie.wit.medicineapp.databinding.NavHeaderBinding
import ie.wit.medicineapp.helpers.customTransformation
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import ie.wit.medicineapp.ui.auth.LoginActivity
import ie.wit.medicineapp.ui.settings.ThemeProvider
import ie.wit.medicineapp.ui.utils.NotificationService
import timber.log.Timber

class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private lateinit var navHeaderBinding : NavHeaderBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loggedInViewModel : LoggedInViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeBinding = HomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        drawerLayout = homeBinding.drawerLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = homeBinding.drawerLayout
        val navView: NavigationView = homeBinding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.groupListFragment, R.id.groupFragment, R.id.schedulerFragment,
                R.id.historyFragment, R.id.settingsFragment
            ), drawerLayout
        )
        createNotificationChannel()
        createHighPriorityNotificationChannel()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun signOut(item : MenuItem) {
        loggedInViewModel.logOut()
        //Launch Login activity and clear the back stack to stop navigating back to the Home activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    public override fun onStart() {
        super.onStart()
        loggedInViewModel = ViewModelProvider(this).get(LoggedInViewModel::class.java)
        loggedInViewModel.liveFirebaseUser.observe(this, Observer { firebaseUser ->
            if (firebaseUser != null)
                updateNavHeader(loggedInViewModel.liveFirebaseUser.value!!)
        })

        loggedInViewModel.loggedOut.observe(this, Observer { loggedout ->
            if (loggedout) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        })

    }

    private fun updateNavHeader(currentUser: FirebaseUser) {
        var headerView = homeBinding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)
        navHeaderBinding.navHeaderEmail.text = currentUser.email
        navHeaderBinding.navHeaderName.text = currentUser.displayName
        if(currentUser.photoUrl != null && currentUser.displayName != null) {
            navHeaderBinding.navHeaderName.text = currentUser.displayName
            Picasso.get().load(currentUser.photoUrl)
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .into(navHeaderBinding.navHeaderImage)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun createHighPriorityNotificationChannel() {
        val name = "High Priority Reminder Channel"
        val description = "Channel for Reminder High Priority Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NotificationService.highChannelId, name, importance)
        channel.description = description
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationChannel() {
        val name = "Default Reminder Channel"
        val description = "Channel for Reminder Default Notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NotificationService.channelID, name, importance)
        channel.description = description
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBackPressed() {
        ///Checks if back stack number is 0 then asks if user wants to quit app
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        Timber.i("Back Stack Num: ${navHostFragment!!.childFragmentManager.backStackEntryCount}")
        if (navHostFragment.childFragmentManager.backStackEntryCount == 0) {
            AlertDialog.Builder(this)
                .setTitle("Quit?")
                .setMessage("Quit Application?")
                .setPositiveButton("Exit") { _, _ ->
                    finish()
                    super.onBackPressed()}
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }
        else
            super.onBackPressed()
    }
}