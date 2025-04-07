package com.example.eventplanner.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
      fun insertEvent(event: Event)

    @Query("SELECT * FROM events WHERE date = :date")
    fun getEventsByDate(date: String): LiveData<List<Event>>

    @Query("SELECT * FROM events")
     fun getAllEvents(): LiveData<List<Event>>
    @Query("SELECT * FROM events WHERE date = :date")

    fun getEventsForDate(date: String): Flow<List<Event>>

    @Delete
     fun deleteEvent(event: Event)
}
