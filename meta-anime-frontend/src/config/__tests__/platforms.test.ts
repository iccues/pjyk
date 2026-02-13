import { describe, expect, it, vi } from "vitest";
import { getAllPlatformConfigs, getAllPlatforms, getPlatformConfig } from "../platforms";

// Mock logo imports
vi.mock("@/assets/logo/anilist.png", () => ({ default: "/mock/anilist.png" }));
vi.mock("@/assets/logo/bangumi.png", () => ({ default: "/mock/bangumi.png" }));
vi.mock("@/assets/logo/myanimelist.png", () => ({
  default: "/mock/myanimelist.png",
}));

describe("platforms.ts", () => {
  describe("getPlatformConfig", () => {
    it("应该返回 MyAnimeList 配置", () => {
      const config = getPlatformConfig("MyAnimeList");

      expect(config.name).toBe("MyAnimeList");
      expect(config.logo).toBeDefined();
      expect(config.baseUrl).toBe("https://myanimelist.net");
      expect(config.getAnimeUrl).toBeDefined();
    });

    it("应该返回 Bangumi 配置", () => {
      const config = getPlatformConfig("Bangumi");

      expect(config.name).toBe("Bangumi");
      expect(config.logo).toBeDefined();
      expect(config.baseUrl).toBe("https://bgm.tv");
      expect(config.getAnimeUrl).toBeDefined();
    });

    it("应该返回 AniList 配置", () => {
      const config = getPlatformConfig("AniList");

      expect(config.name).toBe("AniList");
      expect(config.logo).toBeDefined();
      expect(config.baseUrl).toBe("https://anilist.co");
      expect(config.getAnimeUrl).toBeDefined();
    });

    it("应该正确生成 MyAnimeList URL", () => {
      const config = getPlatformConfig("MyAnimeList");
      const url = config.getAnimeUrl?.("16498");

      expect(url).toBe("https://myanimelist.net/anime/16498");
    });

    it("应该正确生成 Bangumi URL", () => {
      const config = getPlatformConfig("Bangumi");
      const url = config.getAnimeUrl?.("123456");

      expect(url).toBe("https://bgm.tv/subject/123456");
    });

    it("应该正确生成 AniList URL", () => {
      const config = getPlatformConfig("AniList");
      const url = config.getAnimeUrl?.("98765");

      expect(url).toBe("https://anilist.co/anime/98765");
    });

    it("应该处理未知平台（返回降级配置）", () => {
      const config = getPlatformConfig("UnknownPlatform");

      expect(config.name).toBe("UnknownPlatform");
      expect(config.logo).toBeUndefined();
      expect(config.baseUrl).toBeUndefined();
      expect(config.getAnimeUrl).toBeUndefined();
    });

    it("应该处理空字符串", () => {
      const config = getPlatformConfig("");

      expect(config.name).toBe("");
      expect(config.logo).toBeUndefined();
    });
  });

  describe("getAllPlatforms", () => {
    it("应该返回所有支持的平台名称", () => {
      const platforms = getAllPlatforms();

      expect(platforms).toHaveLength(3);
      expect(platforms).toContain("AniList");
      expect(platforms).toContain("Bangumi");
      expect(platforms).toContain("MyAnimeList");
    });

    it("返回的数组应该是字符串数组", () => {
      const platforms = getAllPlatforms();

      platforms.forEach((platform) => {
        expect(typeof platform).toBe("string");
      });
    });
  });

  describe("getAllPlatformConfigs", () => {
    it("应该返回所有平台配置", () => {
      const configs = getAllPlatformConfigs();

      expect(configs).toHaveLength(3);
    });

    it("每个配置都应该包含必要的字段", () => {
      const configs = getAllPlatformConfigs();

      configs.forEach((config) => {
        expect(config.name).toBeDefined();
        expect(typeof config.name).toBe("string");
        expect(config.logo).toBeDefined();
        expect(config.baseUrl).toBeDefined();
        expect(config.getAnimeUrl).toBeDefined();
        expect(typeof config.getAnimeUrl).toBe("function");
      });
    });

    it("应该包含所有已知平台的配置", () => {
      const configs = getAllPlatformConfigs();
      const names = configs.map((c) => c.name);

      expect(names).toContain("AniList");
      expect(names).toContain("Bangumi");
      expect(names).toContain("MyAnimeList");
    });
  });
});
