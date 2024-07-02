import React from "react";
import { getServerSession } from "next-auth";
import Intergrations from "./integration";
import { authOptions } from "@/app/utils/authOptions";
import { fetchDataForOrganization } from "@/app/utils/helper";

/**
 * Asynchronous function to generate metadata for the Integrations page.
 * Fetches organization data and constructs title and description using the organization name.
 *
 * @returns Promise<{ title: string, description: string }> - Object containing title and description for the page
 */

export async function generateMetadata(): Promise<{
  title: string;
  description: string;
}> {
  const session = await getServerSession(authOptions);

  // Fetch organization data
  const organizationData = await fetchDataForOrganization(session);

  // Extract organization name
  const organizationName = organizationData?.data?.orgName;

  // Construct title and description using the organization name
  const title = `Integrations - ${organizationName} - Quash`;
  const description = `Integrations tab for - ${organizationName}`;

  return { title, description };
}

const Integrationpage = async () => {
  const session = await getServerSession(authOptions);
  const dataOrg = await fetchDataForOrganization(session);

  return <Intergrations orgData={dataOrg} sessionData={session} />;
};

export default Integrationpage;
