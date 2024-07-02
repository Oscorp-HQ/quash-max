import React from "react";
import Dashboard from "./dashboard";
import { getServerSession } from "next-auth";
import { redirect } from "next/navigation";
import { authOptions } from "../utils/authOptions";
import { fetchData, fetchDataForOrganization } from "../utils/helper";
import { IntegrationKeyMap } from "../types/integration-key-map-types";

/**
 * Asynchronous function to generate metadata for the dashboard page.
 * Fetches organization data, constructs title and description based on the organization name.
 *
 * @returns Promise<{ title: string, description: string }>
 */

type Integration = keyof IntegrationKeyMap;

export async function generateMetadata(): Promise<{
  title: string;
  description: string;
}> {
  const session = await getServerSession(authOptions);

  const organizationData = await fetchDataForOrganization(session);

  const organizationName = organizationData?.data?.orgName;

  const title = `${organizationName} - Quash`;
  const description = `Dashboard for - ${organizationName}`;

  return { title, description };
}

/**
 * Asynchronous function to fetch integration data.
 *
 * @returns Promise
 */
async function getIntegrationData() {
  const session = await getServerSession(authOptions);

  return await fetchData(`/api/integrations`, session);
}

/**
 * Asynchronous function to handle the dashboard page logic.
 * Fetches session, organization data, and integration data.
 * Determines if an invite needs to be sent, checks completed integrations, and handles user verification.
 *
 * @returns JSX.Element - Dashboard component with necessary props
 */

const Dashboardpage = async () => {
  const session = await getServerSession(authOptions);
  const dataOrg = await fetchDataForOrganization(session);

  const dataIntegration = await getIntegrationData();
  let inviteMember = false;
  let integrationsDone: string[] = [];

  let integration = false;

  const [data, integrationData] = await Promise.all([dataOrg, dataIntegration]);
  if (
    data?.data?.orgMembers?.length === 1 &&
    data?.data?.orgMembers[0].email === session?.user.email
  ) {
    inviteMember = true;
  }

  if (integrationData?.success) {
    integrationData?.data.forEach(
      (integration: { integrationType: Integration }) => {
        const integrationType = integration.integrationType;

        switch (integrationType) {
          case "JIRA":
            integrationsDone.push("jira");
            break;
          case "SLACK":
            integrationsDone.push("slack");
            break;
          case "LINEAR":
            integrationsDone.push("linear");
            break;
          case "GITHUB":
            integrationsDone.push("github");
            break;
        }
      },
    );

    if (integrationsDone.length === 0) {
      integration = true;
    }
  }

  if (session) {
    if (session?.user?.shouldNavigateToDashboard === false) {
      if (session?.user?.isVerified === false) {
        redirect("/verify");
      }
    }
  }
  return (
    <Dashboard
      orgData={data}
      inviteMember={inviteMember}
      integrationsDone={integrationsDone}
      integration={integration}
    />
  );
};

export default Dashboardpage;
