package pe.edu.upc.gigumobile.pulls.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.pulls.data.repository.PullRepository
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

data class PullsListState(
    val isLoading: Boolean = false,
    val data: List<Pull> = emptyList(),
    val message: String = ""
)

data class PullDetailState(
    val isLoading: Boolean = false,
    val data: Pull? = null,
    val message: String = ""
)

data class CreatePullState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val createdPull: Pull? = null,
    val message: String = ""
)

class PullViewModel(
    private val repository: PullRepository
) : ViewModel() {

    val listState = mutableStateOf(PullsListState())
    val detailState = mutableStateOf(PullDetailState())
    val createState = mutableStateOf(CreatePullState())

    /**
     * Cargar todos los pulls
     */
    fun loadPulls() {
        viewModelScope.launch {
            listState.value = listState.value.copy(isLoading = true, message = "")
            when (val res = repository.getAllPulls()) {
                is Resource.Success -> {
                    listState.value = listState.value.copy(
                        isLoading = false,
                        data = res.data ?: emptyList(),
                        message = ""
                    )
                }
                is Resource.Error -> {
                    listState.value = listState.value.copy(
                        isLoading = false,
                        message = res.message ?: "No se pudieron obtener los pulls."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Cargar pulls por buyerId
     */
    fun loadPullsByBuyerId(buyerId: Int) {
        viewModelScope.launch {
            listState.value = listState.value.copy(isLoading = true, message = "")
            when (val res = repository.getPullsByBuyerId(buyerId)) {
                is Resource.Success -> {
                    listState.value = listState.value.copy(
                        isLoading = false,
                        data = res.data ?: emptyList(),
                        message = ""
                    )
                }
                is Resource.Error -> {
                    listState.value = listState.value.copy(
                        isLoading = false,
                        message = res.message ?: "No se pudieron obtener tus pulls."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Cargar detalle de un pull
     */
    fun loadPullDetail(id: Int) {
        viewModelScope.launch {
            detailState.value = detailState.value.copy(isLoading = true, message = "", data = null)
            when (val res = repository.getPullById(id)) {
                is Resource.Success -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        data = res.data
                    )
                }
                is Resource.Error -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        message = res.message ?: "No se pudo obtener el detalle del pull."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Crear un nuevo pull
     */
    fun createPull(
        sellerId: Int,
        gigId: Int,
        priceInit: Double,
        priceUpdate: Double,
        buyerId: Int,
        state: PullState = PullState.PENDING
    ) {
        viewModelScope.launch {
            createState.value = createState.value.copy(isLoading = true, success = false, message = "")
            when (val res = repository.createPull(sellerId, gigId, priceInit, priceUpdate, buyerId, state)) {
                is Resource.Success -> {
                    createState.value = createState.value.copy(
                        isLoading = false,
                        success = true,
                        createdPull = res.data,
                        message = "Pull creado exitosamente"
                    )
                    // Recargar la lista
                    loadPullsByBuyerId(buyerId)
                }
                is Resource.Error -> {
                    createState.value = createState.value.copy(
                        isLoading = false,
                        success = false,
                        message = res.message ?: "No se pudo crear el pull."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Actualizar un pull
     */
    fun updatePull(
        id: Int,
        newPrice: Double,
        newState: PullState,
        buyerId: Int
    ) {
        viewModelScope.launch {
            detailState.value = detailState.value.copy(isLoading = true, message = "")
            when (val res = repository.updatePull(id, newPrice, newState)) {
                is Resource.Success -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        data = res.data,
                        message = "Pull actualizado"
                    )
                    // Recargar la lista
                    loadPullsByBuyerId(buyerId)
                }
                is Resource.Error -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        message = res.message ?: "No se pudo actualizar el pull."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Cerrar un pull
     */
    fun closePull(id: Int, buyerId: Int) {
        viewModelScope.launch {
            when (val res = repository.closePull(id)) {
                is Resource.Success -> {
                    // Recargar la lista después de cerrar
                    loadPullsByBuyerId(buyerId)
                }
                is Resource.Error -> {
                    listState.value = listState.value.copy(
                        message = res.message ?: "No se pudo cerrar el pull."
                    )
                }
                is Resource.Loading<*> -> { }
            }
        }
    }

    /**
     * Reset create state
     */
    fun resetCreateState() {
        createState.value = CreatePullState()
    }

    /**
     * Obtener el userId del usuario en sesión
     */
    suspend fun getCurrentUserId(): Int {
        return repository.getCurrentUserId()
    }
}

