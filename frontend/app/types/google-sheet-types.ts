import { IntegrationLocal } from "./integration-types";
import { IntegrationKeyMapLocal } from "./integration-key-map-types";

export interface GoogleIntegrationProps {
  setIntegrationSelected: React.Dispatch<
    React.SetStateAction<IntegrationLocal | null>
  >;
  showDeleteIntegration: boolean;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  fetchIntegrations: () => void;
  integrationData: IntegrationKeyMapLocal;
  setIntegrationData: React.Dispatch<
    React.SetStateAction<IntegrationKeyMapLocal>
  >;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
}

export interface GoogleIntegrationDialogProps {
  openDialouge: boolean;
  setOpenDialouge: React.Dispatch<React.SetStateAction<boolean>>;
  loading: boolean;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  integrationData: IntegrationKeyMapLocal;
  setIntegrationData: React.Dispatch<
    React.SetStateAction<IntegrationKeyMapLocal>
  >;
}

export interface GoogleRepo {
  name: string;
}

export interface GoogleIntegrationRequestBody {
  appId: string;
  integrationType: string;
  repoName: string;
}
