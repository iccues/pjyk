import { describe, expect, it } from "vitest";
import { REVIEW_STATUS_NAME_MAP, SEASON_NAME_MAP } from "../enum-maps";

describe("enum-maps.ts", () => {
  describe("SEASON_NAME_MAP", () => {
    it("应该包含所有季度的映射", () => {
      expect(SEASON_NAME_MAP).toHaveProperty("WINTER");
      expect(SEASON_NAME_MAP).toHaveProperty("SPRING");
      expect(SEASON_NAME_MAP).toHaveProperty("SUMMER");
      expect(SEASON_NAME_MAP).toHaveProperty("FALL");
    });

    it("应该正确映射季度到中文名称", () => {
      expect(SEASON_NAME_MAP.WINTER).toBe("冬季");
      expect(SEASON_NAME_MAP.SPRING).toBe("春季");
      expect(SEASON_NAME_MAP.SUMMER).toBe("夏季");
      expect(SEASON_NAME_MAP.FALL).toBe("秋季");
    });

    it("应该包含正确数量的映射", () => {
      const keys = Object.keys(SEASON_NAME_MAP);
      expect(keys).toHaveLength(4);
    });

    it("所有值都应该是字符串", () => {
      Object.values(SEASON_NAME_MAP).forEach((value) => {
        expect(typeof value).toBe("string");
      });
    });
  });

  describe("REVIEW_STATUS_NAME_MAP", () => {
    it("应该包含所有审核状态的映射", () => {
      expect(REVIEW_STATUS_NAME_MAP).toHaveProperty("PENDING");
      expect(REVIEW_STATUS_NAME_MAP).toHaveProperty("APPROVED");
      expect(REVIEW_STATUS_NAME_MAP).toHaveProperty("REJECTED");
    });

    it("应该正确映射审核状态到中文名称", () => {
      expect(REVIEW_STATUS_NAME_MAP.PENDING).toBe("待审核");
      expect(REVIEW_STATUS_NAME_MAP.APPROVED).toBe("已通过");
      expect(REVIEW_STATUS_NAME_MAP.REJECTED).toBe("已拒绝");
    });

    it("应该包含正确数量的映射", () => {
      const keys = Object.keys(REVIEW_STATUS_NAME_MAP);
      expect(keys).toHaveLength(3);
    });

    it("所有值都应该是字符串", () => {
      Object.values(REVIEW_STATUS_NAME_MAP).forEach((value) => {
        expect(typeof value).toBe("string");
      });
    });
  });
});
