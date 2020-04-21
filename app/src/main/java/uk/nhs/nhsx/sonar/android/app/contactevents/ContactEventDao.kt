/*
 * Copyright © 2020 NHSX. All rights reserved.
 */

package uk.nhs.nhsx.sonar.android.app.contactevents

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Seconds
import timber.log.Timber

@Dao
interface ContactEventDao {
    @Insert
    fun insert(contactEvent: ContactEvent)

    @Update
    fun update(contactEvent: ContactEvent)

    @Transaction
    suspend fun createOrUpdate(newEvent: ContactEvent, errorMargin: Int) {
        val sorted = getAll().sortedBy { it.timestamp }
        val eventsById = sorted.filter { it.sonarId.contentEquals(newEvent.sonarId) }
        val matchedEvent = aggregate(newEvent, eventsById, errorMargin)
        if (matchedEvent != null) {
            Timber.d("saving Updated event; $matchedEvent")
            update(matchedEvent)
            return
        }
        // no event within error margin
        Timber.d("saving No matching event; inserting $newEvent")
        insert(newEvent)
    }

    @Query("SELECT * FROM ${ContactEvent.TABLE_NAME}")
    suspend fun getAll(): List<ContactEvent>

    @Query("DELETE FROM ${ContactEvent.TABLE_NAME}")
    suspend fun clearEvents()

    @Query("DELETE FROM ${ContactEvent.TABLE_NAME} WHERE timestamp < :timestamp")
    suspend fun clearOldEvents(timestamp: Long)
}

fun aggregate(newEvent: ContactEvent, events: List<ContactEvent>, errorMargin: Int): ContactEvent? {
    for (event in events) {
        val newEventTime = DateTime(newEvent.timestamp, DateTimeZone.UTC)
        val storedEventTimeStart = DateTime(event.timestamp, DateTimeZone.UTC)
        val storedEventTimeEnd =
            DateTime(event.timestamp, DateTimeZone.UTC).plus(Seconds.seconds(event.duration))

        val rssis = event.rssiValues
        val rssiTimestamps = event.rssiTimestamps

        if (newEventTime.isBefore(storedEventTimeStart) &&
            newEventTime.isAfter(storedEventTimeStart.minus(Seconds.seconds(errorMargin)))
        ) {
            return event.copy(
                timestamp = newEvent.timestamp,
                duration = Seconds.secondsBetween(newEventTime, storedEventTimeEnd).seconds,
                rssiValues = newEvent.rssiValues.plus(rssis),
                rssiTimestamps = newEvent.rssiTimestamps.plus(rssiTimestamps)
            )
        } else if (newEventTime.isAfter(storedEventTimeEnd) &&
            newEventTime.isBefore(storedEventTimeEnd.plus(Seconds.seconds(errorMargin)))
        ) {
            return event.copy(
                duration = Seconds.secondsBetween(storedEventTimeStart, newEventTime).seconds,
                rssiValues = rssis.plus(newEvent.rssiValues),
                rssiTimestamps = rssiTimestamps.plus(newEvent.rssiTimestamps)
            )
        } else if (newEventTime.isAfter(storedEventTimeStart) &&
            newEventTime.isBefore(storedEventTimeEnd)
        ) {
            val rssisAndTimestamps =
                (rssis.zip(rssiTimestamps) + Pair(
                    newEvent.rssiValues.first(),
                    newEvent.rssiTimestamps.first()
                ))
                    .sortedBy { it.second }
            return event.copy(
                rssiValues = rssisAndTimestamps.map { it.first },
                rssiTimestamps = rssisAndTimestamps.map { it.second }
            )
        }
    }

    return null
}
