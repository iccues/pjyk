import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import PublicLayout from "../PublicLayout.vue";

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: "/", component: { template: "<div>Home Page</div>" } },
    { path: "/list", component: { template: "<div>List Page</div>" } },
  ],
});

describe("PublicLayout.vue", () => {
  it("应该渲染 Header 组件", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    expect(wrapper.findComponent({ name: "Header" }).exists()).toBe(true);
  });

  it("应该渲染 router-view", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    expect(wrapper.findComponent({ name: "RouterView" }).exists()).toBe(true);
  });

  it("应该渲染 footer", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    const footer = wrapper.find("footer");
    expect(footer.exists()).toBe(true);
    expect(footer.text()).toContain("2025 Meta Anime");
    expect(footer.text()).toContain("All rights reserved");
  });

  it("main 元素应该有正确的 padding-top", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    const main = wrapper.find("main");
    expect(main.exists()).toBe(true);
    expect(main.classes()).toContain("pt-20");
  });

  it("footer 应该有正确的样式类", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    const footer = wrapper.find("footer");
    expect(footer.classes()).toContain("py-6");
    expect(footer.classes()).toContain("text-center");
    expect(footer.classes()).toContain("text-sm");
    expect(footer.classes()).toContain("text-gray-500");
  });

  it("应该有正确的布局结构", () => {
    const wrapper = mount(PublicLayout, {
      global: {
        plugins: [router],
        stubs: {
          Header: true,
          RouterView: true,
        },
      },
    });

    // 检查布局顺序：Header -> main -> footer
    const html = wrapper.html();
    const headerIndex = html.indexOf("header-stub");
    const mainIndex = html.indexOf("<main");
    const footerIndex = html.indexOf("<footer");

    expect(headerIndex).toBeLessThan(mainIndex);
    expect(mainIndex).toBeLessThan(footerIndex);
  });
});
