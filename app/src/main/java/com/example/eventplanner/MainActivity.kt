package com.example.eventplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventplanner.data.EventRepository
import com.example.eventplanner.data.db.AppDatabase
import com.example.eventplanner.data.db.Event
import com.example.eventplanner.databinding.ActivityMainBinding
import com.example.eventplanner.databinding.DialogAddEventBinding
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.viewmodel.EventViewModelFactory
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: EventViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eventAdapter = EventAdapter(emptyList()) { eventToDelete ->
            viewModel.deleteEvent(eventToDelete)
        }

        binding.eventRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = eventAdapter
        }



        setupViewModel()
        setupCalendar()
        observeEvents()

        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun setupViewModel() {
        val dao = AppDatabase.getDatabase(this).eventDao()
        val repo = EventRepository(dao)
        val factory = EventViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { month: CalendarMonth ->
            val title = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
            binding.tvMonthYear.text = title
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: android.view.View): DayViewContainer {
                return DayViewContainer(view)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()

                // Disable dates outside of the current month
                if (day.position != com.kizitonwose.calendar.core.DayPosition.MonthDate) {
                    container.textView.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                    container.view.setOnClickListener(null)
                } else {
                    container.textView.setTextColor(resources.getColor(android.R.color.black, theme))

                    // Apply a circle background for today’s date
                    if (day.date == LocalDate.now()) {
                        container.textView.setBackgroundResource(R.drawable.today_circle) // Ensure this drawable exists
                    } else {
                        container.textView.background = null
                    }

                    // Handle click for selecting the date
                    container.view.setOnClickListener {
                        selectedDate = day.date
                        viewModel.loadEventsForDate(day.date.toString()) // Load events for selected date
                    }
                }

                // Highlight dates with events by checking the event list
                viewModel.events.observe(this@MainActivity) { events ->
                    Log.d("Day Date", "Day Date: ${day.date.toString()}")
                  //  Log.d("Event Date", "Event Date: ${events.get(1).date}")

                   // val formattedDayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.date)

                    if (events.isNotEmpty()) {
                        if (events.any { it.date == day.date.toString() }) {
                            container.textView.setBackgroundResource(R.drawable.selected_day_bg) // Ensure this drawable exists
                        } else {

                        }
                    } else {
                        // Handle the case where there are no events
                        Log.d("Event", "No events available")
                    }


                    container.textView.setOnClickListener {
                        // Open the RecyclerView with events for that day
                        val selectedDate = day.date
                        showEventsForDate(selectedDate)
                    }
                }

                }




            }
        }


    private fun showEventsForDate(date: LocalDate) {
        val formattedDate = "${date.year}-${String.format("%02d", date.monthValue)}-${String.format("%02d", date.dayOfMonth)}"

        viewModel.getEventsForDate(formattedDate).observe(this) { filteredEvents ->


            eventAdapter.updateEvents(filteredEvents)

            binding.tvNoEvents.visibility = if (filteredEvents.isEmpty()) View.VISIBLE else View.GONE
        }
    }


    private fun observeEvents() {
        viewModel.events.observe(this) { events ->
            val filtered = events.filter { it.date == selectedDate.toString() }
            binding.tvEventList.text = if (filtered.isEmpty()) {
                "Events On This Day"
            } else {

                filtered.joinToString("\n\n") { "${it.title} - ${it.time}\n${it.description}" }
            }
            binding.calendarView.notifyCalendarChanged() // Refresh calendar to update date highlights
        }
    }

    private fun showAddEventDialog() {
        val dialogBinding = DialogAddEventBinding.inflate(LayoutInflater.from(this))

        // Set up the DatePicker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format selected date and set it in the event
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            dialogBinding.etDate.setText(formattedDate)
        }, year, month, day)

        // Set up the TimePicker
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Format selected time and set it in the event
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            dialogBinding.etTime.setText(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        AlertDialog.Builder(this)
            .setTitle("Add Event")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val time = dialogBinding.etTime.text.toString()
                val desc = dialogBinding.etDescription.text.toString()
                val date = dialogBinding.etDate.text.toString()
              //  val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

                if (title.isNotBlank() && time.isNotBlank() && date.isNotBlank()) {
                    val event = Event(
                        title = title,
                        time = time,
                        description = desc,
                        date = date // Storing date as String (e.g., YYYY-MM-DD)
                    )
                    viewModel.addEvent(event) // Add event to database
                } else {
                    Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

        // Open DatePickerDialog when clicking on Date field
        dialogBinding.etDate.setOnClickListener {
            datePickerDialog.show()
        }

        // Open TimePickerDialog when clicking on Time field
        dialogBinding.etTime.setOnClickListener {
            timePickerDialog.show()
        }
    }

    inner class DayViewContainer(view: android.view.View) : ViewContainer(view) {
        val textView = view.findViewById<android.widget.TextView>(R.id.textViewDay)
    }
}
