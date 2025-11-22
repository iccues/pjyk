package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.Season;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SeasonService {
    public LocalDateRange getStartDateRange(Integer year, Season season) {
        if (year == null) {
            return null;
        }

        if (season == null) {
            LocalDate start = LocalDate.of(year, 1, 1).minusMonths(1);
            LocalDate end = start.plusYears(1);
            return new LocalDateRange(start, end);
        }

        LocalDate start = LocalDate.of(year, season.toMonth(), 1).minusMonths(1);
        LocalDate end = start.plusMonths(3);
        return new LocalDateRange(start, end);
    }
}
