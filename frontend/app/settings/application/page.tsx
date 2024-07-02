import React from "react";
import Application from "./application";
import { fetchDataForOrganization } from "@/app/utils/helper";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/utils/authOptions";

/**
 * Asynchronously fetches organization data and constructs metadata containing the organization's name for a webpage's title and description.
 * @returns An object with title and description properties.
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
  const title = `Applications - ${organizationName} - Quash`;
  const description = `Applications tab for - ${organizationName}`;

  return { title, description };
}

/**
 * Fetches data for an organization , generates metadata, and renders the Application component.
 *
 * @returns {JSX.Element} The Application component with organization data
 */

const Applicationpage = async () => {
  const session = await getServerSession(authOptions);

  const dataOrg = await fetchDataForOrganization(session);

  return <Application orgData={dataOrg} />;
};

export default Applicationpage;
