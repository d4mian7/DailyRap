package com.dailyrap.app.domain.usecase

import com.dailyrap.app.data.model.ArtistPuzzle
import com.dailyrap.app.data.repository.GuessArtistRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetDailyArtistUseCase @Inject constructor(
    private val repository: GuessArtistRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): ArtistPuzzle? {
        val all = repository.load().artists
        if (all.isEmpty()) return null
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        // Dni bez zestawu: zestawy po kolei, żeby test nie zależał od kalendarza
        return all.find { it.date == dateString }
            ?: all[Math.floorMod(date.toEpochDay(), all.size.toLong()).toInt()]
    }
}
