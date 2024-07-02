import { App } from "./application-types";
import { IntegrationLocal } from "./integration-types";

export interface GitHubIntegrationProps {
  setIntegrationSelected: React.Dispatch<
    React.SetStateAction<IntegrationLocal | null>
  >;
  showDeleteIntegration: boolean;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  fetchIntegrations: () => void;
  applications: App[];
  setApplications: React.Dispatch<React.SetStateAction<App[]>>;
  deleteAlert: boolean;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
}

export interface GitHubRepo {
  name: string;
}

export interface GitHubIntegrationRequestBody {
  appId: string;
  integrationType: string;
  repoName: string;
}
