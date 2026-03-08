package com.yukiani.graphql;

import com.yukiani.generated.types.AnimePage;
import com.yukiani.generated.types.PageInfo;
import com.yukiani.entity.Anime;
import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.Platform;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GraphQLTypeMapperTest {

    @Resource
    private GraphQLTypeMapper graphQLTypeMapper;

    private Anime testAnime;

    @BeforeEach
    public void setUp() {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("進撃の巨人");
        titles.setTitleRomaji("Shingeki no Kyojin");
        titles.setTitleEn("Attack on Titan");
        titles.setTitleCn("进击的巨人");

        testAnime = new Anime();
        testAnime.setAnimeId(42L);
        testAnime.setTitle(titles);
        testAnime.setCoverImage("https://example.com/cover.jpg");
        testAnime.setStartDate(LocalDate.of(2024, 4, 1));
        testAnime.setAverageScore(9.2);
        testAnime.setPopularity(98765.0);

        Mapping mapping = new Mapping();
        mapping.setMappingId(10L);
        mapping.setSourcePlatform(Platform.AniList);
        mapping.setPlatformId("16498");
        mapping.setRawScore(88.0);
        mapping.setNormalizedScore(8.8);
        mapping.setRawPopularity(50000.0);
        mapping.setNormalizedPopularity(0.9);
        mapping.setAnime(testAnime);

        testAnime.setMappings(List.of(mapping));
    }

    // ============ toSortBy 测试 ============

    @Test
    public void testToSortBy_SCORE() {
        com.yukiani.entity.SortBy result =
                graphQLTypeMapper.toSortBy(com.yukiani.generated.types.SortBy.SCORE);
        assertEquals(com.yukiani.entity.SortBy.SCORE, result);
    }

    @Test
    public void testToSortBy_POPULARITY() {
        com.yukiani.entity.SortBy result =
                graphQLTypeMapper.toSortBy(com.yukiani.generated.types.SortBy.POPULARITY);
        assertEquals(com.yukiani.entity.SortBy.POPULARITY, result);
    }

    @Test
    public void testToSortBy_Null() {
        com.yukiani.entity.SortBy result = graphQLTypeMapper.toSortBy(null);
        assertNull(result);
    }

    // ============ toSeason 测试 ============

    @Test
    public void testToSeason_WINTER() {
        com.yukiani.entity.Season result =
                graphQLTypeMapper.toSeason(com.yukiani.generated.types.Season.WINTER);
        assertEquals(com.yukiani.entity.Season.WINTER, result);
    }

    @Test
    public void testToSeason_SPRING() {
        com.yukiani.entity.Season result =
                graphQLTypeMapper.toSeason(com.yukiani.generated.types.Season.SPRING);
        assertEquals(com.yukiani.entity.Season.SPRING, result);
    }

    @Test
    public void testToSeason_SUMMER() {
        com.yukiani.entity.Season result =
                graphQLTypeMapper.toSeason(com.yukiani.generated.types.Season.SUMMER);
        assertEquals(com.yukiani.entity.Season.SUMMER, result);
    }

    @Test
    public void testToSeason_FALL() {
        com.yukiani.entity.Season result =
                graphQLTypeMapper.toSeason(com.yukiani.generated.types.Season.FALL);
        assertEquals(com.yukiani.entity.Season.FALL, result);
    }

    @Test
    public void testToSeason_Null() {
        com.yukiani.entity.Season result = graphQLTypeMapper.toSeason(null);
        assertNull(result);
    }

    // ============ longToString 测试 ============

    @Test
    public void testLongToString_ValidId() {
        String result = graphQLTypeMapper.longToString(42L);
        assertEquals("42", result);
    }

    @Test
    public void testLongToString_Zero() {
        String result = graphQLTypeMapper.longToString(0L);
        assertEquals("0", result);
    }

    @Test
    public void testLongToString_Null() {
        String result = graphQLTypeMapper.longToString(null);
        assertNull(result);
    }

    // ============ localDateToString 测试 ============

    @Test
    public void testLocalDateToString_ValidDate() {
        String result = graphQLTypeMapper.localDateToString(LocalDate.of(2024, 4, 1));
        assertEquals("2024-04-01", result);
    }

    @Test
    public void testLocalDateToString_Null() {
        String result = graphQLTypeMapper.localDateToString(null);
        assertNull(result);
    }

    // ============ toGraphQLAnime 测试 ============

    @Test
    public void testToGraphQLAnime_AllFields() {
        com.yukiani.generated.types.Anime result = graphQLTypeMapper.toGraphQLAnime(testAnime);

        assertNotNull(result);
        // animeId: Long -> String
        assertEquals("42", result.getAnimeId());
        // 标题映射
        assertNotNull(result.getTitle());
        assertEquals("進撃の巨人", result.getTitle().getTitleNative());
        assertEquals("Shingeki no Kyojin", result.getTitle().getTitleRomaji());
        assertEquals("Attack on Titan", result.getTitle().getTitleEn());
        assertEquals("进击的巨人", result.getTitle().getTitleCn());
        // coverImage
        assertEquals("https://example.com/cover.jpg", result.getCoverImage());
        // startDate: LocalDate -> String
        assertEquals("2024-04-01", result.getStartDate());
        // 数值字段
        assertEquals(9.2, result.getAverageScore());
        assertEquals(98765.0, result.getPopularity());
        // mappings
        assertNotNull(result.getMappings());
        assertEquals(1, result.getMappings().size());
    }

    @Test
    public void testToGraphQLAnime_NullStartDate() {
        testAnime.setStartDate(null);

        com.yukiani.generated.types.Anime result = graphQLTypeMapper.toGraphQLAnime(testAnime);

        assertNotNull(result);
        assertNull(result.getStartDate());
    }

    @Test
    public void testToGraphQLAnime_EmptyMappings() {
        testAnime.setMappings(new ArrayList<>());

        com.yukiani.generated.types.Anime result = graphQLTypeMapper.toGraphQLAnime(testAnime);

        assertNotNull(result);
        assertNotNull(result.getMappings());
        assertEquals(0, result.getMappings().size());
    }

    // ============ toGraphQLMapping 测试 ============

    @Test
    public void testToGraphQLMapping_AllFields() {
        Mapping mapping = testAnime.getMappings().get(0);

        com.yukiani.generated.types.Mapping result = graphQLTypeMapper.toGraphQLMapping(mapping);

        assertNotNull(result);
        // mappingId: Long -> String
        assertEquals("10", result.getMappingId());
        assertEquals(com.yukiani.generated.types.Platform.AniList, result.getSourcePlatform());
        assertEquals("16498", result.getPlatformId());
        assertEquals(88.0, result.getRawScore());
        assertEquals(8.8, result.getNormalizedScore());
        assertEquals(50000.0, result.getRawPopularity());
        assertEquals(0.9, result.getNormalizedPopularity());
    }

    @Test
    public void testToGraphQLMapping_NullScores() {
        Mapping mapping = new Mapping();
        mapping.setMappingId(20L);
        mapping.setSourcePlatform(Platform.Bangumi);
        mapping.setPlatformId("99999");
        // rawScore / normalizedScore 均为 null

        com.yukiani.generated.types.Mapping result = graphQLTypeMapper.toGraphQLMapping(mapping);

        assertNotNull(result);
        assertEquals("20", result.getMappingId());
        assertNull(result.getRawScore());
        assertNull(result.getNormalizedScore());
        assertNull(result.getRawPopularity());
        assertNull(result.getNormalizedPopularity());
    }

    // ============ toAnimePage 测试 ============

    @Test
    public void testToAnimePage_SinglePage() {
        List<Anime> animeList = List.of(testAnime);
        PageRequest pageRequest = PageRequest.of(0, 30);
        Page<Anime> page = new PageImpl<>(animeList, pageRequest, 1);

        AnimePage result = graphQLTypeMapper.toAnimePage(page);

        assertNotNull(result);
        // content
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("42", result.getContent().get(0).getAnimeId());
        // pageInfo
        PageInfo pageInfo = result.getPageInfo();
        assertNotNull(pageInfo);
        assertEquals(30, pageInfo.getSize());
        assertEquals(0, pageInfo.getNumber());
        assertEquals(1, pageInfo.getTotalElements());
        assertEquals(1, pageInfo.getTotalPages());
    }

    @Test
    public void testToAnimePage_EmptyPage() {
        PageRequest pageRequest = PageRequest.of(0, 30);
        Page<Anime> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        AnimePage result = graphQLTypeMapper.toAnimePage(emptyPage);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(0, result.getContent().size());

        PageInfo pageInfo = result.getPageInfo();
        assertNotNull(pageInfo);
        assertEquals(0, pageInfo.getTotalElements());
        assertEquals(0, pageInfo.getTotalPages());
    }

    @Test
    public void testToAnimePage_MultiPage() {
        // 模拟总共 100 条数据，每页 10 条，当前第 2 页（0-indexed）
        List<Anime> animeList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Anime anime = new Anime();
            anime.setAnimeId((long) (i + 10));
            AnimeTitles t = new AnimeTitles();
            t.setTitleNative("动画" + i);
            anime.setTitle(t);
            anime.setMappings(new ArrayList<>());
            animeList.add(anime);
        }
        PageRequest pageRequest = PageRequest.of(2, 10);
        Page<Anime> page = new PageImpl<>(animeList, pageRequest, 100);

        AnimePage result = graphQLTypeMapper.toAnimePage(page);

        assertNotNull(result);
        assertEquals(10, result.getContent().size());

        PageInfo pageInfo = result.getPageInfo();
        assertEquals(10, pageInfo.getSize());
        assertEquals(2, pageInfo.getNumber());
        assertEquals(100, pageInfo.getTotalElements());
        assertEquals(10, pageInfo.getTotalPages());
    }
}
