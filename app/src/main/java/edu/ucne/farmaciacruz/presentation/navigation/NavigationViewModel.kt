package edu.ucne.farmaciacruz.presentation.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel()