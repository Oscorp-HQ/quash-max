import { Member } from "./member-types";
import { App } from "./application-types";

export interface ApiError {
  data?: {
    message?: string;
  };
  message?: string;
  response?: {
    data?: {
      message?: string;
    };
  };
}

export interface Organisation {
  orgId: string;
  orgName: string;
  orgAbb: string;
  orgCreatedAt: string;
  organisationKey: string;
  orgMembers: Member[];
  organisationApps: App[];
}

export interface OrganisationApiResponse {
  success: boolean;
  message: string;
  data: Organisation;
}

export interface OrganisationOfUser {
  id: number;
  orgAbbreviation: string;
  orgUniqueKey: string;
  name: string;
  profileImage: string | null;
  coverImage: string | null;
  createdBy: UserType;
  createdAt: string;
  lastActive: string;
  shouldSend24HrMail: boolean;
  shouldSend72HrMail: boolean;
}

export interface UserType {
  id: string;
  fullName: string;
  username: string | null;
  workEmail: string;
  password: string;
  profileImage: string | null;
  coverImage: string | null;
  emailVerified: boolean;
  verificationToken: string | null;
  tokenExpiration: string | null;
  createdAt: string;
  shouldNavigateToDashboard: boolean;
  userOrganisationRole: string;
  signUpType: string;
  lastActive: string | null;
  outhState: string | null;
  enabled: boolean;
  authorities: string | null;
  credentialsNonExpired: boolean;
  accountNonExpired: boolean;
  accountNonLocked: boolean;
}

export interface TeamMemberDetails {
  id: string;
  organisation: OrganisationOfUser;
  user: UserType;
  role: string;
  joinedAt: string;
  hasAccepted: boolean;
  phoneNumber: string;
  active: boolean;
}

export interface userDataApiResponse {
  success: boolean;
  message: string;
  data: TeamMemberDetails;
}
