import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";

// Mock OIDC manager - must be before component import
vi.mock("@/auth/oidc", () => ({
  oidcManager: {
    signinRedirectCallback: vi.fn(),
  },
}));

import { oidcManager } from "@/auth/oidc";
import Callback from "../Callback.vue";

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: "/auth/callback", component: Callback },
    { path: "/admin", component: { template: "<div>Admin</div>" } },
  ],
});

describe("Callback.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("应该显示加载中文本", () => {
    const wrapper = mount(Callback, {
      global: {
        plugins: [router],
      },
    });

    expect(wrapper.text()).toContain("加载中");
  });

  it("挂载时应该调用 signinRedirectCallback", async () => {
    vi.mocked(oidcManager.signinRedirectCallback).mockResolvedValue(
      undefined as any,
    );

    mount(Callback, {
      global: {
        plugins: [router],
      },
    });

    await flushPromises();

    expect(oidcManager.signinRedirectCallback).toHaveBeenCalled();
  });

  it("认证成功后应该跳转到 /admin", async () => {
    vi.mocked(oidcManager.signinRedirectCallback).mockResolvedValue(
      undefined as any,
    );
    const replaceSpy = vi.spyOn(router, "replace");

    mount(Callback, {
      global: {
        plugins: [router],
      },
    });

    await flushPromises();

    expect(replaceSpy).toHaveBeenCalledWith("/admin");
  });

  it("认证失败时应该记录错误", async () => {
    const consoleErrorSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});
    const error = new Error("Auth failed");
    vi.mocked(oidcManager.signinRedirectCallback).mockRejectedValue(error);

    mount(Callback, {
      global: {
        plugins: [router],
      },
    });

    await flushPromises();

    expect(consoleErrorSpy).toHaveBeenCalledWith("Auth callback failed", error);

    consoleErrorSpy.mockRestore();
  });
});
