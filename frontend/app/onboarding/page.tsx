import { getServerSession } from "next-auth/next";
import { redirect } from "next/navigation";
import OnBoardingForm from "./onboarding-form";
import type { Metadata } from "next";
import { baseURL } from "../apis/api-config";
import { authOptions } from "../utils/authOptions";

export const metadata: Metadata = {
  title: "Onboarding - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

async function getData() {
  const session = await getServerSession(authOptions);

  const authToken = session?.data?.token;

  if (baseURL) {
    const data = await fetch(`${baseURL}/api/dashboard/organisation`, {
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
    })
      .then((res) => {
        if (res.status === 200) return res.json();
      })
      .catch((e) => console.log(e));

    return data;
  }
}

export default async function OnBoarding() {
  const session = await getServerSession(authOptions);

  if (session) {
    if (session?.user?.isVerified === true) {
      if (session?.user?.shouldNavigateToDashboard === true) {
        redirect("/dashboard");
      }
    } else if (session?.user?.isVerified === false) {
      redirect("/verify");
    }
  }

  const dataOrg = await getData();

  return (
    <div className="onboarding-page-layout">
      <div className="onboarding-header">
        <h1 className="title">
          {!session?.user?.isOrganizationPresent
            ? "Lets get to know you"
            : `${dataOrg?.data?.orgName || ""} Workspace`}
        </h1>
        <p className="sub-title">
          {!session?.user?.isOrganizationPresent
            ? "Knowing your details is essential to generate a key for you."
            : "Set up your profile for your teammates to recognise you"}
        </p>
      </div>

      {session?.user?.isOrganizationPresent !== undefined && (
        <OnBoardingForm
          isOrganizationPresent={session?.user?.isOrganizationPresent}
        />
      )}
    </div>
  );
}
