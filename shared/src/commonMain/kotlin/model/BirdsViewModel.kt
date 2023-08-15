package model

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirdsUIState(val images:List<BirdImage> = emptyList(),val selectedCategory:String ? = null){
    val categories = images.map {
        it.category
    }.toSet()
    val selectedImages = images.filter {
        it.category == selectedCategory
    }
}




class BirdsViewModel:ViewModel() {
    private val _uiState = MutableStateFlow<BirdsUIState>(BirdsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        updateImages()
    }

    fun selectedCategory(category: String){
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        val images =
            httpClient.get("https://sebi.io/demo-image-api/pictures.json").body<List<BirdImage>>()
        return images
    }

    fun updateImages(){
        viewModelScope.launch {
            val images = getImages()
            _uiState.update {
                it.copy(images = images)
            }
        }
    }

    override fun onCleared() {
        httpClient.close()
        super.onCleared()
    }
}