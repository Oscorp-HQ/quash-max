import { ArrowLeft, X } from "@/app/lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import React, { useEffect, useState } from "react";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useRouter, useSearchParams } from "next/navigation";
import {
  GetJiraUsers,
  GetProjects,
  JiraCallback,
  SetProjectKeys,
} from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import Image from "next/image";
import { Skeleton } from "@/components/ui/skeleton";
import { CONSTANT_JIRA_AUTH_URL } from "@/app/constants/constants";
import {
  JiraIntegrationProps,
  JiraIntegrationRequestBody,
  JiraIssue,
  JiraProject,
} from "@/app/types/jira-types";
import { App } from "@/app/types/application-types";

/**
 * Functional component for managing Jira integrations.
 * Handles authentication, fetching Jira projects, setting project keys, and UI interactions.
 */

const Jira = ({
  setIntegrationSelected,
  fetchIntegrations,
  integrationsDone,
  showDeleteIntegration,
  applications,
  setApplications,
  setDeleteAlert,
}: JiraIntegrationProps) => {
  const jira_url = CONSTANT_JIRA_AUTH_URL;

  const [projectOfJiraSelected, setProjectOfJiraSelected] = useState<
    JiraProject[]
  >([]);
  const [loading, setLoading] = useState(false);
  const [showButton, setShowButton] = useState<boolean>(true);
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const { toast } = useToast();
  const router = useRouter();
  const jiraIntegrationDone = integrationsDone.includes("jira");
  const [loadingJiraData, setLoadingJiraData] = useState(false);

  useEffect(() => {
    let temp = true;

    applications.forEach((app: App) => {
      const integrationKeyMap = app?.integrationKeyMap?.JIRA;

      if (integrationKeyMap) {
        if (integrationKeyMap.projectKey && !integrationKeyMap.issueTypeKey) {
          temp = false;
        }
      }
    });

    setShowButton(temp);
  }, [applications]);

  useEffect(() => {
    if (code) {
      makeCallback(code);
    } else {
      {
        jiraIntegrationDone && getUpdatedJiraProjects();
      }
    }
  }, []);

  const makeCallback = async (code: string) => {
    try {
      const res = await JiraCallback(code);
      if (res.success) {
        toast({
          description: res.message,
        });
        await fetchIntegrations();
        try {
          await GetJiraUsers();
        } catch (error) {
          console.error("Error fetching Jira users:", error);
        }
        await getUpdatedJiraProjects();
      } else {
        toast({
          description: res.message,
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
      console.log(error);
    }
  };

  const getUpdatedJiraProjects = async () => {
    setLoadingJiraData(true);
    try {
      const { data, message, success } = await GetProjects();
      if (success) {
        setProjectOfJiraSelected([...data?.projects?.values]);
        setLoadingJiraData(false);
      } else {
        setLoadingJiraData(false);
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      setLoadingJiraData(false);
      toast({
        description: "Something went wrong. Please try again.",
        typeof: "error",
      });
    }
  };

  const setJiraProject = async () => {
    setLoading(true);
    let body: JiraIntegrationRequestBody[] = [];

    applications.forEach((app: App) => {
      const integrationKeyMap = app?.integrationKeyMap?.JIRA;

      if (integrationKeyMap?.projectKey && integrationKeyMap?.issueTypeKey) {
        body.push({
          appId: app.appId,
          integrationType: "JIRA",
          projectKey: integrationKeyMap?.projectKey,
          issueType: integrationKeyMap?.issueTypeKey,
        });
      }

      if (
        integrationKeyMap?.projectKey !== undefined &&
        !integrationKeyMap?.projectKey &&
        !integrationKeyMap?.issueTypeKey
      ) {
        body.push({
          appId: app.appId,
          integrationType: "JIRA",
          projectKey: integrationKeyMap?.projectKey,
          issueType: integrationKeyMap?.issueTypeKey,
        });
      }
    });
    try {
      const { data, message, success } = await SetProjectKeys(body);
      if (success) {
        setLoading(false);
        toast({
          description: "Changes Saved",
        });
      } else {
        setLoading(false);
        toast({
          description: message,
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      setLoading(false);
      console.log(error);
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    }
  };

  return (
    <div className="integration-container">
      <div className="integration-head-container">
        <ArrowLeft
          size={24}
          onClick={() => {
            if (code) {
              router.push(`/settings/integrations`);
            } else {
              {
                setIntegrationSelected(null);
              }
            }
          }}
          className="nav-icon"
        />
        <div className="head">
          <h1 className="title"> Jira</h1>
          <p className="sub-title">Export Bug reports as Jira tickets</p>
        </div>
      </div>
      <div className="integration-content">
        <div className="integration-action-container">
          <div className="integration-action">
            <div className="integration-message-container">
              <Image
                priority
                width={32}
                height={32}
                src="/icons/Jira.svg"
                alt="jira"
                className="logo"
              />
              <span className="integration-message">
                {jiraIntegrationDone
                  ? "Your integration is active"
                  : "Export bugs from Quash to Jira Projects"}
              </span>
            </div>
            {!jiraIntegrationDone ? (
              <Link href={jira_url || ""}>
                <Button className="integration-connect">
                  <Image
                    priority
                    width={32}
                    height={32}
                    src="/icons/Jira.svg"
                    alt="Jira"
                    className="logo"
                  />
                  Integrate Jira
                </Button>
              </Link>
            ) : (
              <Button
                disabled={!showDeleteIntegration}
                className="integration-disconnect"
                variant="ghost"
                onClick={() => {
                  setDeleteAlert(true);
                }}
              >
                Disconnect
              </Button>
            )}
          </div>
          {!jiraIntegrationDone && <hr className="divider" />}{" "}
          {!jiraIntegrationDone && (
            <span className="integration-description">
              Authenticate with your Atlassian account and export bug tickets to
              your Jira projects.
            </span>
          )}
        </div>
        {jiraIntegrationDone && (
          <>
            <div className="integration-config-container">
              <span className="title">
                Select Jira Projects for your applications.
              </span>
              <div className="integration-apps-container">
                {applications?.length > 0 &&
                  applications.map((app: App, index: number) => (
                    <div className="integration-app" key={index}>
                      <span className="app-name">{app?.appName}</span>
                      {loadingJiraData ? (
                        <div className="integration-dropdown-container">
                          <Skeleton className="integration-skeleton jira-project" />
                          <Skeleton className="integration-skeleton jira-export-type" />
                        </div>
                      ) : (
                        <div className="integration-dropdown-container">
                          <Select
                            defaultValue={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.JIRA?.projectKey
                                : ""
                            }
                            value={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.JIRA?.projectKey
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];
                              temp = applications.map((item: App) => {
                                if (item.appId === app.appId) {
                                  item.integrationKeyMap = {
                                    ...item.integrationKeyMap,
                                    JIRA: {
                                      projectKey: e,
                                      issueTypeKey: "",
                                      integrationType: "JIRA",
                                    },
                                  };
                                }
                                return item;
                              });
                              setApplications(temp.slice(0));
                            }}
                          >
                            <SelectTrigger
                              className={`integration-dropdown-trigger jira-project ${
                                app?.integrationKeyMap?.JIRA?.projectKey
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              }`}
                            >
                              <SelectValue
                                placeholder="Select a Project on Jira"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Select a Project on Jira
                                </SelectItem>
                                {projectOfJiraSelected?.length > 0 &&
                                  projectOfJiraSelected?.map(
                                    (project: JiraProject, index: number) => (
                                      <SelectItem
                                        value={project.id}
                                        className="integration-dropdown-item"
                                        key={index}
                                      >
                                        {project.name}
                                      </SelectItem>
                                    ),
                                  )}
                              </SelectGroup>
                            </SelectContent>
                          </Select>
                          <Select
                            defaultValue={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.JIRA?.issueTypeKey
                                : ""
                            }
                            value={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.JIRA?.issueTypeKey
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];
                              temp = applications.map((item: App) => {
                                if (
                                  item.appId === app.appId &&
                                  item.integrationKeyMap.JIRA
                                ) {
                                  item.integrationKeyMap.JIRA.issueTypeKey = e;
                                }
                                return item;
                              });
                              setApplications(temp.slice(0));
                            }}
                          >
                            <SelectTrigger
                              className={`integration-dropdown-trigger jira-export-type ${
                                app?.integrationKeyMap?.JIRA?.issueTypeKey
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              }`}
                            >
                              <SelectValue
                                placeholder=" Export as"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Export as
                                </SelectItem>

                                {app.integrationKeyMap &&
                                  app?.integrationKeyMap?.JIRA?.projectKey &&
                                  projectOfJiraSelected
                                    .filter(
                                      (project: JiraProject) =>
                                        project.id ===
                                        app?.integrationKeyMap?.JIRA
                                          ?.projectKey,
                                    )[0]
                                    ?.issueTypes?.filter(
                                      (issue: JiraIssue) =>
                                        !issue.name.includes("Sub"),
                                    )
                                    ?.map((type: JiraIssue, index: number) => (
                                      <SelectItem
                                        value={type.id}
                                        className="integration-dropdown-item"
                                        key={index}
                                      >
                                        {type.name}
                                      </SelectItem>
                                    ))}
                              </SelectGroup>
                            </SelectContent>
                          </Select>
                        </div>
                      )}
                    </div>
                  ))}
              </div>
            </div>
          </>
        )}
      </div>
      {jiraIntegrationDone && (
        <div>
          <Button
            className="save-config"
            variant="default"
            disabled={loading || !showButton}
            onClick={() => {
              setJiraProject();
            }}
          >
            {loading ? <SpinLoader /> : "Save Configuration"}
          </Button>
        </div>
      )}
    </div>
  );
};

export default Jira;
