package com.yukiani.service;

import com.yukiani.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InfoService 的单元测试
 * 测试动画信息聚合逻辑
 */
public class InfoServiceTest {

    private InfoService infoService;
    private Anime anime;
    private Mapping mapping1;
    private Mapping mapping2;
    private Mapping mapping3;

    @BeforeEach
    void setUp() {
        infoService = new InfoService();
        anime = new Anime();
        
        // 创建带有 MappingInfo 的 Mapping
        AnimeTitles titles1 = new AnimeTitles();
        titles1.setTitleNative("动画标题1");
        titles1.setTitleEn("Anime Title 1");
        MappingInfo mappingInfo1 = new MappingInfo(
                titles1,
                "https://example.com/cover1.jpg",
                LocalDate.of(2024, 4, 1)
        );
        mapping1 = new Mapping(Platform.AniList, "1", mappingInfo1);

        AnimeTitles titles2 = new AnimeTitles();
        titles2.setTitleNative("动画标题2");
        titles2.setTitleRomaji("Anime Romaji 2");
        MappingInfo mappingInfo2 = new MappingInfo(
                titles2,
                "https://example.com/cover2.jpg",
                LocalDate.of(2024, 4, 5)
        );
        mapping2 = new Mapping(Platform.MyAnimeList, "2", mappingInfo2);

        AnimeTitles titles3 = new AnimeTitles();
        titles3.setTitleNative("动画标题3");
        MappingInfo mappingInfo3 = new MappingInfo(
                titles3,
                "https://example.com/bangumi.jpg",
                LocalDate.of(2024, 4, 10)
        );
        mapping3 = new Mapping(Platform.Bangumi, "3", mappingInfo3);
    }

    // ==================== aggregateInfo 测试 ====================

    @Test
    void testAggregateInfo_WithMultipleMappings() {
        // 添加多个 mapping
        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);

        // 聚合信息
        infoService.aggregateInfo(anime);

        // 验证：封面应该来自 Bangumi（优先级最高）
        assertEquals("https://example.com/bangumi.jpg", anime.getCoverImage());
        
        // 验证：开始日期应该是第一个 mapping 的日期
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        
        // 验证：标题应该合并所有 mapping 的标题
        assertNotNull(anime.getTitle());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
        assertEquals("Anime Title 1", anime.getTitle().getTitleEn());
        assertEquals("Anime Romaji 2", anime.getTitle().getTitleRomaji());
    }

    @Test
    void testAggregateInfo_WithSingleMapping() {
        // 只添加一个 mapping
        anime.addMapping(mapping1);

        // 聚合信息
        infoService.aggregateInfo(anime);

        // 验证
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
        assertEquals("Anime Title 1", anime.getTitle().getTitleEn());
    }

    @Test
    void testAggregateInfo_WithNoMappings() {
        // 不添加任何 mapping
        
        // 聚合信息
        infoService.aggregateInfo(anime);

        // 验证：所有字段应该被清空
        assertNull(anime.getCoverImage());
        assertNull(anime.getStartDate());
        assertNotNull(anime.getTitle());
        assertNull(anime.getTitle().getTitleNative());
    }

    @Test
    void testAggregateInfo_WithNullMappingInfo() {
        // 创建没有 MappingInfo 的 Mapping
        Mapping mappingWithoutInfo = new Mapping();
        mappingWithoutInfo.setSourcePlatform(Platform.AniList);
        mappingWithoutInfo.setPlatformId("999");
        
        anime.addMapping(mappingWithoutInfo);
        anime.addMapping(mapping1);

        // 聚合信息
        infoService.aggregateInfo(anime);

        // 验证：应该跳过没有 MappingInfo 的 mapping，使用有效的 mapping
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
    }

    // ==================== applyMappingInfo 测试 ====================

    @Test
    void testApplyMappingInfo_ToEmptyAnime() {
        // 应用 mapping 信息到空的 anime
        infoService.applyMappingInfo(anime, mapping1);

        // 验证：所有信息都被应用
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
        assertEquals("Anime Title 1", anime.getTitle().getTitleEn());
    }

    @Test
    void testApplyMappingInfo_WithExistingCoverImage() {
        // anime 已有封面
        anime.setCoverImage("https://example.com/existing.jpg");

        // 应用 mapping 信息
        infoService.applyMappingInfo(anime, mapping1);

        // 验证：封面不被覆盖
        assertEquals("https://example.com/existing.jpg", anime.getCoverImage());
        
        // 验证：其他信息被应用
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
    }

    @Test
    void testApplyMappingInfo_WithExistingStartDate() {
        // anime 已有开始日期
        anime.setStartDate(LocalDate.of(2024, 1, 1));

        // 应用 mapping 信息
        infoService.applyMappingInfo(anime, mapping1);

        // 验证：开始日期不被覆盖
        assertEquals(LocalDate.of(2024, 1, 1), anime.getStartDate());
        
        // 验证：其他信息被应用
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals("动画标题1", anime.getTitle().getTitleNative());
    }

    @Test
    void testApplyMappingInfo_MergesTitles() {
        // anime 已有部分标题
        AnimeTitles existingTitles = new AnimeTitles();
        existingTitles.setTitleNative("现有标题");
        existingTitles.setTitleCn("现有中文标题");
        anime.setTitle(existingTitles);

        // 应用 mapping 信息
        infoService.applyMappingInfo(anime, mapping1);

        // 验证：标题被合并（不是覆盖）
        assertEquals("现有标题", anime.getTitle().getTitleNative());
        assertEquals("现有中文标题", anime.getTitle().getTitleCn());
        assertEquals("Anime Title 1", anime.getTitle().getTitleEn());
    }

    @Test
    void testApplyMappingInfo_WithNullMappingInfo() {
        // 创建没有 MappingInfo 的 Mapping
        Mapping mappingWithoutInfo = new Mapping();
        mappingWithoutInfo.setSourcePlatform(Platform.AniList);

        // 应用 mapping 信息
        infoService.applyMappingInfo(anime, mappingWithoutInfo);

        // 验证：anime 保持不变（因为 MappingInfo 为 null，方法提前返回）
        assertNull(anime.getCoverImage());
        assertNull(anime.getStartDate());
        assertNotNull(anime.getTitle());
        assertNull(anime.getTitle().getTitleNative());
    }

    // ==================== cleanInfo 测试 ====================

    @Test
    void testCleanInfo_ClearsAllFields() {
        // 设置 anime 的信息
        anime.setCoverImage("https://example.com/cover.jpg");
        anime.setStartDate(LocalDate.of(2024, 4, 1));
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("标题");
        anime.setTitle(titles);

        // 清空信息
        infoService.cleanInfo(anime);

        // 验证：所有字段被清空
        assertNull(anime.getCoverImage());
        assertNull(anime.getStartDate());
        assertNotNull(anime.getTitle());
        assertNull(anime.getTitle().getTitleNative());
    }

    @Test
    void testCleanInfo_OnEmptyAnime() {
        // 清空已经是空的 anime
        infoService.cleanInfo(anime);

        // 验证：不会出错，字段保持为空
        assertNull(anime.getCoverImage());
        assertNull(anime.getStartDate());
        assertNotNull(anime.getTitle());
    }

    // ==================== setCoverImageFromBangumi 测试 ====================

    @Test
    void testSetCoverImageFromBangumi_WithBangumiMapping() {
        // 添加 Bangumi mapping
        anime.addMapping(mapping3);

        // 设置封面
        infoService.setCoverImageFromBangumi(anime);

        // 验证：封面来自 Bangumi
        assertEquals("https://example.com/bangumi.jpg", anime.getCoverImage());
    }

    @Test
    void testSetCoverImageFromBangumi_WithoutBangumiMapping() {
        // 只添加非 Bangumi mapping
        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        // 设置封面
        infoService.setCoverImageFromBangumi(anime);

        // 验证：封面保持为 null
        assertNull(anime.getCoverImage());
    }

    @Test
    void testSetCoverImageFromBangumi_WithNullMappingInfo() {
        // 创建没有 MappingInfo 的 Bangumi Mapping
        Mapping bangumiWithoutInfo = new Mapping();
        bangumiWithoutInfo.setSourcePlatform(Platform.Bangumi);
        bangumiWithoutInfo.setPlatformId("999");
        
        anime.addMapping(bangumiWithoutInfo);

        // 设置封面
        infoService.setCoverImageFromBangumi(anime);

        // 验证：不会出错，封面保持为 null
        assertNull(anime.getCoverImage());
    }

    @Test
    void testSetCoverImageFromBangumi_OverwritesExisting() {
        // anime 已有封面
        anime.setCoverImage("https://example.com/old.jpg");
        
        // 添加 Bangumi mapping
        anime.addMapping(mapping3);

        // 设置封面
        infoService.setCoverImageFromBangumi(anime);

        // 验证：封面被 Bangumi 的封面覆盖
        assertEquals("https://example.com/bangumi.jpg", anime.getCoverImage());
    }

    // ==================== 集成测试 ====================

    @Test
    void testAggregateInfo_CompleteWorkflow() {
        // 模拟完整的聚合流程
        
        // 1. 添加第一个 mapping（AniList）
        anime.addMapping(mapping1);
        infoService.aggregateInfo(anime);
        
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        
        // 2. 添加第二个 mapping（MyAnimeList）
        anime.addMapping(mapping2);
        infoService.aggregateInfo(anime);
        
        // 封面和日期保持不变（已有值）
        assertEquals("https://example.com/cover1.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
        // 标题被合并
        assertEquals("Anime Romaji 2", anime.getTitle().getTitleRomaji());
        
        // 3. 添加 Bangumi mapping
        anime.addMapping(mapping3);
        infoService.aggregateInfo(anime);
        
        // Bangumi 封面优先级最高，覆盖之前的封面
        assertEquals("https://example.com/bangumi.jpg", anime.getCoverImage());
        assertEquals(LocalDate.of(2024, 4, 1), anime.getStartDate());
    }
}
