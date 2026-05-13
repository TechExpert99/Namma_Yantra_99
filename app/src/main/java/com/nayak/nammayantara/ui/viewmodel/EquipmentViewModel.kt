package com.nayak.nammayantara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.data.repository.EquipmentRepository
import kotlinx.coroutines.launch

class EquipmentViewModel : ViewModel() {

    private val repository = EquipmentRepository()

    var equipmentList by mutableStateOf<List<Equipment>>(emptyList())
    var isLoading by mutableStateOf(false)
    var selectedType by mutableStateOf("All")
    var sampleDataAdded by mutableStateOf(false)

    val types = listOf("All", "Tractor", "Harvester", "Sprayer")

    val filteredList get() = if (selectedType == "All") equipmentList
    else equipmentList.filter { it.type == selectedType }

    init { loadEquipment() }

    fun loadEquipment() {
        viewModelScope.launch {
            isLoading = true
            equipmentList = repository.getAllEquipment()
            isLoading = false
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            repository.addSampleData()
            sampleDataAdded = true
            loadEquipment()
        }
    }
}