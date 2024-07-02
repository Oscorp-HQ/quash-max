interface Integration {
  integrationType: string;
  id?: string;
}

export interface IntegrationCommon {
  integrationType: string;
  repoName: string;
  teamId: string;
  projectId: string;
  projectKey: string;
  issueTypeKey: string;
  channelId: string;
  settings: {
    sheet_link: string;
  };
  workspaceId: string;
  spaceId: string;
  folderId: string;
  listId: string;
}

interface GitHubIntegration extends Integration {
  repoName: string;
}

interface LinearIntegration extends Integration {
  teamId: string;
  projectId: string;
}

interface JiraIntegration extends Integration {
  projectKey: string;
  issueTypeKey: string;
}

interface SlackIntegration extends Integration {
  channelId: string;
}

interface GoogleIntegration extends Integration {
  settings: {
    sheet_link: string;
  };
}

interface ClickUpIntegration extends Integration {
  workspaceId: string;
  spaceId: string;
  folderId: string;
  listId: string;
}

export type IntegrationKeyMap = {
  GITHUB?: GitHubIntegration;
  LINEAR?: LinearIntegration;
  JIRA?: JiraIntegration;
  SLACK?: SlackIntegration;
  GOOGLE_SHEETS?: GoogleIntegration;
  CLICKUP?: ClickUpIntegration;
};

export type IntegrationKeyMapLocal = {
  github?: GitHubIntegration;
  linear?: LinearIntegration;
  jira?: JiraIntegration;
  slack?: SlackIntegration;
  google?: GoogleIntegration;
  clickup?: ClickUpIntegration;
};
