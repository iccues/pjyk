import { mount } from "@vue/test-utils";
import type { Mapping } from "@/types/anime";
import AnimeScoreItem from "../AnimeScoreItem.vue";

const mockMapping: Mapping = {
  mappingId: 1,
  sourcePlatform: "MyAnimeList",
  platformId: "16498",
  rawScore: 8.6,
};

describe("AnimeScoreItem.vue", () => {
  it("应该正确渲染平台名称", () => {
    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: mockMapping,
      },
    });

    expect(wrapper.text()).toContain(mockMapping.sourcePlatform);
  });

  it("应该正确渲染评分", () => {
    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: mockMapping,
      },
    });

    expect(wrapper.text()).toContain("8.6");
  });

  it("应该处理不同的平台名称", () => {
    const bangumiMapping: Mapping = {
      ...mockMapping,
      sourcePlatform: "Bangumi",
      rawScore: 7.8,
    };

    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: bangumiMapping,
      },
    });

    expect(wrapper.text()).toContain("Bangumi");
    expect(wrapper.text()).toContain("7.8");
  });

  it("应该显示平台 logo", () => {
    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: mockMapping,
      },
    });

    const img = wrapper.find("img");
    expect(img.exists()).toBe(true);
    expect(img.attributes("alt")).toBe("MyAnimeList");
  });

  it("应该渲染为可点击的链接", () => {
    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: mockMapping,
      },
    });

    const link = wrapper.find("a");
    expect(link.exists()).toBe(true);
    expect(link.attributes("href")).toContain("myanimelist.net/anime/16498");
    expect(link.attributes("target")).toBe("_blank");
    expect(link.attributes("rel")).toBe("noopener noreferrer");
  });

  it("应该正确生成 Bangumi 链接", () => {
    const bangumiMapping: Mapping = {
      mappingId: 2,
      sourcePlatform: "Bangumi",
      platformId: "123456",
      rawScore: 7.8,
    };

    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: bangumiMapping,
      },
    });

    const link = wrapper.find("a");
    expect(link.attributes("href")).toContain("bgm.tv/subject/123456");
  });

  it("应该正确生成 AniList 链接", () => {
    const anilistMapping: Mapping = {
      mappingId: 3,
      sourcePlatform: "AniList",
      platformId: "98765",
      rawScore: 9.2,
    };

    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: anilistMapping,
      },
    });

    const link = wrapper.find("a");
    expect(link.attributes("href")).toContain("anilist.co/anime/98765");
  });

  it("应该正确格式化小数评分（保留一位小数）", () => {
    const decimalMapping: Mapping = {
      mappingId: 4,
      sourcePlatform: "MyAnimeList",
      platformId: "12345",
      rawScore: 9.123456,
    };

    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: decimalMapping,
      },
    });

    // toFixed(1) 应该显示 9.1
    expect(wrapper.text()).toContain("9.1");
  });

  it("应该处理未知平台（降级显示）", () => {
    const unknownMapping: Mapping = {
      mappingId: 5,
      sourcePlatform: "UnknownPlatform",
      platformId: "999",
      rawScore: 6.5,
    };

    const wrapper = mount(AnimeScoreItem, {
      props: {
        mapping: unknownMapping,
      },
    });

    // 应该显示平台名称，即使没有配置
    expect(wrapper.text()).toContain("UnknownPlatform");
    expect(wrapper.text()).toContain("6.5");
  });
});
