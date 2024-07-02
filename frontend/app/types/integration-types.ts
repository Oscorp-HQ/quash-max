import { ReactElement } from "react";
import { App } from "./application-types";
import { IntegrationKeyMapLocal } from "./integration-key-map-types";
import { OrganisationApiResponse } from "./organisation-types";

export interface IntegrationLocal {
  title: string;
  src: string;
  component: ReactElement<IntegrationProps>;
  value: string;
}

export interface IntegrationProps {
  setIntegrationSelected: React.Dispatch<
    React.SetStateAction<IntegrationLocal | null>
  >;
  data: OrganisationApiResponse;
  showDeleteIntegration: boolean;
  integrationData: IntegrationKeyMapLocal;
  setIntegrationData: React.Dispatch<
    React.SetStateAction<IntegrationKeyMapLocal>
  >;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  fetchIntegrations: () => void;
  applications: App[];
  setApplications: React.Dispatch<React.SetStateAction<App[]>>;
  deleteAlert: boolean;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
}
