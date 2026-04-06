package com.dailyrap.app.domain.usecase

import com.dailyrap.app.data.model.DailyWord
import com.dailyrap.app.data.repository.WordRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetDailyWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): DailyWord? {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return wordRepository.getWordByDate(dateString)
            ?: wordRepository.getAllWords().firstOrNull()
    }
}
