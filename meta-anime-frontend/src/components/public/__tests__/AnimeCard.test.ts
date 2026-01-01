import { mount } from "@vue/test-utils";
import type { Anime } from "@/types/anime";
import AnimeCard from "../AnimeCard.vue";

// 模拟测试数据
const mockAnime: Anime = {
  animeId: 1,
  title: {
    titleNative: "進撃の巨人",
    titleRomaji: "Shingeki no Kyojin",
    titleEn: "Attack on Titan",
    titleCn: "进击的巨人",
  },
  coverImage: "https://example.com/cover.jpg",
  averageScore: 8.5,
  mappings: [
    {
      mappingId: 1,
      sourcePlatform: "MyAnimeList",
      platformId: "16498",
      rawScore: 8.6,
    },
  ],
};

describe("AnimeCard.vue", () => {
  it("应该正确渲染动漫封面图片", () => {
    const wrapper = mount(AnimeCard, {
      props: {
        anime: mockAnime,
      },
    });

    const img = wrapper.find("img");
    expect(img.exists()).toBe(true);
    expect(img.attributes("src")).toBe(mockAnime.coverImage);
    expect(img.attributes("alt")).toBe(mockAnime.title.titleCn);
  });

  it("应该正确渲染动漫标题", () => {
    const wrapper = mount(AnimeCard, {
      props: {
        anime: mockAnime,
      },
    });

    const title = wrapper.find("h3");
    expect(title.exists()).toBe(true);
    expect(title.text()).toBe(mockAnime.title.titleCn);
  });

  it("应该显示平均评分", () => {
    const wrapper = mount(AnimeCard, {
      props: {
        anime: mockAnime,
      },
    });

    const score = wrapper.find(".absolute.bottom-1.right-3");
    expect(score.exists()).toBe(true);
    expect(score.text()).toBe("9"); // 8.5 四舍五入为 9
  });

  it("当没有中文标题时应该显示原生标题", () => {
    const animeWithoutCnTitle: Anime = {
      ...mockAnime,
      title: {
        ...mockAnime.title,
        titleCn: "",
      },
    };

    const wrapper = mount(AnimeCard, {
      props: {
        anime: animeWithoutCnTitle,
      },
    });

    const title = wrapper.find("h3");
    expect(title.text()).toBe(mockAnime.title.titleNative);
  });

  it("点击卡片应该切换翻转状态", async () => {
    const wrapper = mount(AnimeCard, {
      props: {
        anime: mockAnime,
      },
    });

    const card = wrapper.find(".relative.w-full.aspect-\\[3\\/4\\]");

    // 初始状态应该是翻转的（flipped = true）
    expect(wrapper.find(".absolute.inset-0.bg-black\\/20").exists()).toBe(
      false,
    );

    // 点击卡片
    await card.trigger("click");

    // 翻转后应该显示详情
    expect(wrapper.find(".absolute.inset-0.bg-black\\/20").exists()).toBe(true);
  });

  it("当没有平均分时不应该显示评分", () => {
    const animeWithoutScore: Anime = {
      ...mockAnime,
      averageScore: 0,
    };

    const wrapper = mount(AnimeCard, {
      props: {
        anime: animeWithoutScore,
      },
    });

    const score = wrapper.find(".absolute.bottom-1.right-3");
    expect(score.exists()).toBe(false);
  });
});
