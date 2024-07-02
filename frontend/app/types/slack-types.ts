import { App } from "./application-types";
import { IntegrationLocal } from "./integration-types";

export interface SlackIntegrationProps {
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

export interface SlackChannel {
  channelId: string;
  channelName: string;
}

export interface SlackIntegrationRequestBody {
  appId: string;
  integrationType: string;
  channelId: string;
}
