import axios from "axios";
import Api, { baseURL } from "./api-config";

interface SignUpRequestBody {
  username: string;
  workEmail: string;
  password: string;
  profileImage: string;
  coverImage: string;
  signUpType: string;
}

interface CreateOrganisationRequestBody {
  fullName: string;
  organisationRole: string;
  organisationName: string;
  phoneNumber: string;
}

export const GetVerifyUser = async () => {
  try {
    const result = await Api.get(`/api/users/is_verified`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const checkVerifyConnection = async (appToken: string) => {
  try {
    const result = await Api.get(
      `${baseURL}/api/app/verify-app?orgToken=${appToken}`,
    );
    return result.data;
  } catch (error) {
    return await Promise.reject(error);
  }
};

export const signUpCall = async (body: SignUpRequestBody) => {
  try {
    const result = await Api.post(`api/auth/signup`, body);
    return result;
  } catch (error) {
    return await Promise.reject(error);
  }
};

export const resendEmail = async (token: string | undefined | null) => {
  try {
    const result = await Api.get(`api/auth/send_email?token=${token}`);
    return result;
  } catch (error) {
    return await Promise.reject(error);
  }
};

export const createOrganization = async (
  body: CreateOrganisationRequestBody,
) => {
  try {
    const result = await Api.post(`api/organisations`, body);
    return result;
  } catch (error) {
    return await Promise.reject(error);
  }
};

export const forgotPassword = async (email: string) => {
  try {
    const result = await Api.get(`api/auth/forgot_password?workEmail=${email}`);
    console.log("result,", result);
    return result;
  } catch (error) {
    return await Promise.reject(error);
  }
};

export const getOrganizationData = async (token: string) => {
  try {
    const res = await axios.get(`${baseURL}api/organisations`, {
      headers: {
        // @ts-ignore
        Authorization: `Bearer ${token}`,
      },
    });
    return res;
  } catch (error) {
    return Promise.reject(error);
  }
};

export const updateUserDetails = async (body: {
  fullName: string;
  userOrganisationRole: string;
}) => {
  try {
    const result = await Api.patch(`/api/users/update-user`, body);
    return result;
  } catch (error) {
    return Promise.reject(error);
  }
};
