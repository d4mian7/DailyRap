package com.dailyrap.app.domain.usecase

import com.dailyrap.app.data.model.Puzzle
import com.dailyrap.app.data.repository.PuzzleRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetDailyPuzzleUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Puzzle? {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return puzzleRepository.getPuzzleByDate(dateString)
            ?: puzzleRepository.getAllPuzzles().firstOrNull()
    }
}
