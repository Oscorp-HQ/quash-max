import { App } from "./application-types";
import { IntegrationLocal } from "./integration-types";

export interface JiraIntegrationProps {
  setIntegrationSelected: React.Dispatch<
    React.SetStateAction<IntegrationLocal | null>
  >;
  showDeleteIntegration: boolean;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  fetchIntegrations: () => void;
  applications: App[];
  setApplications: React.Dispatch<React.SetStateAction<App[]>>;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
}

export interface JiraIssue {
  name: string;
  id: string;
}

export interface JiraProject {
  name: string;
  id: string;
  issueTypes: JiraIssue[];
}

export interface JiraIntegrationRequestBody {
  appId: string;
  integrationType: string;
  projectKey: string;
  issueType: string;
}
