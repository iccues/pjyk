import { mount } from "@vue/test-utils";
import { createRouter, createWebHistory } from "vue-router";
import Header from "../Header.vue";

// 创建一个简单的路由用于测试
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: { template: "<div>Home</div>" } },
    { path: "/list", name: "list", component: { template: "<div>List</div>" } },
  ],
});

describe("Header.vue", () => {
  it("应该正确渲染 logo 和标题", () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    const img = wrapper.find("img");
    expect(img.exists()).toBe(true);
    expect(img.attributes("alt")).toBe("Meta Anime Logo");

    expect(wrapper.text()).toContain("Meta Anime");
  });

  it("应该包含指向首页的链接", () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    const link = wrapper.findComponent({ name: "RouterLink" });
    expect(link.exists()).toBe(true);
    expect(link.props("to")).toBe("/");
  });

  it("初始状态应该是透明背景", () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    const header = wrapper.find("header");
    expect(header.classes()).toContain("bg-transparent");
    expect(header.classes()).not.toContain("bg-white/70");
  });

  it("滚动后应该改变样式", async () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
      attachTo: document.body,
    });

    // 模拟滚动
    Object.defineProperty(window, "scrollY", {
      writable: true,
      configurable: true,
      value: 50,
    });

    window.dispatchEvent(new Event("scroll"));
    await wrapper.vm.$nextTick();

    const header = wrapper.find("header");
    expect(header.classes()).toContain("bg-white/70");
    expect(header.classes()).toContain("backdrop-blur-md");
    expect(header.classes()).toContain("shadow-sm");

    wrapper.unmount();
  });

  it("滚动距离小于 10px 时应该保持透明", async () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
      attachTo: document.body,
    });

    // 模拟小距离滚动
    Object.defineProperty(window, "scrollY", {
      writable: true,
      configurable: true,
      value: 5,
    });

    window.dispatchEvent(new Event("scroll"));
    await wrapper.vm.$nextTick();

    const header = wrapper.find("header");
    expect(header.classes()).toContain("bg-transparent");
    expect(header.classes()).not.toContain("bg-white/70");

    wrapper.unmount();
  });

  it("组件卸载时应该移除滚动监听器", () => {
    const removeEventListenerSpy = vi.spyOn(window, "removeEventListener");

    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    wrapper.unmount();

    expect(removeEventListenerSpy).toHaveBeenCalledWith(
      "scroll",
      expect.any(Function),
    );
  });

  it("应该有正确的 z-index 确保在顶层", () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    const header = wrapper.find("header");
    expect(header.classes()).toContain("z-50");
    expect(header.classes()).toContain("fixed");
  });

  it("logo 应该有 hover 效果", () => {
    const wrapper = mount(Header, {
      global: {
        plugins: [router],
      },
    });

    const img = wrapper.find("img");
    expect(img.classes()).toContain("group-hover:shadow-indigo-500/30");
  });
});
