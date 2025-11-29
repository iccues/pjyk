package com.iccues.metaanimebackend.mapper;

import com.iccues.metaanimebackend.dto.AnimeDTO;
import com.iccues.metaanimebackend.dto.MappingDTO;
import com.iccues.metaanimebackend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AnimeMapperTest {

    @Resource
    private AnimeMapper animeMapper;

    private Anime testAnime;
    private AnimeTitles testTitles;
    private Mapping testMapping1;
    private Mapping testMapping2;

    @BeforeEach
    public void setUp() {
        // 创建测试用的 AnimeTitles
        testTitles = new AnimeTitles();
        testTitles.setTitleNative("ネイティブタイトル");
        testTitles.setTitleRomaji("Romaji Title");
        testTitles.setTitleEn("English Title");
        testTitles.setTitleCn("中文标题");

        // 创建测试用的 Anime
        testAnime = new Anime();
        testAnime.setAnimeId(100L);
        testAnime.setTitle(testTitles);
        testAnime.setCoverImage("https://example.com/cover.jpg");
        testAnime.setStartDate(LocalDate.of(2024, 4, 15));
        testAnime.setAverageScore(9.2);

        // 创建测试用的 Mapping 1
        testMapping1 = new Mapping();
        testMapping1.setMappingId(1L);
        testMapping1.setAnime(testAnime);
        testMapping1.setSourcePlatform(Platform.AniList);
        testMapping1.setPlatformId("11111");
        testMapping1.setRawScore(92.0);

        // 创建测试用的 Mapping 2
        testMapping2 = new Mapping();
        testMapping2.setMappingId(2L);
        testMapping2.setAnime(testAnime);
        testMapping2.setSourcePlatform(Platform.Bangumi);
        testMapping2.setPlatformId("22222");
        testMapping2.setRawScore(9.1);

        // 设置 mappings
        testAnime.setMappings(List.of(testMapping1, testMapping2));
    }

    // ============ toDto 基础测试 ============

    @Test
    public void testToDto_Success() {
        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.animeId());
        assertEquals(testTitles, result.title());
        assertEquals("https://example.com/cover.jpg", result.coverImage());
        assertEquals(LocalDate.of(2024, 4, 15), result.startDate());
        assertEquals(9.2, result.averageScore());
    }

    @Test
    public void testToDto_NullAnime() {
        // 执行映射
        AnimeDTO result = animeMapper.toDto(null);

        // 验证结果为 null
        assertNull(result);
    }

    // ============ AnimeTitles 映射测试 ============

    @Test
    public void testToDto_AnimeTitles_AllFields() {
        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证 AnimeTitles 所有字段都正确映射
        assertNotNull(result.title());
        assertEquals("ネイティブタイトル", result.title().getTitleNative());
        assertEquals("Romaji Title", result.title().getTitleRomaji());
        assertEquals("English Title", result.title().getTitleEn());
        assertEquals("中文标题", result.title().getTitleCn());
    }

    @Test
    public void testToDto_AnimeTitles_PartialFields() {
        // 准备：只设置部分 title 字段
        AnimeTitles partialTitles = new AnimeTitles();
        partialTitles.setTitleRomaji("Partial Title");
        partialTitles.setTitleEn("Partial English");
        testAnime.setTitle(partialTitles);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result.title());
        assertNull(result.title().getTitleNative());
        assertEquals("Partial Title", result.title().getTitleRomaji());
        assertEquals("Partial English", result.title().getTitleEn());
        assertNull(result.title().getTitleCn());
    }

    @Test
    public void testToDto_NullAnimeTitles() {
        // 准备：设置 title 为 null
        testAnime.setTitle(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNull(result.title());
    }

    // ============ Mappings 列表映射测试 ============

    @Test
    public void testToDto_Mappings_MultipleMappings() {
        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证 mappings 列表
        assertNotNull(result.mappings());
        assertEquals(2, result.mappings().size());

        // 验证第一个 Mapping
        MappingDTO mapping1 = result.mappings().get(0);
        assertEquals(1L, mapping1.mappingId());
        assertEquals(Platform.AniList, mapping1.sourcePlatform());
        assertEquals("11111", mapping1.platformId());
        assertEquals(92.0, mapping1.rawScore());

        // 验证第二个 Mapping
        MappingDTO mapping2 = result.mappings().get(1);
        assertEquals(2L, mapping2.mappingId());
        assertEquals(Platform.Bangumi, mapping2.sourcePlatform());
        assertEquals("22222", mapping2.platformId());
        assertEquals(9.1, mapping2.rawScore());
    }

    @Test
    public void testToDto_Mappings_EmptyList() {
        // 准备：设置空 mappings
        testAnime.setMappings(new ArrayList<>());

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.mappings());
        assertEquals(0, result.mappings().size());
    }

    @Test
    public void testToDto_Mappings_NullList() {
        // 准备：设置 mappings 为 null
        testAnime.setMappings(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNull(result.mappings());
    }

    @Test
    public void testToDto_Mappings_SingleMapping() {
        // 准备：只保留一个 mapping
        testAnime.setMappings(List.of(testMapping1));

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result.mappings());
        assertEquals(1, result.mappings().size());
        assertEquals(1L, result.mappings().get(0).mappingId());
    }

    // ============ 字段值测试 ============

    @Test
    public void testToDto_NullCoverImage() {
        // 准备：设置 coverImage 为 null
        testAnime.setCoverImage(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNull(result.coverImage());
    }

    @Test
    public void testToDto_NullStartDate() {
        // 准备：设置 startDate 为 null
        testAnime.setStartDate(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNull(result.startDate());
    }

    @Test
    public void testToDto_NullAverageScore() {
        // 准备：设置 averageScore 为 null
        testAnime.setAverageScore(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNull(result.averageScore());
    }

    @Test
    public void testToDto_AllNullFields() {
        // 准备：设置所有可选字段为 null
        testAnime.setTitle(null);
        testAnime.setCoverImage(null);
        testAnime.setStartDate(null);
        testAnime.setAverageScore(null);
        testAnime.setMappings(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.animeId()); // animeId 不为 null
        assertNull(result.title());
        assertNull(result.coverImage());
        assertNull(result.startDate());
        assertNull(result.averageScore());
        assertNull(result.mappings());
    }

    // ============ 边界值测试 ============

    @Test
    public void testToDto_MinDate() {
        // 准备：设置最小日期
        testAnime.setStartDate(LocalDate.of(1900, 1, 1));

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(LocalDate.of(1900, 1, 1), result.startDate());
    }

    @Test
    public void testToDto_MaxDate() {
        // 准备：设置最大日期
        testAnime.setStartDate(LocalDate.of(2099, 12, 31));

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(LocalDate.of(2099, 12, 31), result.startDate());
    }

    @Test
    public void testToDto_ZeroScore() {
        // 准备：设置评分为 0
        testAnime.setAverageScore(0.0);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(0.0, result.averageScore());
    }

    @Test
    public void testToDto_MaxScore() {
        // 准备：设置评分为 10.0
        testAnime.setAverageScore(10.0);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(10.0, result.averageScore());
    }

    @Test
    public void testToDto_EmptyStrings() {
        // 准备：设置空字符串
        testAnime.setCoverImage("");
        AnimeTitles emptyTitles = new AnimeTitles();
        emptyTitles.setTitleNative("");
        emptyTitles.setTitleRomaji("");
        emptyTitles.setTitleEn("");
        emptyTitles.setTitleCn("");
        testAnime.setTitle(emptyTitles);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals("", result.coverImage());
        assertEquals("", result.title().getTitleNative());
        assertEquals("", result.title().getTitleRomaji());
        assertEquals("", result.title().getTitleEn());
        assertEquals("", result.title().getTitleCn());
    }

    // ============ MappingDTO 字段完整性测试 ============

    @Test
    public void testToDto_MappingDTO_AllFieldsMapped() {
        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证 MappingDTO 的所有字段都被正确映射
        MappingDTO mapping = result.mappings().get(0);
        assertNotNull(mapping.mappingId());
        assertNotNull(mapping.sourcePlatform());
        assertNotNull(mapping.platformId());
        assertNotNull(mapping.rawScore());
    }

    @Test
    public void testToDto_MappingDTO_NullRawScore() {
        // 准备：设置 rawScore 为 null
        testMapping1.setRawScore(null);

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证结果
        MappingDTO mapping = result.mappings().get(0);
        assertNull(mapping.rawScore());
    }

    @Test
    public void testToDto_MappingDTO_DifferentPlatforms() {
        // 准备：创建更多不同平台的 mappings
        Mapping mapping3 = new Mapping();
        mapping3.setMappingId(3L);
        mapping3.setAnime(testAnime);
        mapping3.setSourcePlatform(Platform.MyAnimeList);
        mapping3.setPlatformId("33333");
        mapping3.setRawScore(9.5);

        testAnime.setMappings(List.of(testMapping1, testMapping2, mapping3));

        // 执行映射
        AnimeDTO result = animeMapper.toDto(testAnime);

        // 验证所有平台都正确映射
        assertEquals(3, result.mappings().size());
        assertEquals(Platform.AniList, result.mappings().get(0).sourcePlatform());
        assertEquals(Platform.Bangumi, result.mappings().get(1).sourcePlatform());
        assertEquals(Platform.MyAnimeList, result.mappings().get(2).sourcePlatform());
    }
}
