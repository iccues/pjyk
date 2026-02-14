import { describe, expect, it } from "vitest";
import { REVIEW_STATUS_OPTIONS, SEASON_OPTIONS, SEASON_OPTIONS_REQUIRED } from "../ui-options";

describe("ui-options.ts", () => {
  describe("SEASON_OPTIONS", () => {
    it('应该包含"全部"选项', () => {
      const allOption = SEASON_OPTIONS[0]!;
      expect(allOption.label).toBe("全部");
      expect(allOption.value).toBeUndefined();
    });

    it("应该包含所有季度选项", () => {
      expect(SEASON_OPTIONS).toHaveLength(5); // "全部" + 4 个季度

      const labels = SEASON_OPTIONS.map((opt) => opt.label);
      expect(labels).toContain("冬季");
      expect(labels).toContain("春季");
      expect(labels).toContain("夏季");
      expect(labels).toContain("秋季");
    });

    it("应该正确映射季度值", () => {
      const winterOption = SEASON_OPTIONS.find((opt) => opt.label === "冬季");
      const springOption = SEASON_OPTIONS.find((opt) => opt.label === "春季");
      const summerOption = SEASON_OPTIONS.find((opt) => opt.label === "夏季");
      const fallOption = SEASON_OPTIONS.find((opt) => opt.label === "秋季");

      expect(winterOption?.value).toBe("WINTER");
      expect(springOption?.value).toBe("SPRING");
      expect(summerOption?.value).toBe("SUMMER");
      expect(fallOption?.value).toBe("FALL");
    });

    it("每个选项都应该有 label 和 value 属性", () => {
      SEASON_OPTIONS.forEach((option) => {
        expect(option).toHaveProperty("label");
        expect(option).toHaveProperty("value");
        expect(typeof option.label).toBe("string");
      });
    });
  });

  describe("SEASON_OPTIONS_REQUIRED", () => {
    it('不应该包含"全部"选项', () => {
      const hasAllOption = SEASON_OPTIONS_REQUIRED.some((opt) => opt.label === "全部");
      expect(hasAllOption).toBe(false);
    });

    it("应该只包含 4 个季度选项", () => {
      expect(SEASON_OPTIONS_REQUIRED).toHaveLength(4);
    });

    it("应该包含所有季度", () => {
      const labels = SEASON_OPTIONS_REQUIRED.map((opt) => opt.label);
      expect(labels).toContain("冬季");
      expect(labels).toContain("春季");
      expect(labels).toContain("夏季");
      expect(labels).toContain("秋季");
    });

    it("所有值都应该是有效的 Season 类型", () => {
      const values = SEASON_OPTIONS_REQUIRED.map((opt) => opt.value);
      expect(values).toContain("WINTER");
      expect(values).toContain("SPRING");
      expect(values).toContain("SUMMER");
      expect(values).toContain("FALL");
    });

    it("所有值都不应该是 undefined", () => {
      SEASON_OPTIONS_REQUIRED.forEach((option) => {
        expect(option.value).toBeDefined();
        expect(option.value).not.toBeUndefined();
      });
    });
  });

  describe("REVIEW_STATUS_OPTIONS", () => {
    it('应该包含"全部"选项', () => {
      const allOption = REVIEW_STATUS_OPTIONS[0]!;
      expect(allOption.label).toBe("全部");
      expect(allOption.value).toBeUndefined();
    });

    it("应该包含所有审核状态选项", () => {
      expect(REVIEW_STATUS_OPTIONS).toHaveLength(4); // "全部" + 3 个状态

      const labels = REVIEW_STATUS_OPTIONS.map((opt) => opt.label);
      expect(labels).toContain("待审核");
      expect(labels).toContain("已通过");
      expect(labels).toContain("已拒绝");
    });

    it("应该正确映射审核状态值", () => {
      const pendingOption = REVIEW_STATUS_OPTIONS.find((opt) => opt.label === "待审核");
      const approvedOption = REVIEW_STATUS_OPTIONS.find((opt) => opt.label === "已通过");
      const rejectedOption = REVIEW_STATUS_OPTIONS.find((opt) => opt.label === "已拒绝");

      expect(pendingOption?.value).toBe("PENDING");
      expect(approvedOption?.value).toBe("APPROVED");
      expect(rejectedOption?.value).toBe("REJECTED");
    });

    it("每个选项都应该有 label 和 value 属性", () => {
      REVIEW_STATUS_OPTIONS.forEach((option) => {
        expect(option).toHaveProperty("label");
        expect(option).toHaveProperty("value");
        expect(typeof option.label).toBe("string");
      });
    });
  });

  describe("选项一致性", () => {
    it("SEASON_OPTIONS 和 SEASON_OPTIONS_REQUIRED 的季度顺序应该一致", () => {
      const optionsSeasons = SEASON_OPTIONS.slice(1).map((opt) => opt.value);
      const requiredSeasons = SEASON_OPTIONS_REQUIRED.map((opt) => opt.value);

      expect(optionsSeasons).toEqual(requiredSeasons);
    });

    it("SEASON_OPTIONS 和 SEASON_OPTIONS_REQUIRED 的标签应该一致", () => {
      const optionsLabels = SEASON_OPTIONS.slice(1).map((opt) => opt.label);
      const requiredLabels = SEASON_OPTIONS_REQUIRED.map((opt) => opt.label);

      expect(optionsLabels).toEqual(requiredLabels);
    });
  });
});
