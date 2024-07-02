import { IntegrationKeyMap } from "./integration-key-map-types";

export interface App {
  appId: string;
  packageName: string;
  appType: string;
  appName: string;
  reportingToken: string;
  integrationKeyMap: IntegrationKeyMap;
}
