package ie.wit.medicineapp.ui.settings

import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import ie.wit.medicineapp.R
import java.security.InvalidParameterException

class ThemeProvider(private val context: Context) {

    //Returns the theme from the shared preferences - If it returns null it will follow the systems theme
    fun getThemeFromPreferences(): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val selectedTheme = sharedPreferences.getString(
            context.getString(R.string.theme_preferences_key),
            context.getString(R.string.system_theme_preference_value)
        )

        return selectedTheme?.let {
            getTheme(it)
        } ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    //returns a string based on the the theme selected
    fun getThemeDescriptionForPreference(preferenceValue: String?): String =
        when (preferenceValue) {
            context.getString(R.string.dark_theme_preference_value) -> context.getString(R.string.dark_theme_description)
            context.getString(R.string.light_theme_preference_value) -> context.getString(R.string.light_theme_description)
            else -> context.getString(R.string.system_theme_description)
        }


    //returns int based on theme passed to function
    fun getTheme(selectedTheme: String): Int =
        when (selectedTheme) {
        context.getString(R.string.dark_theme_preference_value) -> UiModeManager.MODE_NIGHT_YES
        context.getString(R.string.light_theme_preference_value) -> UiModeManager.MODE_NIGHT_NO
        context.getString(R.string.system_theme_preference_value) -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        else -> throw InvalidParameterException("Theme not defined for $selectedTheme")
    }
}