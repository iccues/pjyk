import { oidcManager } from "@/auth/oidc";
import type { Response } from "@/types/response";

interface RequestOptions {
  headers?: Record<string, string>;
  params?: Record<string, string | number | boolean | undefined | null>;
}

/**
 * 通用请求处理函数
 */
async function request<T>(
  url: string,
  method: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<T> {
  // 处理 URL 参数
  let finalUrl = url;
  if (options?.params) {
    const params = new URLSearchParams();
    Object.entries(options.params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });
    const queryString = params.toString();
    if (queryString) {
      finalUrl = `${url}?${queryString}`;
    }
  }

  const user = await oidcManager.getUser();

  // 构建请求配置
  const config: RequestInit = {
    method,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${user?.id_token}`,
      ...options?.headers,
    },
  };

  // 添加请求体（如果有）
  if (body) {
    config.body = JSON.stringify(body);
  }

  // 发送请求
  const response = await fetch(finalUrl, config);

  // 检查 HTTP 状态
  if (!response.ok) {
    try {
      const result: Response<T> = await response.json();
      // 如果能解析且有业务错误信息，使用业务错误
      if (!result.success && result.message) {
        throw new Error(result.message);
      }
    } catch (e) {
      // 解析失败或没有业务错误信息，使用HTTP错误
      if (e instanceof Error && e.message !== `HTTP错误: ${response.status}`) {
        throw e; // 重新抛出已解析的业务错误
      }
    }
    // 兜底：HTTP错误
    throw new Error(`HTTP错误: ${response.status} ${response.statusText}`);
  }

  // 解析响应
  const result: Response<T> = await response.json();

  // 检查业务状态
  if (!result.success) {
    throw new Error(result.message);
  }

  return result.data;
}

/**
 * GET 请求
 */
export async function get<T>(url: string, options?: RequestOptions): Promise<T> {
  return request<T>(url, "GET", undefined, options);
}

/**
 * POST 请求
 */
export async function post<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
  return request<T>(url, "POST", body, options);
}

/**
 * PUT 请求
 */
export async function put<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
  return request<T>(url, "PUT", body, options);
}

/**
 * DELETE 请求
 */
export async function del<T>(url: string, options?: RequestOptions): Promise<T> {
  return request<T>(url, "DELETE", undefined, options);
}

/**
 * PATCH 请求
 */
export async function patch<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
  return request<T>(url, "PATCH", body, options);
}
