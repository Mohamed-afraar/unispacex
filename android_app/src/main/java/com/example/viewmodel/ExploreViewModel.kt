package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BusinessRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ServiceRepository
import com.example.data.repository.UserRepository
import com.example.model.Business
import com.example.model.Product
import com.example.model.Service
import com.example.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val searchQuery: String = "",
    val activeCategory: String = "All",
    val activeCollege: String = "All",
    val activeSubTab: String = "All",
    val sortBy: String = "Recommended",
    val verifiedOnly: Boolean = false,
    val inStockOnly: Boolean = false,
    val priceRange: String = "All",
    val businesses: List<Business> = emptyList(),
    val students: List<Student> = emptyList(),
    val products: List<Product> = emptyList(),
    val services: List<Service> = emptyList(),
    val savedItems: Set<String> = emptySet(),
    val followedBusinesses: Set<String> = emptySet()
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeAll().collect { prods ->
                _uiState.update { it.copy(products = prods) }
            }
        }
        viewModelScope.launch {
            serviceRepository.observeAll().collect { servs ->
                _uiState.update { it.copy(services = servs) }
            }
        }
        viewModelScope.launch {
            businessRepository.observeAll().collect { bizs ->
                _uiState.update { it.copy(businesses = bizs) }
            }
        }
        viewModelScope.launch {
            userRepository.observeAllProfiles().collect { studs ->
                _uiState.update { it.copy(students = studs) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun setCollegeFilter(college: String) {
        _uiState.update { it.copy(activeCollege = college) }
    }

    fun setSortBy(sort: String) {
        _uiState.update { it.copy(sortBy = sort) }
    }

    fun toggleVerifiedOnly() {
        _uiState.update { it.copy(verifiedOnly = !it.verifiedOnly) }
    }

    fun toggleSaveItem(id: String) {
        _uiState.update { state ->
            val set = state.savedItems.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            state.copy(savedItems = set)
        }
    }

    fun toggleFollowBusiness(businessId: String) {
        _uiState.update { state ->
            val set = state.followedBusinesses.toMutableSet()
            if (set.contains(businessId)) set.remove(businessId) else set.add(businessId)
            state.copy(followedBusinesses = set)
        }
    }
}
