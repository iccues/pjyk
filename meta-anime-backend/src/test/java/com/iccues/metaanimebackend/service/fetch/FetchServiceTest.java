package com.iccues.metaanimebackend.service.fetch;

import com.iccues.metaanimebackend.entity.Season;
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
    public void testGetFetchServiceByName_Bangumi() {
        AbstractAnimeFetchService result = fetchService.getFetchServiceByName("Bangumi");

        assertNotNull(result);
        assertEquals(bangumiFetchService, result);
    }

    @Test
    public void testGetFetchServiceByName_AniList() {
        AbstractAnimeFetchService result = fetchService.getFetchServiceByName("AniList");

        assertNotNull(result);
        assertEquals(aniListFetchService, result);
    }

    @Test
    public void testGetFetchServiceByName_MyAnimeList() {
        AbstractAnimeFetchService result = fetchService.getFetchServiceByName("MyAnimeList");

        assertNotNull(result);
        assertEquals(myAnimeListFetchService, result);
    }

    @Test
    public void testGetFetchServiceByName_Unknown() {
        AbstractAnimeFetchService result = fetchService.getFetchServiceByName("Unknown");

        assertNull(result);
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
    public void testGetFetchServiceByName_CaseSensitive() {
        assertNull(fetchService.getFetchServiceByName("bangumi"));
        assertNull(fetchService.getFetchServiceByName("BANGUMI"));
        assertNull(fetchService.getFetchServiceByName("anilist"));
        assertNull(fetchService.getFetchServiceByName("myanimelist"));
    }
}
