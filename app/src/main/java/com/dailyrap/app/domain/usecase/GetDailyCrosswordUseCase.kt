package com.dailyrap.app.domain.usecase

import com.dailyrap.app.data.model.CrosswordPuzzle
import com.dailyrap.app.data.repository.CrosswordRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetDailyCrosswordUseCase @Inject constructor(
    private val crosswordRepository: CrosswordRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): CrosswordPuzzle? {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return crosswordRepository.getCrosswordByDate(dateString)
            ?: crosswordRepository.getAllCrosswords().firstOrNull()
    }
}
