import CredentialsProvider from "next-auth/providers/credentials";
import GoogleProvider from "next-auth/providers/google";
import axios from "axios";
import { baseURL } from "@/app/apis/api-config";
import { signOut } from "next-auth/react";

export const refreshBaseURL = process.env.NEXT_PUBLIC_REFRESH_BASE_URL;
const googleClientId = process.env.GOOGLE_CLIENT_ID as string;
const googleClientSecret = process.env.GOOGLE_CLIENT_SECRET as string;

// if (!googleClientId || !googleClientSecret) {
//   throw new Error("Missing Google client ID or secret");
// }

export const fetchFreshTokens = async (refreshAccessToken: any) => {
  console.log("Attempting to refresh tokens with provided refresh token.");
  try {
    const response = await axios.get(
      `${refreshBaseURL}api/auth/get-refresh-token`,
      {
        params: {
          refreshToken: refreshAccessToken,
        },
      },
    );

    // Check if the response is valid and contains the required tokens
    if (response.status === 200 && response.data && response.data.data) {
      const { token, refreshToken } = response.data.data;
      if (token && refreshToken) {
        console.log("Tokens refreshed successfully.");
        return { token, refreshToken };
      } else {
        throw new Error("Response missing tokens");
      }
    } else {
      throw new Error("Failed to refresh tokens due to invalid response");
    }
  } catch (error: any) {
    console.error("Error refreshing tokens:", error);
    await signOut();
    throw new Error(
      `Error fetching new tokens: ${error.message || error.toString()}`,
    );
  }
};

const isTokenExpired = (token: string) => {
  try {
    const parts = token.split(".");
    const payload = JSON.parse(atob(parts[1]));
    const expirationTime = payload.exp * 1000;
    return Date.now() > expirationTime;
  } catch (error) {
    console.error("Error checking token expiration:", error);
    return false;
  }
};

export const authOptions = {
  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: {
          label: "Email",
          type: "email",
          placeholder: "johndoe@email.com",
        },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials) {
        try {
          const res = await getUserData(credentials);
          return {
            ...credentials,
            data: res,
          } as any;
        } catch (error: any) {
          return Promise.reject(
            new Error(
              JSON.stringify({ errors: error.response.data, status: false }),
            ),
          );
        }
      },
    }),
    GoogleProvider({
      clientId: googleClientId,
      clientSecret: googleClientSecret,
    }),
  ],
  secret: process.env.NEXTAUTH_SECRET,
  callbacks: {
    async signIn({ account }: any) {
      if (account.provider === "google") {
        try {
          return true;
        } catch (error: any) {
          return Promise.reject(
            new Error(
              JSON.stringify({ errors: error.response.data, status: false }),
            ),
          );
        }
      }

      return true;
    },
    jwt: async ({ token, user, trigger, session, account }: any) => {
      // Attempt to refresh token if it's expired
      const accessToken = token?.data?.token;
      if (accessToken && isTokenExpired(accessToken)) {
        try {
          const response = await fetchFreshTokens(token?.data?.refreshToken);
          if (!response || !response.token || !response.refreshToken) {
            throw new Error("Failed to refresh tokens");
          }
          return {
            ...token,
            data: {
              ...token.data,
              token: response.token,
              refreshToken: response.refreshToken,
            },
          };
        } catch (error) {
          console.error("JWT refresh token error:", error);
        }
      }
      if (trigger === "update") {
        return {
          ...token,

          data: {
            ...token.data,
            isVerified: session.user.isVerified,
            shouldNavigateToDashboard: session.user.shouldNavigateToDashboard,
            fullName: session.user.fullName,
          },
        };
      }
      // If new user data is present (e.g., during sign-in or token refresh), update the token's data
      if (account?.provider === "google") {
        const code: string = await account.access_token;

        try {
          const res = await axios.get(
            `${baseURL}/api/auth/google/authorize?code=${code}`,
            {
              headers: {
                "Content-Type": "application/json",
              },
            },
          );

          const response = {
            shouldNavigateToDashboard:
              res?.data.data.should_navigate_to_dashboard,
            token: res?.data.data.token,
            refreshToken: res?.data.data.refreshToken,
            isVerified: true,
            isOrganizationPresent: res?.data.data.organisation_present,
          };

          if (user) {
            return {
              ...token,
              data: response,
            };
          }

          // call Google authorization API
          return true;
        } catch (error: any) {
          return Promise.reject(
            new Error(
              JSON.stringify({ errors: error.response.data, status: false }),
            ),
          );
        }
      } else {
        if (user) {
          return {
            ...token,
            data: user.data,
          };
        }
        return token;
      }
    },
    session: async ({ session, token }: any) => {
      if (token) {
        session.data = token.data;
        session.isVerified = token?.isVerified;
        session.user.isVerified = token?.data?.isVerified || false;
        session.user.shouldNavigateToDashboard =
          token.data.shouldNavigateToDashboard;
        session.user.fullName = token?.data?.fullName || "";
        session.user.isOrganizationPresent =
          token?.data?.isOrganizationPresent || false;
      }
      return session;
    },
  },
  pages: {
    signIn: "/",
    error: "/",
  },
};
async function getUserData(credentials: any) {
  const payload = {
    workEmail: credentials?.email,
    password: credentials?.password,
  };
  const user = await axios.post(`${baseURL}/api/auth/signin`, payload, {
    headers: {
      "Content-Type": "application/json",
    },
  });

  // User verified APIs
  const isVerifiedDataRes = await axios.get(
    `${baseURL}/api/users/is_verified`,
    {
      headers: {
        // @ts-ignore
        Authorization: `Bearer ${user?.data?.data.token}`,
      },
    },
  );

  // get user data
  const userDetails = await axios.get(`${baseURL}/api/team-members`, {
    headers: {
      // @ts-ignore
      Authorization: `Bearer ${user?.data?.data.token}`,
    },
  });

  const response = {
    shouldNavigateToDashboard: user?.data?.data?.should_navigate_to_dashboard,
    token: user?.data?.data?.token,
    refreshToken: user?.data?.data?.refreshToken,
    isVerified: isVerifiedDataRes?.data?.data,
    fullName: userDetails?.data?.data?.user?.fullName || "",
    isOrganizationPresent: user?.data.data.organisation_present,
  };

  return response;
}
