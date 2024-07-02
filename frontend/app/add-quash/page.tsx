import React from "react";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/utils/authOptions";
import { fetchDataForOrganization } from "@/app/utils/helper";
import { Metadata } from "next/types";
import AddQuash from "./add-quash";

export const metadata: Metadata = {
  title: "Onboarding - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

const Integrationpage = async () => {
  const session = await getServerSession(authOptions);
  const dataOrg = await fetchDataForOrganization(session);
  return <AddQuash appToken={dataOrg?.data?.organisationKey} />;
};

export default Integrationpage;
