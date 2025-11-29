package com.iccues.metaanimebackend.service.fetch;

import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.exception.FetchFailedException;
import com.iccues.metaanimebackend.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FetchServiceTest {

    @Mock
    private BangumiFetchService bangumiFetchService;

    @Mock
    private AniListFetchService aniListFetchService;

    @Mock
    private MyAnimeListFetchService myAnimeListFetchService;

    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private FetchService fetchService;

    @Test
    public void testGetFetchService_Bangumi() {
        AbstractAnimeFetchService result = fetchService.getFetchService(Platform.Bangumi);

        assertNotNull(result);
        assertEquals(bangumiFetchService, result);
    }

    @Test
    public void testGetFetchService_AniList() {
        AbstractAnimeFetchService result = fetchService.getFetchService(Platform.AniList);

        assertNotNull(result);
        assertEquals(aniListFetchService, result);
    }

    @Test
    public void testGetFetchService_MyAnimeList() {
        AbstractAnimeFetchService result = fetchService.getFetchService(Platform.MyAnimeList);

        assertNotNull(result);
        assertEquals(myAnimeListFetchService, result);
    }

    @Test
    public void testFetchMapping_CallsAllServices() {
        int year = 2024;
        Season season = Season.WINTER;

        fetchService.fetchMapping(year, season);

        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testLinkMappings_CallsAllServices() {
        fetchService.linkMappings();

        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();
    }

    @Test
    public void testCalculateAllAverageScore_CallsScoreService() {
        fetchService.calculateAllAverageScore();

        verify(scoreService, times(1)).calculateAllAverageScore();
    }

    @Test
    public void testFetchAnime_CallsAllSteps() {
        int year = 2024;
        Season season = Season.SPRING;

        fetchService.fetchAnime(year, season);

        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);

        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();

        verify(scoreService, times(1)).calculateAllAverageScore();
    }


    @Test
    public void testFetchMapping_BangumiFailsButOthersContinue() {
        int year = 2024;
        Season season = Season.SPRING;

        // Mock Bangumi to throw exception
        doThrow(new FetchFailedException(Platform.Bangumi, "year=2024, season=SPRING"))
                .when(bangumiFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception
        fetchService.fetchMapping(year, season);

        // Verify all services were called
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testFetchMapping_AniListFailsButOthersContinue() {
        int year = 2024;
        Season season = Season.SUMMER;

        // Mock AniList to throw exception
        doThrow(new FetchFailedException(Platform.AniList, "year=2024, season=SUMMER"))
                .when(aniListFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception
        fetchService.fetchMapping(year, season);

        // Verify all services were called
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testFetchMapping_MyAnimeListFailsButOthersContinue() {
        int year = 2024;
        Season season = Season.FALL;

        // Mock MyAnimeList to throw exception
        doThrow(new FetchFailedException(Platform.MyAnimeList, "year=2024, season=FALL"))
                .when(myAnimeListFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception
        fetchService.fetchMapping(year, season);

        // Verify all services were called
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testFetchMapping_MultiplePlatformsFailButContinue() {
        int year = 2024;
        Season season = Season.WINTER;

        // Mock Bangumi and AniList to throw exceptions
        doThrow(new FetchFailedException(Platform.Bangumi, "year=2024, season=WINTER"))
                .when(bangumiFetchService).fetchAndSaveMappings(year, season);
        doThrow(new RuntimeException("AniList network error"))
                .when(aniListFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception
        fetchService.fetchMapping(year, season);

        // Verify all services were called despite failures
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testFetchMapping_AllPlatformsFail() {
        int year = 2024;
        Season season = Season.SPRING;

        // Mock all services to throw exceptions
        doThrow(new FetchFailedException(Platform.Bangumi, "year=2024, season=SPRING"))
                .when(bangumiFetchService).fetchAndSaveMappings(year, season);
        doThrow(new FetchFailedException(Platform.AniList, "year=2024, season=SPRING"))
                .when(aniListFetchService).fetchAndSaveMappings(year, season);
        doThrow(new FetchFailedException(Platform.MyAnimeList, "year=2024, season=SPRING"))
                .when(myAnimeListFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception even when all fail
        fetchService.fetchMapping(year, season);

        // Verify all services were called
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testLinkMappings_BangumiFailsButOthersContinue() {
        // Mock Bangumi to throw exception
        doThrow(new RuntimeException("Bangumi link failed"))
                .when(bangumiFetchService).linkAllOrphanedMappings();

        // Execute - should not throw exception
        fetchService.linkMappings();

        // Verify all services were called
        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();
    }

    @Test
    public void testLinkMappings_AniListFailsButOthersContinue() {
        // Mock AniList to throw exception
        doThrow(new RuntimeException("AniList link failed"))
                .when(aniListFetchService).linkAllOrphanedMappings();

        // Execute - should not throw exception
        fetchService.linkMappings();

        // Verify all services were called
        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();
    }

    @Test
    public void testLinkMappings_MyAnimeListFailsButOthersContinue() {
        // Mock MyAnimeList to throw exception
        doThrow(new RuntimeException("MyAnimeList link failed"))
                .when(myAnimeListFetchService).linkAllOrphanedMappings();

        // Execute - should not throw exception
        fetchService.linkMappings();

        // Verify all services were called
        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();
    }

    @Test
    public void testLinkMappings_AllPlatformsFail() {
        // Mock all services to throw exceptions
        doThrow(new RuntimeException("Bangumi link failed"))
                .when(bangumiFetchService).linkAllOrphanedMappings();
        doThrow(new RuntimeException("AniList link failed"))
                .when(aniListFetchService).linkAllOrphanedMappings();
        doThrow(new RuntimeException("MyAnimeList link failed"))
                .when(myAnimeListFetchService).linkAllOrphanedMappings();

        // Execute - should not throw exception even when all fail
        fetchService.linkMappings();

        // Verify all services were called
        verify(bangumiFetchService, times(1)).linkAllOrphanedMappings();
        verify(aniListFetchService, times(1)).linkAllOrphanedMappings();
        verify(myAnimeListFetchService, times(1)).linkAllOrphanedMappings();
    }
}
