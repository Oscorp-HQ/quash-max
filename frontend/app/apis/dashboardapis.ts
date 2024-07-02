import Api from "./api-config";
import ApiForm from "./api-config";

// To fetch
export const GetReports = async (
  app_id: string,
  page = 0,
  size = 10,
  key: string,
) => {
  try {
    const result = await Api.get(
      `/api/report?orgKey=${key}&appId=${app_id}&page=${page}&size=${size}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const patchReport = async (reportId: string, data: FormData) => {
  try {
    const result = await ApiForm.patch(`/api/report/${reportId}`, data);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const DeleteReport = async (reportId: string) => {
  try {
    const result = await Api.delete(`/api/report/${reportId}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const ExportReports = async (
  body: {
    issues: string[];
  },
  type: string,
) => {
  try {
    const result = await Api.post(
      `/api/integrations/${type}/export-${
        type === "clickup" ? "task" : "issues"
      }`,
      body,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetCrashLogs = async (file: string) => {
  try {
    const result = await Api.get(file);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetIntegration = async () => {
  try {
    const result = await Api.get(`/api/integrations`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetAppVerification = async (token: string) => {
  try {
    const result = await Api.get(`/api/app/verify-app?orgToken=${token}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetOrganization = async () => {
  try {
    const result = await Api.get(`/api/dashboard/organisation`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetNetworkLogs = async (reportId: string) => {
  try {
    const result = await Api.get(`/api/report/network-logs/${reportId}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const PostComment = async (body: FormData) => {
  try {
    const result = await Api.post(`/api/report/comment-thread`, body);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetComments = async (reportId: string) => {
  try {
    const result = await Api.get(`/api/report/get-thread?reportId=${reportId}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const DeleteComment = async (threadId: string) => {
  try {
    const result = await Api.delete(
      `/api/report/delete-thread?threadId=${threadId}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetReportsById = async (reportId: string) => {
  try {
    const result = await Api.get(
      `/api/report/getReportById?reportId=${reportId}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GenerateGifByReportId = async (reportId: string) => {
  try {
    const result = await Api.get(`/api/report/${reportId}/generate-gif`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};
