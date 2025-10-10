package pe.edu.upc.gigumobile.gigs.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.gigs.data.repository.GigRepository
import pe.edu.upc.gigumobile.gigs.domain.model.Gig

data class GigsListState(
    val isLoading: Boolean = false,
    val data: List<Gig> = emptyList(),
    val message: String = ""
)

data class GigDetailState(
    val isLoading: Boolean = false,
    val data: Gig? = null,
    val message: String = ""
)

class GigViewModel(
    private val repository: GigRepository
) : ViewModel() {

    val listState = mutableStateOf(GigsListState())
    val detailState = mutableStateOf(GigDetailState())

    fun loadGigs(
        page: Int = 1,
        pageSize: Int = 10,
        searchTerm: String? = null
    ) {
        viewModelScope.launch {
            listState.value = listState.value.copy(isLoading = true, message = "")
            when (val res = repository.getAllGigs(
                page = page,
                pageSize = pageSize,
                searchTerm = searchTerm,
                sortBy = "createdAt",
                descending = true
            )) {
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
                        message = res.message ?: "No se pudo obtener Gigs."
                    )
                }

                is Resource.Loading<*> -> TODO()
            }
        }
    }

    fun loadGigDetail(id: String) {
        viewModelScope.launch {
            detailState.value = detailState.value.copy(isLoading = true, message = "", data = null)
            when (val res = repository.getGigById(id)) {
                is Resource.Success -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        data = res.data
                    )
                }
                is Resource.Error -> {
                    detailState.value = detailState.value.copy(
                        isLoading = false,
                        message = res.message ?: "No se pudo obtener el detalle."
                    )
                }

                is Resource.Loading<*> -> TODO()
            }
        }
    }
}
