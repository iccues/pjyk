package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.MappingInfo;
import com.yukiani.entity.Platform;
import com.yukiani.repo.AnimeRepository;
import com.yukiani.repo.MappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TitleBasedLinkServiceTest {

    @Mock
    private AnimeRepoService animeRepoService;

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private MappingRepository mappingRepository;

    @Mock
    private AnimeAggregationService animeAggregationService;

    @InjectMocks
    private TitleBasedLinkService titleBasedLinkService;

    @Test
    public void testFindOrCreateAnime_NewAnime() {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("New Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/new.jpg", LocalDate.of(2024, 4, 1));

        // Mock animeRepoService.findAnime 返回 null（找不到现有动画）
        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(null);

        // Mock animeRepoService.createAnime 返回新创建的动画
        Anime newAnime = new Anime();
        newAnime.setTitle(titles);
        newAnime.setStartDate(LocalDate.of(2024, 4, 1));
        newAnime.setCoverImage("https://example.com/new.jpg");
        when(animeRepoService.createAnime(any(MappingInfo.class))).thenReturn(newAnime);
        when(animeRepository.save(any(Anime.class))).thenReturn(newAnime);

        // 调用方法
        Anime result = titleBasedLinkService.findOrCreateAnime(mappingInfo);

        // 验证结果
        assertNotNull(result);
        assertEquals("https://example.com/new.jpg", result.getCoverImage());
        verify(animeRepoService, times(1)).createAnime(mappingInfo);
        verify(animeRepository, times(1)).save(newAnime);
    }

    @Test
    public void testFindOrCreateAnime_ExistingAnime() {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Existing Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/new.jpg", LocalDate.of(2024, 4, 1));

        // Mock animeRepoService.findAnime 返回一个已有的动画
        Anime existingAnime = new Anime();
        existingAnime.setTitle(titles);
        existingAnime.setStartDate(LocalDate.of(2024, 4, 1));
        existingAnime.setCoverImage("https://example.com/old.jpg");

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(existingAnime);

        // 调用方法
        Anime result = titleBasedLinkService.findOrCreateAnime(mappingInfo);

        // 验证结果：应该直接返回现有的 Anime，不调用 save
        assertNotNull(result);
        assertEquals("https://example.com/old.jpg", result.getCoverImage());
        verify(animeRepository, never()).save(any(Anime.class));
        verify(animeRepoService, never()).createAnime(any(MappingInfo.class));
    }

    @Test
    public void testLinkMappingToAnime_UnlinkedMapping() {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Test Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/image.jpg", LocalDate.of(2024, 1, 15));

        // 准备未关联的 Mapping
        Mapping mapping = new Mapping(Platform.Bangumi, "12345", mappingInfo);

        // Mock anime - findAnime 返回现有的 Anime
        Anime anime = new Anime();
        anime.setTitle(titles);

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(anime);
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        // 调用方法
        titleBasedLinkService.linkMappingToAnime(mapping);

        // 验证：findOrCreateAnime 找到现有 Anime 不调用 save，linkMappingToAnime 调用一次，共1次
        verify(animeRepository, times(1)).save(anime);
    }

    @Test
    public void testLinkAllOrphanedMappings() {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Orphaned Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/image.jpg", LocalDate.of(2024, 1, 15));

        Mapping mapping1 = new Mapping(Platform.Bangumi, "1", mappingInfo);
        Mapping mapping2 = new Mapping(Platform.Bangumi, "2", mappingInfo);

        List<Mapping> orphanedMappings = List.of(mapping1, mapping2);

        // Mock repository
        when(mappingRepository.findAllByAnimeIsNullAndMappingInfo_StartDateIsNotNull())
                .thenReturn(orphanedMappings);

        // Mock anime service - findAnime 返回现有的 Anime
        Anime anime = new Anime();
        anime.setTitle(titles);

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(anime);
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        // 调用方法
        titleBasedLinkService.linkAllOrphanedMappings();

        // 验证：每个 mapping 调用 linkMappingToAnime，findOrCreateAnime 找到现有 Anime 不调用 save，linkMappingToAnime 调用一次，共2次
        verify(animeRepository, times(2)).save(any(Anime.class));
    }
}
