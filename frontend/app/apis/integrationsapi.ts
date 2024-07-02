import { GitHubIntegrationRequestBody } from "../types/github-types";
import { JiraIntegrationRequestBody } from "../types/jira-types";
import { LinearIntegrationRequestBody } from "../types/linear-types";
import { SlackIntegrationRequestBody } from "../types/slack-types";
import Api from "./api-config";

export const JiraCallback = async (code: string) => {
  try {
    const result = await Api.get(
      `/api/integrations/jira/callback?code=${code}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const SlackCallback = async (code: string) => {
  try {
    const result = await Api.post(
      `/api/integrations/slack/oauth/callback?code=${code}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const LinearCallback = async (code: string) => {
  try {
    const result = await Api.post(
      `/api/integrations/linear/oauth/callback?code=${code}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetLinearProjects = async () => {
  try {
    const result = await Api.get(`/api/integrations/linear/get-projects`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetProjects = async () => {
  try {
    const result = await Api.get(`/api/integrations/jira/get-projects`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetSlackAuth = async () => {
  try {
    const result = await Api.get(`/api/integrations/slack/auth`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetLinearAuth = async () => {
  try {
    const result = await Api.get(`/api/integrations/linear/auth`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetChannels = async (id: string) => {
  try {
    const result = await Api.get(`/api/app/slack/fetch-channels?appId=${id}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetJiraUsers = async () => {
  try {
    const result = await Api.get(`/api/integrations/jira/get-users`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const IntegrationDelete = async (id: string) => {
  try {
    const result = await Api.delete(`/api/integrations?integrationId=${id}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const SetProjectKeys = async (
  body:
    | GitHubIntegrationRequestBody[]
    | JiraIntegrationRequestBody[]
    | LinearIntegrationRequestBody[]
    | SlackIntegrationRequestBody[],
) => {
  try {
    const result = await Api.post(`/api/app/set-project-key`, body);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const RequestIntegration = async (body: { featureRequest: string }) => {
  try {
    const result = await Api.post(
      `/api/integrations/request-integration`,
      body,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetGithubAuth = async () => {
  try {
    const result = await Api.get(`/api/integrations/github/auth`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GithubCallback = async (code: string) => {
  try {
    const result = await Api.post(
      `/api/integrations/github/oauth/callback?code=${code}`,
    );
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetRepositories = async () => {
  try {
    const result = await Api.get(`/api/integrations/github/get-repositories`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};
