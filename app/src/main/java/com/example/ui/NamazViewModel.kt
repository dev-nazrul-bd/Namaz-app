package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NamazDatabase
import com.example.data.NamazRepository
import com.example.data.PrayerCompletion
import com.example.data.UserSettings
import com.example.firebase.FirebaseManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NamazViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NamazRepository
    
    // Core observe flows
    val settings: StateFlow<UserSettings>
    
    private val _currentDate = MutableStateFlow(getCurrentDateString())
    val currentDate: StateFlow<String> = _currentDate
    
    val currentCompletion: StateFlow<PrayerCompletion>

    init {
        val database = NamazDatabase.getDatabase(application)
        repository = NamazRepository(database.namazDao())

        // Retrieve settings, default if not defined
        settings = repository.settingsFlow
            .map { it ?: UserSettings() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserSettings()
            )

        // Query completions for current date reactively
        currentCompletion = _currentDate
            .flatMapLatest { date ->
                repository.getPrayerCompletionFlow(date).map { it ?: PrayerCompletion(date = date) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PrayerCompletion(date = getCurrentDateString())
            )

        // Initialize Firebase connection and trigger active parental listener
        viewModelScope.launch {
            FirebaseManager.initialize(application)
            FirebaseManager.startParentalControlListener(application)
        }
    }

    /**
     * Persist user preferences in DB
     */
    fun saveSettings(updatedSettings: UserSettings) {
        viewModelScope.launch {
            repository.saveSettings(updatedSettings)
        }
    }

    /**
     * Persist daily completions in DB
     */
    fun saveCompletion(updatedCompletion: PrayerCompletion) {
        viewModelScope.launch {
            repository.savePrayerCompletion(updatedCompletion)
        }
    }

    /**
     * Refreshes currentDate daily
     */
    fun refreshDate() {
        _currentDate.value = getCurrentDateString()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }
}
