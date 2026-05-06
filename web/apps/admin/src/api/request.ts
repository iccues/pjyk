import axios from "axios";

import { oidcManager } from "@/auth/oidc";
import type { Response } from "@/types/response";

// 响应拦截器：成功解包和业务错误处理
const onResponseFulfilled = (response: any) => {
  const result: Response<any> = response.data;
  if (!result.success) {
    return Promise.reject(new Error(result.message || "请求失败"));
  }
  return { ...response, data: result.data };
};

// 响应拦截器：HTTP 错误处理
const onResponseRejected = (error: any) => {
  const message = error.response?.data?.message || error.message || "网络请求异常";
  return Promise.reject(new Error(message));
};

// 1. 需要鉴权的实例 (Admin)
const adminClient = axios.create();

adminClient.interceptors.request.use(async (config) => {
  const user = await oidcManager.getUser();
  if (user?.id_token) {
    config.headers.Authorization = `Bearer ${user.id_token}`;
  }
  return config;
});

adminClient.interceptors.response.use(onResponseFulfilled, onResponseRejected);

// 2. 公开接口的实例 (Public，无鉴权)
export const publicClient = axios.create();
publicClient.interceptors.response.use(onResponseFulfilled, onResponseRejected);

export default adminClient;
