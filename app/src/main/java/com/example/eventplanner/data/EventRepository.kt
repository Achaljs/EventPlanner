package com.example.eventplanner.data

import androidx.lifecycle.LiveData
import com.example.eventplanner.data.db.Event
import com.example.eventplanner.data.db.EventDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class EventRepository(private val eventDao: EventDao) {

    // Use LiveData for getting events
    fun getEventsByDate(date: String): LiveData<List<Event>> = eventDao.getEventsByDate(date)

    // Use LiveData for getting all events
    fun getAllEvents(): LiveData<List<Event>> = eventDao.getAllEvents()

    // Insert event into database
    fun insertEvent(event: Event) {
        // Perform the insertion operation on a background thread using a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            eventDao.insertEvent(event)
        }
    }
    fun getEventsForDate(date: String): Flow<List<Event>> {
        return eventDao.getEventsForDate(date)
    }
     fun deleteEvent(event: Event) {
         CoroutineScope(Dispatchers.IO).launch {
        eventDao.deleteEvent(event)
         }
    }

    // Delete event from database
//    suspend fun deleteEvent(event: Event) {
//        eventDao.deleteEvent(event)
//    }
}

