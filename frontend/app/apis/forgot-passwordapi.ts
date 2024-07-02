import axios from "axios";
import { baseURL } from "./api-config";

export const UpdatePassword = async (
  body: { newPassword: string },
  authToken: string | null,
) => {
  try {
    const result = await axios.post(
      `${baseURL}/api/auth/reset_password`,
      body,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${authToken}`,
        },
      },
    );
    return result.data;
  } catch (error) {
    return Promise.reject(error);
  }
};
