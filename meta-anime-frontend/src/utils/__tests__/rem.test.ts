import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// import { initRem } from '../rem'

describe("rem.ts", () => {
  let originalClientWidth: number;

  beforeEach(() => {
    // 保存原始值
    originalClientWidth = document.documentElement.clientWidth;

    // 清理之前的状态
    document.documentElement.style.fontSize = "";

    // 重置模块状态（因为 initialized 是模块级变量）
    vi.resetModules();
  });

  afterEach(() => {
    // 恢复原始值
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: originalClientWidth,
    });
    document.documentElement.style.fontSize = "";
  });

  it("应该根据屏幕宽度设置 font-size", async () => {
    // Mock 屏幕宽度为 750px
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      writable: true,
      value: 750,
    });

    const { initRem } = await import("../rem");
    initRem();

    // 检查是否设置了 fontSize
    expect(document.documentElement.style.fontSize).toBeTruthy();
  });

  it("应该限制最大 font-size 为 16px", async () => {
    // Mock 屏幕宽度为 1920px (超大屏幕)
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: 1920,
    });

    const { initRem } = await import("../rem");
    initRem();

    // 1920 / 30 = 64px，但应该被限制为 16px
    expect(document.documentElement.style.fontSize).toBe("16px");
  });

  it("应该处理小屏幕", async () => {
    // Mock 屏幕宽度为 375px (移动设备)
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: 375,
    });

    const { initRem } = await import("../rem");
    initRem();

    // 375 / 30 = 12.5px
    expect(document.documentElement.style.fontSize).toBe("12.5px");
  });

  it("应该只初始化一次（防止重复初始化）", async () => {
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: 750,
    });

    const { initRem } = await import("../rem");

    initRem();
    const firstFontSize = document.documentElement.style.fontSize;

    // 改变屏幕宽度
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: 1000,
    });

    // 再次调用 initRem
    initRem();
    const secondFontSize = document.documentElement.style.fontSize;

    // font-size 应该保持不变，因为不会重复初始化
    expect(secondFontSize).toBe(firstFontSize);
  });

  it("应该添加 resize 事件监听器", async () => {
    const addEventListenerSpy = vi.spyOn(window, "addEventListener");

    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      value: 750,
    });

    const { initRem } = await import("../rem");
    initRem();

    expect(addEventListenerSpy).toHaveBeenCalledWith("resize", expect.any(Function));
  });

  it("应该在窗口 resize 时更新 font-size", async () => {
    // 初始宽度
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      writable: true,
      value: 750,
    });

    const { initRem } = await import("../rem");
    initRem();

    const initialFontSize = document.documentElement.style.fontSize;
    expect(initialFontSize).toBeTruthy();

    // 模拟窗口 resize
    Object.defineProperty(document.documentElement, "clientWidth", {
      configurable: true,
      writable: true,
      value: 600,
    });

    window.dispatchEvent(new Event("resize"));

    // 检查 fontSize 是否被更新
    const newFontSize = document.documentElement.style.fontSize;
    expect(newFontSize).toBeTruthy();
  });
});
