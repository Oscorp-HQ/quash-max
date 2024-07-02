"use client";
import React, { useEffect, useState } from "react";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { CaretRight, Plus, X } from "../../lib/icons";
import { useSearchParams } from "next/navigation";
import { GetIntegration } from "@/app/apis/dashboardapis";
import { useTheme } from "next-themes";
import { signOut } from "next-auth/react";
import SuspenseWrapper from "@/app/dashboard/components/suspense-wrapper";
import { Session } from "next-auth";
import { OrganisationApiResponse } from "@/app/types/organisation-types";
import {
  IntegrationCommon,
  IntegrationKeyMapLocal,
} from "@/app/types/integration-key-map-types";
import { App } from "@/app/types/application-types";
import { Member } from "@/app/types/member-types";
import { IntegrationLocal } from "@/app/types/integration-types";
const RequestIntegrationDialog = React.lazy(
  () => import("./components/request-integration-dialog"),
);
const DeleteIntegrationDialog = React.lazy(
  () => import("./components/delete-integration-dialog"),
);
const LazyJira = React.lazy(() => import("./jira"));
const LazyLinear = React.lazy(() => import("./linear"));
const LazySlack = React.lazy(() => import("./slack"));
const LazyGithub = React.lazy(() => import("./gitHub"));

/**
 * Functional component for managing integrations in a React application.
 * Handles fetching integration data, displaying integration options, and managing integration state.
 * Utilizes lazy loading for various components and suspense for loading states.
 * Integrations can be selected, requested, and deleted with corresponding dialogs.
 * Manages user sessions, theme settings, and integration data.
 * Supports sign out functionality and tracks integration page views.
 */

const Intergrations = ({
  orgData,
  sessionData,
}: {
  orgData: OrganisationApiResponse;
  sessionData: Session | null;
}) => {
  const searchParams = useSearchParams();
  const integration = searchParams.get("integration");
  const [integrationData, setIntegrationData] =
    useState<IntegrationKeyMapLocal>({});
  const [requestForm, setRequestForm] = useState(false);
  const [showDeleteIntegration, setShowDeleteIntegration] = useState(false);
  const session = sessionData;
  const [integrationSelected, setIntegrationSelected] =
    useState<IntegrationLocal | null>(null);
  const [integrationsDone, setIntegrationsDone] = useState<string[]>([]);
  const [deleteAlert, setDeleteAlert] = useState(false);
  const [applications, setApplications] = useState<App[]>([]);
  const { theme } = useTheme();

  const githubLogoColor =
    theme === "light" ? "/github-mark.svg" : "/github-mark-white.svg";

  const integrations: IntegrationLocal[] = [
    {
      title: "Jira",
      src: "/icons/Jira.svg",
      component: (
        <LazyJira
          setIntegrationSelected={function (
            value: React.SetStateAction<IntegrationLocal | null>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          showDeleteIntegration={false}
          integrationsDone={[]}
          setIntegrationsDone={function (
            value: React.SetStateAction<string[]>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          fetchIntegrations={function (): void {
            throw new Error("Function not implemented.");
          }}
          applications={[]}
          setApplications={function (value: React.SetStateAction<App[]>): void {
            throw new Error("Function not implemented.");
          }}
          setDeleteAlert={function (
            value: React.SetStateAction<boolean>,
          ): void {
            throw new Error("Function not implemented.");
          }}
        />
      ),
      value: "jira",
    },

    {
      title: "Slack",
      src: "/icons/slack.svg",
      component: (
        <LazySlack
          setIntegrationSelected={function (
            value: React.SetStateAction<IntegrationLocal | null>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          showDeleteIntegration={false}
          integrationsDone={[]}
          setIntegrationsDone={function (
            value: React.SetStateAction<string[]>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          fetchIntegrations={function (): void {
            throw new Error("Function not implemented.");
          }}
          applications={[]}
          setApplications={function (value: React.SetStateAction<App[]>): void {
            throw new Error("Function not implemented.");
          }}
          deleteAlert={false}
          setDeleteAlert={function (
            value: React.SetStateAction<boolean>,
          ): void {
            throw new Error("Function not implemented.");
          }}
        />
      ),
      value: "slack",
    },
    {
      title: "Linear",
      src: "/icons/linear.svg",
      component: (
        <LazyLinear
          setIntegrationSelected={function (
            value: React.SetStateAction<IntegrationLocal | null>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          showDeleteIntegration={false}
          integrationsDone={[]}
          setIntegrationsDone={function (
            value: React.SetStateAction<string[]>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          fetchIntegrations={function (): void {
            throw new Error("Function not implemented.");
          }}
          applications={[]}
          setApplications={function (value: React.SetStateAction<App[]>): void {
            throw new Error("Function not implemented.");
          }}
          deleteAlert={false}
          setDeleteAlert={function (
            value: React.SetStateAction<boolean>,
          ): void {
            throw new Error("Function not implemented.");
          }}
        />
      ),
      value: "linear",
    },
    {
      title: "Github",
      src: githubLogoColor,
      component: (
        <LazyGithub
          setIntegrationSelected={function (
            value: React.SetStateAction<IntegrationLocal | null>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          showDeleteIntegration={false}
          integrationsDone={[]}
          setIntegrationsDone={function (
            value: React.SetStateAction<string[]>,
          ): void {
            throw new Error("Function not implemented.");
          }}
          fetchIntegrations={function (): void {
            throw new Error("Function not implemented.");
          }}
          applications={[]}
          setApplications={function (value: React.SetStateAction<App[]>): void {
            throw new Error("Function not implemented.");
          }}
          deleteAlert={false}
          setDeleteAlert={function (
            value: React.SetStateAction<boolean>,
          ): void {
            throw new Error("Function not implemented.");
          }}
        />
      ),
      value: "github",
    },
  ];

  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };

  useEffect(() => {
    fetchIntegrations();
  }, []);

  useEffect(() => {
    if (integration) {
      let temp: IntegrationLocal | null =
        integrations.find(
          (element) => element.value === integration.toLocaleLowerCase(),
        ) ?? null;
      setIntegrationSelected(temp);
    } else {
      setIntegrationSelected(null);
    }
  }, [searchParams]);

  useEffect(() => {
    if (orgData) {
      const temp = orgData?.data?.organisationApps
        ?.map((app: App) => {
          return {
            ...app,
          };
        })
        .slice(0);
      setApplications(temp.slice(0));
    } else {
      handleSignOut();
    }
    if (
      orgData &&
      orgData?.data?.orgMembers?.filter(
        (member: Member) => member.email === session?.user?.email,
      )[0]?.admin
    ) {
      setShowDeleteIntegration(true);
    }
  }, [session, orgData]);

  const fetchIntegrations = async () => {
    try {
      const { data, success } = await GetIntegration();
      if (success) {
        let temp: string[] = [];
        let tempIntegrationData: IntegrationKeyMapLocal = {};

        data.forEach((integration: IntegrationCommon) => {
          const integrationType = integration.integrationType;

          switch (integrationType) {
            case "JIRA":
              tempIntegrationData["jira"] = integration;
              temp.push("jira");
              break;
            case "SLACK":
              tempIntegrationData["slack"] = integration;
              temp.push("slack");
              break;
            case "LINEAR":
              tempIntegrationData["linear"] = integration;
              temp.push("linear");
              break;
            case "GITHUB":
              tempIntegrationData["github"] = integration;
              temp.push("github");
              break;
          }
        });

        setIntegrationsDone(temp.slice(0));
        setIntegrationData({ ...tempIntegrationData });
      }
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <>
      {!integrationSelected ? (
        <div className="integrations">
          <div className="head flex flex-col gap-3">
            <h1 className="title"> Integrations</h1>
            <p className="sub-title">
              Setup and configure your Quash integrations
            </p>
          </div>
          <div className="integrations-container">
            {integrations.map((integration, index) => (
              <Button
                variant="ghost"
                className={`integration`}
                key={index}
                onClick={() => {
                  setIntegrationSelected(integration);
                }}
              >
                <div className="integration-info">
                  <Image
                    priority
                    width={32}
                    height={32}
                    src={integration.src}
                    alt={integration.value}
                    className="bg-none"
                  />

                  {integration.title}
                </div>
                {integrationsDone.includes(integration.value) && (
                  <span className="active">Active</span>
                )}
                <CaretRight size={16} className="icon" />
              </Button>
            ))}
            <div className="">
              <Button
                variant="ghost"
                className="request-integration"
                onClick={() => {
                  setRequestForm(true);
                }}
              >
                <Plus /> Request Integration
              </Button>
            </div>
          </div>

          {requestForm && (
            <SuspenseWrapper>
              <RequestIntegrationDialog
                requestForm={requestForm}
                setRequestForm={setRequestForm}
              />
            </SuspenseWrapper>
          )}
        </div>
      ) : (
        <SuspenseWrapper>
          {integrationSelected &&
            integrationSelected.component &&
            React.isValidElement(integrationSelected.component) &&
            React.cloneElement(integrationSelected.component, {
              setIntegrationSelected: setIntegrationSelected,
              data: orgData,
              showDeleteIntegration: showDeleteIntegration,
              integrationData: integrationData,
              setIntegrationData: setIntegrationData,
              integrationsDone: integrationsDone,
              setIntegrationsDone: setIntegrationsDone,
              fetchIntegrations: fetchIntegrations,
              applications: applications,
              setApplications: setApplications,
              deleteAlert: deleteAlert,
              setDeleteAlert: setDeleteAlert,
            })}
        </SuspenseWrapper>
      )}
      {deleteAlert && integrationSelected !== null && (
        <SuspenseWrapper>
          <DeleteIntegrationDialog
            deleteAlert={deleteAlert}
            setDeleteAlert={setDeleteAlert}
            integrationsDone={integrationsDone}
            setIntegrationsDone={setIntegrationsDone}
            integrationData={integrationData}
            integrationSelected={integrationSelected}
          />
        </SuspenseWrapper>
      )}
    </>
  );
};

export default Intergrations;
