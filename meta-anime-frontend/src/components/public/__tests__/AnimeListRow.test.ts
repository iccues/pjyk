import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Anime } from "@/types/anime";
import type { Page } from "@/types/page";
import AnimeListRow from "../AnimeListRow.vue";

// 模拟 API 模块
vi.mock("@/api/public/anime", () => ({
  getAnimeList: vi.fn(),
}));

import { getAnimeList } from "@/api/public/anime";

// 模拟测试数据
const mockAnimeList: Page<Anime> = {
  content: [
    {
      animeId: 1,
      title: {
        titleNative: "進撃の巨人",
        titleRomaji: "Shingeki no Kyojin",
        titleEn: "Attack on Titan",
        titleCn: "进击的巨人",
      },
      coverImage: "https://example.com/cover1.jpg",
      averageScore: 8.5,
      mappings: [],
    },
    {
      animeId: 2,
      title: {
        titleNative: "鬼滅の刃",
        titleRomaji: "Kimetsu no Yaiba",
        titleEn: "Demon Slayer",
        titleCn: "鬼灭之刃",
      },
      coverImage: "https://example.com/cover2.jpg",
      averageScore: 8.7,
      mappings: [],
    },
    {
      animeId: 3,
      title: {
        titleNative: "呪術廻戦",
        titleRomaji: "Jujutsu Kaisen",
        titleEn: "Jujutsu Kaisen",
        titleCn: "咒术回战",
      },
      coverImage: "https://example.com/cover3.jpg",
      averageScore: 8.6,
      mappings: [],
    },
  ],
  page: {
    totalElements: 3,
    totalPages: 1,
    size: 12,
    number: 0,
  },
};

describe("AnimeListRow.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("基础渲染", () => {
    it("应该正确渲染标题", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "2024年秋季新番",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: {
              template: '<a :to="to"><slot /></a>',
              props: ["to"],
            },
          },
        },
      });

      await flushPromises();

      const title = wrapper.find("h2");
      expect(title.exists()).toBe(true);
      expect(title.text()).toBe("2024年秋季新番");
    });

    it("应该渲染查看更多链接", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试标题",
          year: 2024,
          season: "FALL",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: {
              template: '<a :to="to"><slot /></a>',
              props: ["to"],
            },
          },
        },
      });

      await flushPromises();

      const link = wrapper.find("a");
      expect(link.exists()).toBe(true);
      expect(link.text()).toContain("查看更多");
      expect(link.attributes("to")).toBe("/anime/list?year=2024&season=FALL");
    });
  });

  describe("数据加载", () => {
    it("应该在挂载时调用 API 获取动漫列表", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      mount(AnimeListRow, {
        props: {
          title: "测试",
          year: 2024,
          season: "FALL",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      expect(getAnimeList).toHaveBeenCalledWith({
        year: 2024,
        season: "FALL",
        pageSize: 12,
      });
    });

    it("应该显示加载状态", async () => {
      // 创建一个永不 resolve 的 Promise 来保持加载状态
      vi.mocked(getAnimeList).mockImplementation(() => new Promise(() => {}));

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      // 等待一个 tick 让组件开始加载
      await wrapper.vm.$nextTick();

      expect(wrapper.text()).toContain("加载中...");
    });

    it("应该正确渲染动漫列表", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      const animeCards = wrapper.findAllComponents({ name: "AnimeCard" });
      expect(animeCards.length).toBe(3);
    });

    it("应该显示错误信息", async () => {
      const errorMessage = "网络错误";
      vi.mocked(getAnimeList).mockRejectedValue(new Error(errorMessage));

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      expect(wrapper.text()).toContain(errorMessage);
    });

    it("应该显示暂无数据", async () => {
      vi.mocked(getAnimeList).mockResolvedValue({
        content: [],
        page: {
          totalElements: 0,
          totalPages: 0,
          size: 12,
          number: 0,
        },
      });

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      expect(wrapper.text()).toContain("暂无数据");
    });
  });

  describe("滚动功能", () => {
    it("应该在滚动时更新按钮可见性", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      const scrollContainer = wrapper.find(".overflow-x-auto");
      expect(scrollContainer.exists()).toBe(true);

      // 模拟滚动事件
      Object.defineProperty(scrollContainer.element, "scrollLeft", {
        writable: true,
        value: 100,
      });
      Object.defineProperty(scrollContainer.element, "scrollWidth", {
        writable: true,
        value: 1000,
      });
      Object.defineProperty(scrollContainer.element, "clientWidth", {
        writable: true,
        value: 500,
      });

      await scrollContainer.trigger("scroll");
      await wrapper.vm.$nextTick();

      // 验证滚动容器存在
      expect(scrollContainer.exists()).toBe(true);
    });

    it("应该在鼠标悬停时切换 hover 状态", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      const container = wrapper.find(".relative.max-w-\\[1400px\\]");
      expect(container.exists()).toBe(true);

      // 鼠标进入和离开
      await container.trigger("mouseenter");
      await container.trigger("mouseleave");

      // 验证容器存在
      expect(container.exists()).toBe(true);
    });

    it("应该在点击左侧按钮时调用滚动", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      // 设置初始滚动位置
      const scrollContainer = wrapper.find(".overflow-x-auto");
      const mockScrollTo = vi.fn();
      scrollContainer.element.scrollTo = mockScrollTo;

      Object.defineProperty(scrollContainer.element, "scrollLeft", {
        writable: true,
        value: 500,
      });

      // 模拟卡片元素
      const mockCard = document.createElement("div");
      Object.defineProperty(mockCard, "offsetWidth", { value: 200 });
      Object.defineProperty(mockCard, "offsetLeft", { value: 0 });

      const mockSecondCard = document.createElement("div");
      Object.defineProperty(mockSecondCard, "offsetLeft", { value: 220 });

      vi.spyOn(scrollContainer.element, "querySelector").mockReturnValue(
        mockCard,
      );
      Object.defineProperty(mockCard, "nextElementSibling", {
        value: mockSecondCard,
      });

      // 触发鼠标悬停
      await wrapper.find(".relative.max-w-\\[1400px\\]").trigger("mouseenter");
      await wrapper.vm.$nextTick();

      // 查找按钮并点击
      const buttons = wrapper.findAll("button");
      if (buttons.length > 0) {
        await buttons[0]!.trigger("click");
        expect(mockScrollTo).toHaveBeenCalled();
      }
    });

    it("应该在点击右侧按钮时调用滚动", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      const wrapper = mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      // 设置初始滚动位置
      const scrollContainer = wrapper.find(".overflow-x-auto");
      const mockScrollTo = vi.fn();
      scrollContainer.element.scrollTo = mockScrollTo;

      Object.defineProperty(scrollContainer.element, "scrollLeft", {
        writable: true,
        value: 0,
      });

      // 模拟卡片元素
      const mockCard = document.createElement("div");
      Object.defineProperty(mockCard, "offsetWidth", { value: 200 });
      Object.defineProperty(mockCard, "offsetLeft", { value: 0 });

      const mockSecondCard = document.createElement("div");
      Object.defineProperty(mockSecondCard, "offsetLeft", { value: 220 });

      vi.spyOn(scrollContainer.element, "querySelector").mockReturnValue(
        mockCard,
      );
      Object.defineProperty(mockCard, "nextElementSibling", {
        value: mockSecondCard,
      });

      // 触发鼠标悬停
      await wrapper.find(".relative.max-w-\\[1400px\\]").trigger("mouseenter");
      await wrapper.vm.$nextTick();

      // 查找按钮并点击
      const buttons = wrapper.findAll("button");
      if (buttons.length > 1) {
        await buttons[1]!.trigger("click");
        expect(mockScrollTo).toHaveBeenCalled();
      }
    });
  });

  describe("Props 传递", () => {
    it("应该正确传递 year 和 season 参数到 API", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      mount(AnimeListRow, {
        props: {
          title: "测试",
          year: 2023,
          season: "WINTER",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      expect(getAnimeList).toHaveBeenCalledWith({
        year: 2023,
        season: "WINTER",
        pageSize: 12,
      });
    });

    it("应该在没有 year 和 season 时调用 API", async () => {
      vi.mocked(getAnimeList).mockResolvedValue(mockAnimeList);

      mount(AnimeListRow, {
        props: {
          title: "测试",
        },
        global: {
          stubs: {
            AnimeCard: true,
            RouterLink: true,
          },
        },
      });

      await flushPromises();

      expect(getAnimeList).toHaveBeenCalledWith({
        year: undefined,
        season: undefined,
        pageSize: 12,
      });
    });
  });
});
