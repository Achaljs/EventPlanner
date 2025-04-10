package com.example.eventplanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.EventRepository
import com.example.eventplanner.data.db.Event
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    val events: LiveData<List<Event>> = repository.getAllEvents() // Directly assign LiveData from repository

    fun loadEventsForDate(date: String): LiveData<List<Event>> {
        return repository.getEventsByDate(date)
    }

    fun getEventsForDate(date: String): LiveData<List<Event>> {
        return repository.getEventsForDate(date).asLiveData()
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
     fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }
}
