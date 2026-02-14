import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Anime } from "@/types/anime";
import type { Page } from "@/types/page";
import AnimeList from "../AnimeList.vue";

// Mock API
vi.mock("@/api/public/anime", () => ({
  getAnimeList: vi.fn(),
}));

import { getAnimeList } from "@/api/public/anime";

const mockAnimeData: Page<Anime> = {
  content: [
    {
      animeId: 1,
      title: {
        titleNative: "进击的巨人",
        titleRomaji: "Shingeki no Kyojin",
        titleEn: "Attack on Titan",
        titleCn: "进击的巨人",
      },
      coverImage: "https://example.com/1.jpg",
      averageScore: 8.5,
      mappings: [],
    },
    {
      animeId: 2,
      title: {
        titleNative: "鬼灭之刃",
        titleRomaji: "Kimetsu no Yaiba",
        titleEn: "Demon Slayer",
        titleCn: "鬼灭之刃",
      },
      coverImage: "https://example.com/2.jpg",
      averageScore: 8.7,
      mappings: [],
    },
    {
      animeId: 3,
      title: {
        titleNative: "咒术回战",
        titleRomaji: "Jujutsu Kaisen",
        titleEn: "Jujutsu Kaisen",
        titleCn: "咒术回战",
      },
      coverImage: "https://example.com/3.jpg",
      averageScore: 8.6,
      mappings: [],
    },
  ],
  page: {
    totalElements: 30,
    totalPages: 3,
    size: 10,
    number: 0,
  },
};

describe("AnimeList.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("应该在挂载时立即获取数据", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    mount(AnimeList, {
      props: {
        year: 2026,
        season: "WINTER",
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    expect(getAnimeList).toHaveBeenCalledWith({
      year: 2026,
      season: "WINTER",
      page: 0,
      pageSize: 10,
    });
  });

  it("应该显示加载状态", () => {
    vi.mocked(getAnimeList).mockImplementation(() => new Promise(() => {}));

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    expect(wrapper.text()).toContain("加载中");
  });

  it("应该显示错误信息", async () => {
    vi.mocked(getAnimeList).mockRejectedValue(new Error("API 错误"));

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    expect(wrapper.text()).toContain("API 错误");
  });

  it("应该渲染动漫卡片", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    const cards = wrapper.findAllComponents({ name: "AnimeCard" });
    expect(cards).toHaveLength(3);
  });

  it('应该在数据为空时显示"暂无数据"', async () => {
    vi.mocked(getAnimeList).mockResolvedValue({
      content: [],
      page: { totalElements: 0, totalPages: 0, size: 10, number: 0 },
    });

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    expect(wrapper.text()).toContain("暂无数据");
  });

  it("应该触发 update:pageInfo 事件", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    expect(wrapper.emitted("update:pageInfo")).toBeTruthy();
    expect(wrapper.emitted("update:pageInfo")?.[0]).toEqual([mockAnimeData.page]);
  });

  it("应该在 props 变化时重新获取数据", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        year: 2026,
        season: "WINTER",
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(1);

    // 改变 year
    await wrapper.setProps({ year: 2025 });
    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(2);
    expect(getAnimeList).toHaveBeenLastCalledWith({
      year: 2025,
      season: "WINTER",
      page: 0,
      pageSize: 10,
    });
  });

  it("应该在 season 变化时重新获取数据", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        year: 2026,
        season: "WINTER",
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(1);

    // 改变 season
    await wrapper.setProps({ season: "SPRING" });
    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(2);
  });

  it("应该在 page 变化时重新获取数据", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(1);

    // 改变 page
    await wrapper.setProps({ page: 1 });
    await flushPromises();
    expect(getAnimeList).toHaveBeenCalledTimes(2);
    expect(getAnimeList).toHaveBeenLastCalledWith({
      page: 1,
      pageSize: 10,
    });
  });

  it("应该使用 grid 布局渲染卡片", async () => {
    vi.mocked(getAnimeList).mockResolvedValue(mockAnimeData);

    const wrapper = mount(AnimeList, {
      props: {
        page: 0,
        pageSize: 10,
      },
      global: {
        stubs: {
          AnimeCard: true,
        },
      },
    });

    await flushPromises();

    const grid = wrapper.find(".grid");
    expect(grid.exists()).toBe(true);
    expect(grid.classes()).toContain("justify-center");
  });
});
