import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import FilterBar from "../FilterBar.vue";

const mockReviewStatusOptions = [
  { label: "全部", value: undefined },
  { label: "待审核", value: "PENDING" as const },
  { label: "已通过", value: "APPROVED" as const },
];

const mockYearOptions = [
  { label: "全部", value: undefined },
  { label: "2026年", value: 2026 },
  { label: "2025年", value: 2025 },
];

const mockSeasonOptions = [
  { label: "全部", value: undefined },
  { label: "春季", value: "SPRING" as const },
  { label: "夏季", value: "SUMMER" as const },
];

describe("FilterBar.vue", () => {
  it("应该渲染筛选文本", () => {
    const wrapper = mount(FilterBar, {
      props: {
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    expect(wrapper.text()).toContain("筛选");
  });

  it("应该渲染 3 个 ElSelect 组件", () => {
    const wrapper = mount(FilterBar, {
      props: {
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    const selects = wrapper.findAllComponents({ name: "ElSelect" });
    expect(selects).toHaveLength(3);
  });

  it("应该接收 selectedReviewStatus prop", () => {
    const wrapper = mount(FilterBar, {
      props: {
        selectedReviewStatus: "PENDING" as const,
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    expect(wrapper.props("selectedReviewStatus")).toBe("PENDING");
  });

  it("应该接收 selectedYear prop", () => {
    const wrapper = mount(FilterBar, {
      props: {
        selectedYear: 2026,
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    expect(wrapper.props("selectedYear")).toBe(2026);
  });

  it("应该接收 selectedSeason prop", () => {
    const wrapper = mount(FilterBar, {
      props: {
        selectedSeason: "SPRING" as const,
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    expect(wrapper.props("selectedSeason")).toBe("SPRING");
  });

  it("应该有正确的布局样式", () => {
    const wrapper = mount(FilterBar, {
      props: {
        reviewStatusOptions: mockReviewStatusOptions,
        yearOptions: mockYearOptions,
        seasonOptions: mockSeasonOptions,
      },
      global: {
        stubs: {
          ElIcon: true,
          ElSelect: true,
          ElOption: true,
          Filter: true,
        },
      },
    });

    const container = wrapper.find(".flex");
    expect(container.exists()).toBe(true);
    expect(container.classes()).toContain("flex-wrap");
    expect(container.classes()).toContain("items-center");
    expect(container.classes()).toContain("gap-2");
  });
});
