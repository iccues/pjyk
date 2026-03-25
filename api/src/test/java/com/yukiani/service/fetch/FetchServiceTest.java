package com.yukiani.service.fetch;

import com.yukiani.entity.Platform;
import com.yukiani.entity.Season;
import com.yukiani.exception.FetchFailedException;
import com.yukiani.service.TitleBasedLinkService;
import com.yukiani.service.MetricService;
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
    private TitleBasedLinkService titleBasedLinkService;

    @Mock
    private MetricService metricService;

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

        fetchService.fetchMapping(year, season, null);

        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testLinkMappings_CallsLinkService() {
        fetchService.linkMappings();

        verify(titleBasedLinkService, times(1)).linkAllOrphanedMappings();
    }

    @Test
    public void testCalculateAllMetricService() {
        fetchService.calculateAllMetric();

        verify(metricService, times(1)).calculateAllMetric();
    }

    @Test
    public void testFetchAnime_CallsAllSteps() {
        int year = 2024;
        Season season = Season.SPRING;

        fetchService.fetchAnime(year, season, null);

        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);

        verify(titleBasedLinkService, times(1)).linkAllOrphanedMappings();

        // calculateAllMetric() 已被注释，因为在 linkMappings 过程中已经通过 AnimeMergeService 计算了指标
        verify(metricService, never()).calculateAllMetric();
    }

    @Test
    public void testFetchMapping_BangumiFailsButOthersContinue() {
        int year = 2024;
        Season season = Season.SPRING;

        // Mock Bangumi to throw exception
        doThrow(new FetchFailedException(Platform.Bangumi, "year=2024, season=SPRING"))
                .when(bangumiFetchService).fetchAndSaveMappings(year, season);

        // Execute - should not throw exception
        fetchService.fetchMapping(year, season, null);

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
        fetchService.fetchMapping(year, season, null);

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
        fetchService.fetchMapping(year, season, null);

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
        fetchService.fetchMapping(year, season, null);

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
        fetchService.fetchMapping(year, season, null);

        // Verify all services were called
        verify(bangumiFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(aniListFetchService, times(1)).fetchAndSaveMappings(year, season);
        verify(myAnimeListFetchService, times(1)).fetchAndSaveMappings(year, season);
    }

    @Test
    public void testLinkMappings_HandleException() {
        // Mock LinkService to throw exception
        doThrow(new RuntimeException("Link failed"))
                .when(titleBasedLinkService).linkAllOrphanedMappings();

        // Execute - should not throw exception
        fetchService.linkMappings();

        // Verify service was called
        verify(titleBasedLinkService, times(1)).linkAllOrphanedMappings();
    }
}
