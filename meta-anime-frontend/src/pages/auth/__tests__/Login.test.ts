import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import Login from "../Login.vue";

// Mock auth module
vi.mock("@/auth/oidc", () => ({
  login: vi.fn(),
}));

import { login } from "@/auth/oidc";

describe("Login.vue", () => {
  it("应该渲染登录按钮", () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          ElButton: true,
        },
      },
    });

    const button = wrapper.findComponent({ name: "ElButton" });
    expect(button.exists()).toBe(true);
  });

  it("登录按钮应该显示'登录'文本", () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          ElButton: {
            template: "<button><slot /></button>",
          },
        },
      },
    });

    expect(wrapper.text()).toContain("登录");
  });

  it("点击按钮应该调用 login 函数", async () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          ElButton: {
            template: "<button @click=\"$emit('click')\"><slot /></button>",
          },
        },
      },
    });

    const button = wrapper.find("button");
    await button.trigger("click");

    expect(login).toHaveBeenCalled();
  });

  it("应该有正确的布局样式", () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          ElButton: true,
        },
      },
    });

    const container = wrapper.find(".min-h-screen");
    expect(container.exists()).toBe(true);
    expect(container.classes()).toContain("flex");
    expect(container.classes()).toContain("items-center");
    expect(container.classes()).toContain("justify-center");
    expect(container.classes()).toContain("bg-gray-50");
  });

  it("按钮应该居中显示", () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          ElButton: true,
        },
      },
    });

    const textCenter = wrapper.find(".text-center");
    expect(textCenter.exists()).toBe(true);
  });
});
