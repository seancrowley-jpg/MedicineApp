package ie.wit.medicineapp.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.wit.medicineapp.R
import ie.wit.medicineapp.ui.auth.LoggedInViewModel
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {
    //lazy values only initialised when accessed for the first time
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()


    private val numberPreference by lazy {
        findPreference<EditTextPreference>(getString(R.string.snooze_preference_key))
    }
    private val themeProvider by lazy { ThemeProvider(requireContext()) }
    private val themePreference by lazy {
        findPreference<ListPreference>(getString(R.string.theme_preferences_key))
    }
    private val notificationSettingsPreference by lazy {
        findPreference<Preference>(getString(R.string.notif_preferences_key))
    }
    private val pharmacyPreference by lazy {
        findPreference<EditTextPreference>(getString(R.string.pharmacy_preference_key))
    }
    private val deleteAccountSetting by lazy {
        findPreference<Preference>("delete_account_setting")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setSnoozeValue()
        setThemePreference()
        openAppNotificationSettings()
        setPharmacyNumber()
        deleteAccount()
    }

    //snooze option setup
    private fun setSnoozeValue() {
        numberPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        numberPreference?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                "Current snooze time: $text minute(s)"
            }

        numberPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == "" || newValue.toString().toInt() == 0 || newValue.toString()
                        .toInt() > 120
                ) {
                    Toast.makeText(
                        context,
                        "Please enter a valid time between 1 & 120 minutes",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    true
                }
            }
    }

    //theme option setup
    private fun setThemePreference() {
        themePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue is String) {
                    val theme = themeProvider.getTheme(newValue)
                    AppCompatDelegate.setDefaultNightMode(theme)
                }
                true
            }
        themePreference?.summaryProvider = getThemeSummaryProvider()
    }

    private fun getThemeSummaryProvider() =
        Preference.SummaryProvider<ListPreference> { preference ->
            themeProvider.getThemeDescriptionForPreference(preference.value)
        }

    private fun openAppNotificationSettings() {
        notificationSettingsPreference?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context!!.packageName)
                }
                context!!.startActivity(intent)
                true
            }
    }

    private fun setPharmacyNumber() {
        pharmacyPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        pharmacyPreference?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                "Your pharmacies phone number: $text"
            }
    }

    private fun deleteAccount() {
        deleteAccountSetting!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                AlertDialog.Builder(context!!)
                    .setIcon(R.drawable.ic_baseline_delete_forever)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? \nAll data will be lost")
                    .setPositiveButton("Confirm") { _, _ ->
                        settingsViewModel.deleteAccount(loggedInViewModel.liveFirebaseUser.value!!.uid)
                        loggedInViewModel.deleteAccount()
                        Timber.i("Confirm")}
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
                true
            }
    }
}
