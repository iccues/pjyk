import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  formatDate,
  generateYearOptions,
  isValidDateFormat,
} from "../dateUtils";

describe("dateUtils", () => {
  describe("generateYearOptions", () => {
    beforeEach(() => {
      // Mock 当前日期为 2026-01-01
      vi.useFakeTimers();
      vi.setSystemTime(new Date("2026-01-01"));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("应该生成默认 10 年的年份选项", () => {
      const options = generateYearOptions();

      // 应该包含 "全部" + 11 个年份（2026 到 2016）
      expect(options).toHaveLength(12);
      expect(options[0]).toEqual({ label: "全部", value: undefined });
      expect(options[1]).toEqual({ label: "2026年", value: 2026 });
      expect(options[11]).toEqual({ label: "2016年", value: 2016 });
    });

    it("应该支持自定义往前推的年数", () => {
      const options = generateYearOptions(5);

      // 应该包含 "全部" + 6 个年份（2026 到 2021）
      expect(options).toHaveLength(7);
      expect(options[0]).toEqual({ label: "全部", value: undefined });
      expect(options[1]).toEqual({ label: "2026年", value: 2026 });
      expect(options[6]).toEqual({ label: "2021年", value: 2021 });
    });

    it("应该按降序排列年份", () => {
      const options = generateYearOptions(3);

      // 跳过第一个 "全部" 选项
      const years = options.slice(1).map((opt) => opt.value);
      expect(years).toEqual([2026, 2025, 2024, 2023]);
    });

    it("应该处理 0 年的情况", () => {
      const options = generateYearOptions(0);

      // 应该只包含 "全部" + 当前年份
      expect(options).toHaveLength(2);
      expect(options[0]).toEqual({ label: "全部", value: undefined });
      expect(options[1]).toEqual({ label: "2026年", value: 2026 });
    });
  });

  describe("formatDate", () => {
    it("应该正确格式化 Date 对象", () => {
      const date = new Date("2026-03-15");
      expect(formatDate(date)).toBe("2026-03-15");
    });

    it("应该正确格式化日期字符串", () => {
      expect(formatDate("2026-12-25")).toBe("2026-12-25");
    });

    it("应该正确补零（月份）", () => {
      const date = new Date("2026-01-15");
      expect(formatDate(date)).toBe("2026-01-15");
    });

    it("应该正确补零（日期）", () => {
      const date = new Date("2026-12-05");
      expect(formatDate(date)).toBe("2026-12-05");
    });

    it("应该处理闰年", () => {
      const date = new Date("2024-02-29");
      expect(formatDate(date)).toBe("2024-02-29");
    });

    it("应该处理不同格式的输入字符串", () => {
      expect(formatDate("2026/06/15")).toBe("2026-06-15");
      expect(formatDate("June 15, 2026")).toBe("2026-06-15");
    });
  });

  describe("isValidDateFormat", () => {
    it("应该验证正确的日期格式", () => {
      expect(isValidDateFormat("2026-01-01")).toBe(true);
      expect(isValidDateFormat("2026-12-31")).toBe(true);
      expect(isValidDateFormat("1999-05-20")).toBe(true);
    });

    it("应该拒绝错误的格式", () => {
      expect(isValidDateFormat("2026/01/01")).toBe(false);
      expect(isValidDateFormat("01-01-2026")).toBe(false);
      expect(isValidDateFormat("2026-1-1")).toBe(false);
      expect(isValidDateFormat("26-01-01")).toBe(false);
    });

    it("应该拒绝无效的日期字符串", () => {
      expect(isValidDateFormat("not a date")).toBe(false);
      // 注意：isValidDateFormat 只验证格式，不验证日期的有效性
      // 所以 2026-13-01 会通过格式验证（虽然月份无效）
      expect(isValidDateFormat("2026-13-01")).toBe(true); // 格式正确
      expect(isValidDateFormat("")).toBe(false);
    });

    it("应该拒绝包含额外字符的字符串", () => {
      expect(isValidDateFormat("2026-01-01 12:00:00")).toBe(false);
      expect(isValidDateFormat("Date: 2026-01-01")).toBe(false);
    });
  });
});
