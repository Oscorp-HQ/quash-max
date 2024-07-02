import { OrganisationOfUser, UserType } from "./organisation-types";

export interface ReportsMetaData {
  currentPage: number;
  perPage: number;
  totalPages: number;
  totalRecords: number;
}

export interface MediaData {
  bugId: string | null;
  createdAt: string;
  id: string;
  mediaRef: string;
  mediaType: string;
  mediaUrl: string;
}

export interface CrashLog {
  bugId: string | null;
  createdAt: string;
  id: string;
  logUrl: string;
  mediaRef: string;
}

export interface Report {
  id: string;
  title: string;
  description: string;
  type: string;
  source: string;
  priority: string;
  listOfMedia: MediaData[] | null;
  crashLog: CrashLog;
  reporterId: string;
  appId: string;
  listOfGif: MediaData[];
  exportedOn: string | null;
  gifStatus: string;
  status: string;
  updatedAt: string | null;
  reportedBy: UserType;
  deviceMetadata: DeviceMetaData | null;
  exported: boolean;
  reportedByName: string;
  createdAt: string;
}

export interface DeviceMetaData {
  id: string;
  device: string;
  os: string;
  screenResolution: string;
  networkType: string;
  batteryLevel: string;
  memoryUsage: string;
  organisation: OrganisationOfUser;
}

export interface ReportsData {
  meta: ReportsMetaData;
  reports: Report[];
}

export interface ReportsApiResponse {
  data: ReportsData;
  success: boolean;
  message: string;
}

export interface ReportsByIdApiResponse {
  data: Report;
  success: boolean;
  message: string;
}

export interface ToastObject {
  message: string;
  type: string;
}

export interface NetworkLog {
  durationMs: number;
  errorMessage: null;
  exceptionMessage: null;
  exceptionStackTrace: null;
  reportId: string;
  requestBody: "";
  requestHeaders: any;
  requestMethod: string;
  requestUrl: string;
  responseBody: string;
  responseCode: number;
  responseHeaders: any;
  timeStamp: string;
}
