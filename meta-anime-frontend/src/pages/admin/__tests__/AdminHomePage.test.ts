import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";

// Mock auth module to prevent OIDC config fetch
vi.mock("@/auth/oidc", () => ({
  login: vi.fn(),
  oidcManager: {
    signinRedirectCallback: vi.fn(),
  },
}));

import AdminHomePage from "../AdminHomePage.vue";

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: "/admin", component: AdminHomePage },
    { path: "/admin/list", component: { template: "<div>List</div>" } },
  ],
});

describe("AdminHomePage.vue", () => {
  it("应该渲染 FetchDataCard 组件", () => {
    const wrapper = mount(AdminHomePage, {
      global: {
        plugins: [router],
        stubs: {
          ElCard: true,
          ElIcon: true,
          List: true,
          FetchDataCard: true,
        },
      },
    });

    const fetchDataCard = wrapper.findComponent({ name: "FetchDataCard" });
    expect(fetchDataCard.exists()).toBe(true);
  });

  it("应该使用 grid 布局", () => {
    const wrapper = mount(AdminHomePage, {
      global: {
        plugins: [router],
        stubs: {
          ElCard: true,
          ElIcon: true,
          List: true,
          FetchDataCard: true,
        },
      },
    });

    const container = wrapper.find(".grid");
    expect(container.exists()).toBe(true);
    expect(container.classes()).toContain("grid-cols-1");
    expect(container.classes()).toContain("md:grid-cols-2");
    expect(container.classes()).toContain("gap-6");
  });

  it("应该有正确的最大宽度和内边距", () => {
    const wrapper = mount(AdminHomePage, {
      global: {
        plugins: [router],
        stubs: {
          ElCard: true,
          ElIcon: true,
          List: true,
          FetchDataCard: true,
        },
      },
    });

    const container = wrapper.find(".max-w-\\[1200px\\]");
    expect(container.exists()).toBe(true);
    expect(container.classes()).toContain("p-6");
    expect(container.classes()).toContain("mx-auto");
  });
});
