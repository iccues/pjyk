package com.yukiani.mapper;

import com.yukiani.dto.admin.*;
import com.yukiani.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AdminAnimeMapperTest {

    @Resource
    private AdminAnimeMapper adminAnimeMapper;

    private Anime testAnime;
    private Mapping testMapping;
    private AnimeTitles testTitles;

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
        testAnime.setAnimeId(1L);
        testAnime.setVersion(1);
        testAnime.setTitle(testTitles);
        testAnime.setCoverImage("https://example.com/cover.jpg");
        testAnime.setStartDate(LocalDate.of(2024, 4, 1));
        testAnime.setAverageScore(8.5);
        testAnime.setReviewStatus(ReviewStatus.APPROVED);

        // 创建测试用的 Mapping
        testMapping = new Mapping();
        testMapping.setMappingId(10L);
        testMapping.setAnime(testAnime);
        testMapping.setSourcePlatform(Platform.AniList);
        testMapping.setPlatformId("12345");
        testMapping.setRawScore(85.0);
        testMapping.setNormalizedScore(8.5);

        // 设置 mappingInfo
        MappingInfo testMappingInfo = new MappingInfo(testTitles, "https://example.com/cover.jpg", LocalDate.of(2024, 4, 1));
        testMapping.setMappingInfo(testMappingInfo);

        testAnime.setMappings(List.of(testMapping));
    }

    // ============ toAnimeDto 测试 ============

    @Test
    public void testToAnimeDto_Success() {
        // 执行映射
        AdminAnimeDTO result = adminAnimeMapper.toAnimeDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.animeId());
        assertEquals(testTitles, result.title());
        assertEquals("https://example.com/cover.jpg", result.coverImage());
        assertEquals(LocalDate.of(2024, 4, 1), result.startDate());
        assertEquals(8.5, result.averageScore());
        assertEquals(ReviewStatus.APPROVED, result.reviewStatus());
        assertNotNull(result.mappings());
        assertEquals(1, result.mappings().size());
    }

    @Test
    public void testToAnimeDto_NullAnime() {
        // 执行映射
        AdminAnimeDTO result = adminAnimeMapper.toAnimeDto(null);

        // 验证结果为 null
        assertNull(result);
    }

    @Test
    public void testToAnimeDto_EmptyMappings() {
        // 准备：清空 mappings
        testAnime.setMappings(new ArrayList<>());

        // 执行映射
        AdminAnimeDTO result = adminAnimeMapper.toAnimeDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.mappings());
        assertEquals(0, result.mappings().size());
    }

    @Test
    public void testToAnimeDto_NullFields() {
        // 准备：设置部分字段为 null
        testAnime.setTitle(null);
        testAnime.setCoverImage(null);
        testAnime.setStartDate(null);
        testAnime.setAverageScore(null);

        // 执行映射
        AdminAnimeDTO result = adminAnimeMapper.toAnimeDto(testAnime);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.animeId());
        assertNull(result.title());
        assertNull(result.coverImage());
        assertNull(result.startDate());
        assertNull(result.averageScore());
    }

    // ============ toAnimeDtoList 测试 ============

    @Test
    public void testToAnimeDtoList_Success() {
        // 准备：创建第二个 Anime
        Anime anime2 = new Anime();
        anime2.setAnimeId(2L);
        anime2.setTitle(testTitles);
        anime2.setCoverImage("https://example.com/cover2.jpg");
        anime2.setStartDate(LocalDate.of(2024, 7, 1));
        anime2.setAverageScore(9.0);
        anime2.setReviewStatus(ReviewStatus.PENDING);
        anime2.setMappings(new ArrayList<>());

        List<Anime> animeList = List.of(testAnime, anime2);

        // 执行映射
        List<AdminAnimeDTO> result = adminAnimeMapper.toAnimeDtoList(animeList);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).animeId());
        assertEquals(2L, result.get(1).animeId());
    }

    @Test
    public void testToAnimeDtoList_EmptyList() {
        // 执行映射
        List<AdminAnimeDTO> result = adminAnimeMapper.toAnimeDtoList(new ArrayList<>());

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToAnimeDtoList_NullList() {
        // 执行映射
        List<AdminAnimeDTO> result = adminAnimeMapper.toAnimeDtoList(null);

        // 验证结果为 null
        assertNull(result);
    }

    // ============ toMappingDto 测试 ============

    @Test
    public void testToMappingDto_WithMappingInfo() {
        // 执行映射
        AdminMappingDTO result = adminAnimeMapper.toMappingDto(testMapping);

        // 验证结果
        assertNotNull(result);
        assertEquals(10L, result.mappingId());
        assertEquals(1L, result.animeId());
        assertEquals(Platform.AniList, result.sourcePlatform());
        assertEquals("12345", result.platformId());
        assertEquals(85.0, result.rawScore());
        assertNotNull(result.mappingInfo());
        assertEquals(testTitles, result.mappingInfo().getTitle());
        assertEquals("https://example.com/cover.jpg", result.mappingInfo().getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), result.mappingInfo().getStartDate());
    }

    @Test
    public void testToMappingDto_NullMappingInfo() {
        // 准备：创建一个没有 mappingInfo 的 Mapping
        testMapping.setMappingInfo(null);

        // 执行映射
        AdminMappingDTO result = adminAnimeMapper.toMappingDto(testMapping);

        // 验证结果
        assertNotNull(result);
        assertEquals(10L, result.mappingId());
        assertNull(result.mappingInfo());
    }

    @Test
    public void testToMappingDto_NullMapping() {
        // 执行映射
        AdminMappingDTO result = adminAnimeMapper.toMappingDto(null);

        // 验证结果为 null
        assertNull(result);
    }

    @Test
    public void testToMappingDto_NullAnime() {
        // 准备：Mapping 没有关联 Anime
        testMapping.setAnime(null);

        // 执行映射
        AdminMappingDTO result = adminAnimeMapper.toMappingDto(testMapping);

        // 验证结果
        assertNotNull(result);
        assertEquals(10L, result.mappingId());
        assertNull(result.animeId()); // animeId 应该为 null
    }

    // ============ toMappingDtoList 测试 ============

    @Test
    public void testToMappingDtoList_Success() {
        // 准备：创建第二个 Mapping
        Mapping mapping2 = new Mapping();
        mapping2.setMappingId(20L);
        mapping2.setAnime(testAnime);
        mapping2.setSourcePlatform(Platform.Bangumi);
        mapping2.setPlatformId("67890");
        mapping2.setRawScore(8.2);

        List<Mapping> mappingList = List.of(testMapping, mapping2);

        // 执行映射
        List<AdminMappingDTO> result = adminAnimeMapper.toMappingDtoList(mappingList);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).mappingId());
        assertEquals(20L, result.get(1).mappingId());
    }

    @Test
    public void testToMappingDtoList_EmptyList() {
        // 执行映射
        List<AdminMappingDTO> result = adminAnimeMapper.toMappingDtoList(new ArrayList<>());

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToMappingDtoList_NullList() {
        // 执行映射
        List<AdminMappingDTO> result = adminAnimeMapper.toMappingDtoList(null);

        // 验证结果为 null
        assertNull(result);
    }

    // ============ requestToAnime 测试 ============

    @Test
    public void testRequestToAnime_Success() {
        // 准备：创建 AnimeCreateRequest
        AnimeCreateRequest request = new AnimeCreateRequest(
                testTitles,
                "https://example.com/new-cover.jpg",
                LocalDate.of(2024, 10, 1)
        );

        // 执行映射
        Anime result = adminAnimeMapper.requestToAnime(request);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getAnimeId()); // animeId 应该被忽略（未设置）
        assertEquals(0, result.getVersion()); // version 应该被忽略，但 Anime 有默认值 0
        assertEquals(testTitles, result.getTitle());
        assertEquals("https://example.com/new-cover.jpg", result.getCoverImage());
        assertEquals(LocalDate.of(2024, 10, 1), result.getStartDate());
        assertNull(result.getAverageScore()); // averageScore 应该被忽略
        assertEquals(ReviewStatus.PENDING, result.getReviewStatus()); // reviewStatus 应该被忽略，但 Anime 有默认值 PENDING
        assertNotNull(result.getMappings()); // mappings 应该被忽略，但 Anime 有默认值 new ArrayList<>()
        assertEquals(0, result.getMappings().size());
    }

    @Test
    public void testRequestToAnime_NullRequest() {
        // 执行映射
        Anime result = adminAnimeMapper.requestToAnime(null);

        // 验证结果为 null
        assertNull(result);
    }

    @Test
    public void testRequestToAnime_NullFields() {
        // 准备：创建所有字段为 null 的 request
        AnimeCreateRequest request = new AnimeCreateRequest(null, null, null);

        // 执行映射
        Anime result = adminAnimeMapper.requestToAnime(request);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getTitle());
        assertNull(result.getCoverImage());
        assertNull(result.getStartDate());
    }

    // ============ updateAnimeByRequest 测试 ============

    @Test
    public void testUpdateAnimeByRequest_Success() {
        // 准备：创建新的 AnimeTitles
        AnimeTitles newTitles = new AnimeTitles();
        newTitles.setTitleNative("新ネイティブタイトル");
        newTitles.setTitleRomaji("New Romaji Title");

        // 创建 AnimeUpdateRequest
        AnimeUpdateRequest request = new AnimeUpdateRequest(
                1L,
                newTitles,
                "https://example.com/updated-cover.jpg",
                ReviewStatus.REJECTED,
                LocalDate.of(2024, 12, 1)
        );

        // 执行映射（更新 testAnime）
        adminAnimeMapper.updateAnimeByRequest(request, testAnime);

        // 验证结果 - 应该更新字段
        assertEquals(newTitles, testAnime.getTitle());
        assertEquals("https://example.com/updated-cover.jpg", testAnime.getCoverImage());
        assertEquals(ReviewStatus.REJECTED, testAnime.getReviewStatus());
        assertEquals(LocalDate.of(2024, 12, 1), testAnime.getStartDate());

        // 验证 ignored 字段没有被修改
        assertEquals(1L, testAnime.getAnimeId()); // animeId 保持不变
        assertEquals(1, testAnime.getVersion()); // version 保持不变
        assertEquals(8.5, testAnime.getAverageScore()); // averageScore 保持不变
    }

    @Test
    public void testUpdateAnimeByRequest_NullValuePropertyIgnore() {
        // 准备：创建部分字段为 null 的 request
        AnimeUpdateRequest request = new AnimeUpdateRequest(
                1L,
                null, // title 为 null
                null, // coverImage 为 null
                ReviewStatus.APPROVED,
                LocalDate.of(2024, 11, 1)
        );

        // 记录原始值
        AnimeTitles originalTitle = testAnime.getTitle();
        String originalCoverImage = testAnime.getCoverImage();

        // 执行映射
        adminAnimeMapper.updateAnimeByRequest(request, testAnime);

        // 验证结果 - null 值应该被忽略（IGNORE 策略）
        assertEquals(originalTitle, testAnime.getTitle()); // title 保持不变
        assertEquals(originalCoverImage, testAnime.getCoverImage()); // coverImage 保持不变
        assertEquals(ReviewStatus.APPROVED, testAnime.getReviewStatus()); // 非 null 值正常更新
        assertEquals(LocalDate.of(2024, 11, 1), testAnime.getStartDate());
    }

    @Test
    public void testUpdateAnimeByRequest_AllFieldsNull() {
        // 准备：所有字段为 null 的 request（除了 animeId）
        AnimeUpdateRequest request = new AnimeUpdateRequest(
                1L,
                null,
                null,
                null,
                null
        );

        // 记录原始值
        AnimeTitles originalTitle = testAnime.getTitle();
        String originalCoverImage = testAnime.getCoverImage();
        ReviewStatus originalReviewStatus = testAnime.getReviewStatus();
        LocalDate originalStartDate = testAnime.getStartDate();

        // 执行映射
        adminAnimeMapper.updateAnimeByRequest(request, testAnime);

        // 验证结果 - 所有字段都应该保持不变（IGNORE 策略）
        assertEquals(originalTitle, testAnime.getTitle());
        assertEquals(originalCoverImage, testAnime.getCoverImage());
        assertEquals(originalReviewStatus, testAnime.getReviewStatus());
        assertEquals(originalStartDate, testAnime.getStartDate());
    }

    @Test
    public void testUpdateAnimeByRequest_PartialUpdate() {
        // 准备：只更新部分字段
        AnimeUpdateRequest request = new AnimeUpdateRequest(
                1L,
                null,
                "https://example.com/partial-update.jpg",
                null,
                null
        );

        // 记录原始值
        AnimeTitles originalTitle = testAnime.getTitle();
        ReviewStatus originalReviewStatus = testAnime.getReviewStatus();
        LocalDate originalStartDate = testAnime.getStartDate();

        // 执行映射
        adminAnimeMapper.updateAnimeByRequest(request, testAnime);

        // 验证结果
        assertEquals(originalTitle, testAnime.getTitle()); // 保持不变
        assertEquals("https://example.com/partial-update.jpg", testAnime.getCoverImage()); // 更新
        assertEquals(originalReviewStatus, testAnime.getReviewStatus()); // 保持不变
        assertEquals(originalStartDate, testAnime.getStartDate()); // 保持不变
    }
}
