import React from "react";
import General from "./general";
import { fetchData, fetchDataForOrganization } from "@/app/utils/helper";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/utils/authOptions";
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
  const title = `General Settings - ${organizationName} - Quash`;
  const description = `General Settings tab for - ${organizationName}`;

  return { title, description };
}

async function getMembers() {
  const session = await getServerSession(authOptions);

  return await fetchData(`/api/team-members`, session);
}

/**
 * This code snippet exports a function named `generateMetadata` that fetches organization data, extracts the organization name, and constructs a title and description using the organization name. It returns an object with the title and description.
 *
 * It also defines an async function `getMembers` that fetches team members data.
 *
 * Lastly, it exports a function named `Generalpage` that asynchronously fetches team members data and returns a component `<General data={data} />`.
 */

const Generalpage = async () => {
  const data = await getMembers();
  return <General data={data} />;
};

export default Generalpage;
