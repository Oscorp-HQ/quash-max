import axios, { AxiosRequestHeaders } from "axios";
import { getSession, signOut } from "next-auth/react";

export const baseURL = process.env.NEXT_PUBLIC_BASE_URL;
export const refreshBaseURL = process.env.NEXT_PUBLIC_REFRESH_BASE_URL;

// Custom Axios instance
const api = axios.create({
  baseURL: baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  async (config) => {
    const session = await getSession();
    if (session?.data?.token) {
      config.headers = {
        Authorization: `Bearer ${session?.data?.token}`,
        Accept: "application/json",
        // "Content-Type": "application/x-www-form-urlencoded",
      } as AxiosRequestHeaders;
    } else {
      config.headers = {
        Accept: "application/json",
      } as AxiosRequestHeaders;
    }

    return config;
  },
  (error) => {
    Promise.reject(error);
  },
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    if (error.response?.status === 401) {
      // Handle global sign out here
      console.log("Session expired or invalid token, signing out...");
      await signOut({ redirect: false });
      window.location.assign("/");
    }
    return Promise.reject(error);
  },
);

export default api;
