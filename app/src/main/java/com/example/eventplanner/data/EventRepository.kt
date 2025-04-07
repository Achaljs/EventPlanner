package com.example.eventplanner.data

import androidx.lifecycle.LiveData
import com.example.eventplanner.data.db.Event
import com.example.eventplanner.data.db.EventDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class EventRepository(private val eventDao: EventDao) {

    fun getEventsByDate(date: String): LiveData<List<Event>> = eventDao.getEventsByDate(date)

    fun getAllEvents(): LiveData<List<Event>> = eventDao.getAllEvents()


    fun insertEvent(event: Event) {
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


}

