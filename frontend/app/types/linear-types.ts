import { App } from "./application-types";
import { IntegrationLocal } from "./integration-types";

export interface LinearIntegrationProps {
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

export interface LineaNode {
  name: string;
  id: string;
}

export interface LinearProject {
  name: string;
  id: string;
  nodes: LineaNode[];
}

export interface LinearTeam {
  name: string;
  id: string;
  projects: LinearProject;
}

export interface LinearIntegrationRequestBody {
  appId: string;
  integrationType: string;
  projectId: string;
  teamId: string;
}
