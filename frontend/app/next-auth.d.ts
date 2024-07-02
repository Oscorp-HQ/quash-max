import NextAuth from "next-auth";

declare module "next-auth" {
  export interface User {
    email: string;
    isVerified: boolean;
    shouldNavigateToDashboard: boolean;
    fullName: string;
    isOrganizationPresent: boolean;
  }

  export interface jwtUser {
    redirect: string;
    email: string;
    password: string;
    csrfToken: string;
    callbackUrl: string;
    json: string;
    data: {
      shouldNavigateToDashboard: boolean;
      token: string;
      refreshToken: string;
      isVerified: boolean;
      fullName: string;
      isOrganizationPresent: boolean;
    };
  }

  export interface SessionData {
    shouldNavigateToDashboard: boolean;
    token: string;
    refreshToken: string;
    isVerified: boolean;
    fullName: string;
    isOrganizationPresent: boolean;
  }
  export interface Session {
    user: User;
    expires?: string;
    data: SessionData;
    isVerified?: boolean;
  }
}

export interface Account {
  provider: string;
  access_token: string;
}

export interface Credentials {
  email: string;
  password: string;
}

export type Trigger = "update" | "otherTrigger";
