import React from "react";
import Members from "./member";
import { fetchDataForOrganization } from "@/app/utils/helper";
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
  const title = `Members - ${organizationName} - Quash`;
  const description = `Members tab for - ${organizationName}`;

  return { title, description };
}

/**
 * This code snippet exports a function `generateMetadata` that fetches organization data, extracts the organization name, and constructs a title and description using the organization name.
 * Additionally, there is an async function `Memberspage` that fetches organization data, then returns a component `Members` with the fetched data as props.
 */

const Memberspage = async () => {
  const session = await getServerSession(authOptions);

  const dataOrg = await fetchDataForOrganization(session);

  return <Members orgData={dataOrg} />;
};

export default Memberspage;
