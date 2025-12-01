package edu.ucne.farmaciacruz.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.admin.GetEstadisticasUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getEstadisticasUseCase: GetEstadisticasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AdminDashboardEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadStats()
    }

    fun onEvent(event: AdminDashboardEvent) {
        when (event) {
            AdminDashboardEvent.LoadStats -> loadStats()
            is AdminDashboardEvent.TabSelected -> selectTab(event.tab)
            AdminDashboardEvent.Refresh -> refresh()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            getEstadisticasUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                stats = result.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _effect.send(
                            AdminDashboardEffect.ShowError(
                                result.message ?: "Error al cargar estadísticas"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun selectTab(tab: AdminTab) {
        _state.update { it.copy(selectedTab = tab) }

        viewModelScope.launch {
            when (tab) {
                AdminTab.PRODUCTOS -> _effect.send(AdminDashboardEffect.NavigateToProductos)
                AdminTab.USUARIOS -> _effect.send(AdminDashboardEffect.NavigateToUsuarios)
                AdminTab.ORDENES -> _effect.send(AdminDashboardEffect.NavigateToOrdenes)
                AdminTab.DASHBOARD -> {}
            }
        }
    }

    private fun refresh() {
        loadStats()
    }
}