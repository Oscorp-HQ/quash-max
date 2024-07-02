import axios from "axios";
import { getSession, signOut } from "next-auth/react";

export const baseURL = process.env.NEXT_PUBLIC_BASE_URL || "";

const ApiForm = () => {
  const instance = axios.create({
    baseURL: baseURL,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  instance.interceptors.request.use(async (request) => {
    const session = await getSession();

    if (session) {
      request.headers.common = {
        Authorization: `${session?.data?.token}`,
      };
    }
    return request;
  });

  instance.interceptors.response.use(
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

  return instance;
};

export default ApiForm();
