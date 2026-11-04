package com.yukiani.server.graphql;

import com.yukiani.server.generated.types.Anime;
import com.yukiani.server.generated.types.Mapping;
import com.yukiani.server.generated.types.Platform;
import com.yukiani.server.service.MetricTimelineService;
import com.yukiani.server.entity.PlatformKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricHistoryResolverTest {
    @Mock MetricTimelineService service;
    @Mock GraphQLTypeMapper mapper;
    @InjectMocks MetricHistoryResolver resolver;

    @Test
    void acceptsNinetyInclusiveDaysIncludingFutureDates() {
        Anime anime = new Anime();
        anime.setMappings(List.of());
        Mapping mapping = new Mapping();
        var key = new com.yukiani.server.generated.types.PlatformKey(Platform.MyAnimeList, "test");
        mapping.setPlatformKey(key);
        when(mapper.toPlatformKey(key))
                .thenReturn(new PlatformKey(com.yukiani.server.entity.Platform.MyAnimeList, "test"));
        // 2099-01-01 至 2099-03-31 含首尾共 90 天。
        assertEquals(List.of(), resolver.animeMetricHistory(anime, "2099-01-01", "2099-03-31"));
        assertEquals(List.of(), resolver.mappingMetricHistory(mapping, "2099-01-01", "2099-03-31"));
        verify(service).animeHistory(List.of(), LocalDate.parse("2099-01-01"), LocalDate.parse("2099-03-31"));
        verify(service).mappingHistory(new PlatformKey(
                com.yukiani.server.entity.Platform.MyAnimeList, "test"),
                LocalDate.parse("2099-01-01"), LocalDate.parse("2099-03-31"));
    }

    @Test
    void rejectsInvalidRangesBeforeQuerying() {
        Anime anime = new Anime();
        anime.setMappings(List.of());
        for (String[] range : List.of(
                new String[]{"2099-01-01", "2099-04-01"},
                new String[]{"2024-01-02", "2024-01-01"},
                new String[]{"2024-02-30", "2024-03-01"})) {
            assertThrows(IllegalArgumentException.class,
                    () -> resolver.animeMetricHistory(anime, range[0], range[1]));
            assertThrows(IllegalArgumentException.class,
                    () -> resolver.mappingMetricHistory(new Mapping(), range[0], range[1]));
        }
        assertThrows(IllegalArgumentException.class,
                () -> resolver.animeMetricAt(anime, "2024-02-30"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.mappingMetricAt(new Mapping(), "2024-02-30"));
        verifyNoInteractions(service);
    }
}
