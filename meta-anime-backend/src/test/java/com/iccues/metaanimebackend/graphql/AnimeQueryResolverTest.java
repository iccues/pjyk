package com.iccues.metaanimebackend.graphql;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AnimeQueryResolver 集成测试。
 * 通过 Spring GraphQL 的 HTTP 端点（POST /graphql）发送查询，验证响应数据。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AnimeQueryResolverTest {

    private static final String GRAPHQL_PATH = "/graphql";

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingRepository mappingRepository;

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    // ============ animeList query 基本测试 ============

    @Test
    public void testAnimeList_EmptyDatabase() throws Exception {
        String query = """
                {
                  "query": "{ animeList { content { animeId } pageInfo { totalElements } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content").isArray())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(0)))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalElements").value(0));
    }

    @Test
    public void testAnimeList_ReturnsSavedAnime() throws Exception {
        Anime anime = createAndSaveAnime("進撃の巨人", LocalDate.of(2024, 1, 15), 9.0, 80000.0);

        String query = """
                {
                  "query": "{ animeList { content { animeId title { titleNative } coverImage startDate averageScore popularity } pageInfo { totalElements totalPages size number } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(1)))
                .andExpect(jsonPath("$.data.animeList.content[0].animeId")
                        .value(anime.getAnimeId().toString()))
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("進撃の巨人"))
                .andExpect(jsonPath("$.data.animeList.content[0].coverImage")
                        .value("https://example.com/cover.jpg"))
                .andExpect(jsonPath("$.data.animeList.content[0].startDate")
                        .value("2024-01-15"))
                .andExpect(jsonPath("$.data.animeList.content[0].averageScore")
                        .value(9.0))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalPages").value(1));
    }

    // ============ 过滤参数测试 ============

    @Test
    public void testAnimeList_FilterByYear() throws Exception {
        createAndSaveAnime("2024动画", LocalDate.of(2024, 4, 15), 8.0, 10000.0);
        createAndSaveAnime("2023动画", LocalDate.of(2023, 4, 15), 7.5, 9000.0);

        String query = """
                {
                  "query": "{ animeList(year: 2024) { content { title { titleNative } } pageInfo { totalElements } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(1)))
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("2024动画"))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalElements").value(1));
    }

    @Test
    public void testAnimeList_FilterBySeason_Spring() throws Exception {
        // 春季：4~6月
        createAndSaveAnime("春季动画", LocalDate.of(2024, 4, 1), 8.0, 10000.0);
        // 夏季：7~9月
        createAndSaveAnime("夏季动画", LocalDate.of(2024, 7, 1), 7.0, 9000.0);

        String query = """
                {
                  "query": "{ animeList(year: 2024, season: SPRING) { content { title { titleNative } } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(1)))
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("春季动画"));
    }

    @Test
    public void testAnimeList_FilterBySeason_Winter() throws Exception {
        // 冬季：1~3月
        createAndSaveAnime("冬季动画", LocalDate.of(2024, 1, 10), 8.0, 10000.0);
        createAndSaveAnime("春季动画", LocalDate.of(2024, 5, 10), 7.0, 9000.0);

        String query = """
                {
                  "query": "{ animeList(year: 2024, season: WINTER) { content { title { titleNative } } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(1)))
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("冬季动画"));
    }

    // ============ 排序测试 ============

    @Test
    public void testAnimeList_SortByScore_Descending() throws Exception {
        createAndSaveAnime("低分动画", LocalDate.of(2024, 4, 1), 5.0, 1000.0);
        createAndSaveAnime("高分动画", LocalDate.of(2024, 4, 2), 9.5, 500.0);
        createAndSaveAnime("中分动画", LocalDate.of(2024, 4, 3), 7.0, 2000.0);

        String query = """
                {
                  "query": "{ animeList(sortBy: SCORE) { content { title { titleNative } averageScore } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(3)))
                // 按分数降序，第一个应该是高分
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("高分动画"))
                .andExpect(jsonPath("$.data.animeList.content[2].title.titleNative")
                        .value("低分动画"));
    }

    @Test
    public void testAnimeList_SortByPopularity_Descending() throws Exception {
        createAndSaveAnime("低人气动画", LocalDate.of(2024, 4, 1), 8.0, 100.0);
        createAndSaveAnime("高人气动画", LocalDate.of(2024, 4, 2), 6.0, 99999.0);

        String query = """
                {
                  "query": "{ animeList(sortBy: POPULARITY) { content { title { titleNative } popularity } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(2)))
                .andExpect(jsonPath("$.data.animeList.content[0].title.titleNative")
                        .value("高人气动画"));
    }

    // ============ 分页测试 ============

    @Test
    public void testAnimeList_Pagination_FirstPage() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createAndSaveAnime("动画" + i, LocalDate.of(2024, 4, i), (double) i, (double) (i * 1000));
        }

        String query = """
                {
                  "query": "{ animeList(pageNumber: 0, pageSize: 2) { content { title { titleNative } } pageInfo { size number totalElements totalPages } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(2)))
                .andExpect(jsonPath("$.data.animeList.pageInfo.size").value(2))
                .andExpect(jsonPath("$.data.animeList.pageInfo.number").value(0))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalElements").value(5))
                .andExpect(jsonPath("$.data.animeList.pageInfo.totalPages").value(3));
    }

    @Test
    public void testAnimeList_Pagination_SecondPage() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createAndSaveAnime("动画" + i, LocalDate.of(2024, 4, 1), (double) i, (double) (i * 1000));
        }

        String query = """
                {
                  "query": "{ animeList(pageNumber: 1, pageSize: 2, sortBy: SCORE) { content { title { titleNative } } pageInfo { number } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.animeList.content", hasSize(2)))
                .andExpect(jsonPath("$.data.animeList.pageInfo.number").value(1));
    }

    // ============ 字段转换验证 ============

    @Test
    public void testAnimeList_AnimeIdIsString() throws Exception {
        createAndSaveAnime("ID转换测试", LocalDate.of(2024, 1, 1), 8.0, 5000.0);

        String query = """
                {
                  "query": "{ animeList { content { animeId } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                // animeId 在 GraphQL 中为 ID 类型（序列化后是字符串，不是数字）
                .andExpect(jsonPath("$.data.animeList.content[0].animeId").isString());
    }

    @Test
    public void testAnimeList_StartDateFormat() throws Exception {
        createAndSaveAnime("日期格式测试", LocalDate.of(2024, 12, 31), 8.0, 5000.0);

        String query = """
                {
                  "query": "{ animeList { content { startDate } } }"
                }
                """;

        mockMvc.perform(post(GRAPHQL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                // startDate 应该是 ISO 格式字符串
                .andExpect(jsonPath("$.data.animeList.content[0].startDate").value("2024-12-31"));
    }

    // ============ 辅助方法 ============

    private Anime createAndSaveAnime(String titleNative, LocalDate startDate,
                                     Double averageScore, Double popularity) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setCoverImage("https://example.com/cover.jpg");
        anime.setStartDate(startDate);
        anime.setAverageScore(averageScore);
        anime.setPopularity(popularity);
        anime.setReviewStatus(ReviewStatus.APPROVED);

        return animeRepository.save(anime);
    }
}
