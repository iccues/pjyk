import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import AdminLayout from "../AdminLayout.vue";

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: "/", component: { template: "<div>Home</div>" } },
    { path: "/admin", component: { template: "<div>Admin Home</div>" } },
    { path: "/admin/auth", component: { template: "<div>Auth</div>" } },
  ],
});

describe("AdminLayout.vue", () => {
  it("应该渲染 header", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const header = wrapper.find("header");
    expect(header.exists()).toBe(true);
  });

  it('应该渲染 "Meta Anime" 链接', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const links = wrapper.findAllComponents({ name: "RouterLink" });
    const metaAnimeLink = links.find((link) =>
      link.text().includes("Meta Anime"),
    );

    expect(metaAnimeLink).toBeDefined();
    expect(metaAnimeLink?.props("to")).toBe("/");
  });

  it('应该渲染 "Admin Panel" 链接', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const links = wrapper.findAllComponents({ name: "RouterLink" });
    const adminPanelLink = links.find((link) =>
      link.text().includes("Admin Panel"),
    );

    expect(adminPanelLink).toBeDefined();
    expect(adminPanelLink?.props("to")).toBe("/admin");
  });

  it("应该渲染用户信息链接", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const links = wrapper.findAllComponents({ name: "RouterLink" });
    const authLink = links.find((link) => link.props("to") === "/admin/auth");

    expect(authLink).toBeDefined();
  });

  it("应该渲染 router-view", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    expect(wrapper.findComponent({ name: "RouterView" }).exists()).toBe(true);
  });

  it("应该使用 flex 布局", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const container = wrapper.find(".h-screen");
    expect(container.exists()).toBe(true);
    expect(container.classes()).toContain("flex");
    expect(container.classes()).toContain("flex-col");
  });

  it("header 应该有正确的样式", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const header = wrapper.find("header");
    expect(header.classes()).toContain("flex");
    expect(header.classes()).toContain("justify-between");
    expect(header.classes()).toContain("bg-white");
    expect(header.classes()).toContain("shadow-sm");
  });

  it("主内容区域应该可以滚动", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const mainContent = wrapper.find(".flex-1");
    expect(mainContent.exists()).toBe(true);
    expect(mainContent.classes()).toContain("overflow-hidden");
  });

  it("应该有 3 个导航链接", () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [router],
        stubs: {
          RouterView: true,
          ElIcon: true,
          User: true,
        },
      },
    });

    const links = wrapper.findAllComponents({ name: "RouterLink" });
    expect(links).toHaveLength(3);
  });
});
