package ie.wit.medicineapp.ui.settings

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.wit.medicineapp.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val numberPreference: EditTextPreference? = findPreference("snooze_limit")

        numberPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        numberPreference?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                "Current Snooze Time: $text minute(s)"
            }

        numberPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == "" || newValue.toString().toInt() == 0 || newValue.toString().toInt() > 120){
                    Toast.makeText(context,"Please Enter a valid time between 1 & 120 minutes", Toast.LENGTH_SHORT).show()
                    false
                }
                else {
                    true
                }
            }
    }
}