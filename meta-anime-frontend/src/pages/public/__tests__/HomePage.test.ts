import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import HomePage from "../HomePage.vue";

describe("HomePage.vue", () => {
  beforeEach(() => {
    // Mock current date to 2026-01-15 (Winter)
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-01-15"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("应该渲染 3 个 AnimeListRow 组件", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    expect(rows).toHaveLength(3);
  });

  it("应该渲染本季新番（冬季）", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    const currentSeasonRow = rows[0];

    expect(currentSeasonRow!.props("title")).toBe("2026年冬季新番");
    expect(currentSeasonRow!.props("year")).toBe(2026);
    expect(currentSeasonRow!.props("season")).toBe("WINTER");
  });

  it("应该渲染当季最高人气", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    const popularityRow = rows[1];

    expect(popularityRow!.props("title")).toBe("2026年冬季最高人气");
    expect(popularityRow!.props("year")).toBe(2026);
    expect(popularityRow!.props("season")).toBe("WINTER");
    expect(popularityRow!.props("sortBy")).toBe("POPULARITY");
  });

  it("应该渲染历史最高", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    const topRatedRow = rows[2];

    expect(topRatedRow!.props("title")).toBe("历史最高");
    expect(topRatedRow!.props("year")).toBeUndefined();
    expect(topRatedRow!.props("season")).toBeUndefined();
  });

  it("应该根据月份正确判断春季", () => {
    vi.setSystemTime(new Date("2026-04-15")); // April = Spring

    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    expect(rows[0]!.props("title")).toBe("2026年春季新番");
    expect(rows[0]!.props("season")).toBe("SPRING");
  });

  it("应该根据月份正确判断夏季", () => {
    vi.setSystemTime(new Date("2026-07-15")); // July = Summer

    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    expect(rows[0]!.props("title")).toBe("2026年夏季新番");
    expect(rows[0]!.props("season")).toBe("SUMMER");
  });

  it("应该根据月份正确判断秋季", () => {
    vi.setSystemTime(new Date("2026-10-15")); // October = Fall

    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const rows = wrapper.findAllComponents({ name: "AnimeListRow" });
    expect(rows[0]!.props("title")).toBe("2026年秋季新番");
    expect(rows[0]!.props("season")).toBe("FALL");
  });

  it("应该有正确的背景样式", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const container = wrapper.find(".bg-gray-50\\/50");
    expect(container.exists()).toBe(true);
  });

  it("应该有正确的间距", () => {
    const wrapper = mount(HomePage, {
      global: {
        stubs: {
          AnimeListRow: true,
        },
      },
    });

    const content = wrapper.find(".space-y-16");
    expect(content.exists()).toBe(true);
    expect(content.classes()).toContain("pt-5");
    expect(content.classes()).toContain("pb-20");
  });
});
