import { ArrowLeft, X } from "@/app/lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { Button } from "@/components/ui/button";
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
  GetGithubAuth,
  GetRepositories,
  GithubCallback,
  SetProjectKeys,
} from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import Image from "next/image";
import { Skeleton } from "@/components/ui/skeleton";
import { useTheme } from "next-themes";
import {
  GitHubIntegrationProps,
  GitHubIntegrationRequestBody,
  GitHubRepo,
} from "@/app/types/github-types";
import { App } from "@/app/types/application-types";

/**
 * Component for managing Github integrations in the application.
 * Handles authentication, fetching repositories, setting project keys, and UI interactions.
 */

const Github = ({
  setIntegrationSelected,
  fetchIntegrations,
  integrationsDone,
  showDeleteIntegration,
  applications,
  setApplications,
  setDeleteAlert,
}: GitHubIntegrationProps) => {
  const [githubRepositories, setGithubRepositories] = useState<GitHubRepo[]>(
    [],
  );
  const [loading, setLoading] = useState(false);
  const [showButton, setShowButton] = useState<boolean>(false);
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const { toast } = useToast();
  const router = useRouter();
  const githubIntegrationDone = integrationsDone.includes("github");
  const [loadingGithubData, setloadingGithubData] = useState(false);
  const { theme } = useTheme();

  const githubLogoColor =
    theme === "light" ? "/github-mark.svg" : "/github-mark-white.svg";

  const githubButtonLogoColor =
    theme === "light" ? "/github-mark-white.svg" : "/github-mark.svg";

  useEffect(() => {
    if (code) {
      makeCallback(code);
    } else {
      {
        githubIntegrationDone && getUpdatedGithubRepository();
      }
    }
  }, []);

  useEffect(() => {
    let temp = false;

    applications.forEach((app: App) => {
      const integrationKeyMap = app?.integrationKeyMap?.GITHUB;
      if (integrationKeyMap) {
        if (integrationKeyMap.repoName) {
          temp = true;
        }
      }
    });

    setShowButton(temp);
  }, [applications]);

  const makeCallback = async (code: string) => {
    try {
      const res = await GithubCallback(code);
      if (res.success) {
        toast({
          description: res.message,
        });
        await fetchIntegrations();
        await getUpdatedGithubRepository();
      } else {
        toast({
          description: res.message,
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      toast({
        description: "Something went wrong, please try again.",
        typeof: "error",
      });
      console.log(error);
    }
  };

  const getUpdatedGithubRepository = async () => {
    setloadingGithubData(true);
    try {
      const { data, message, success } = await GetRepositories();
      if (success) {
        setGithubRepositories([...data]);
        setloadingGithubData(false);
      } else {
        setloadingGithubData(false);
        false;
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      setloadingGithubData(false);
      false;
    }
  };

  const setGithubRepository = async () => {
    setLoading(true);
    let body: GitHubIntegrationRequestBody[] = [];

    applications.map((app: App) => {
      if (app.integrationKeyMap?.GITHUB) {
        body.push({
          appId: app.appId,
          integrationType: "GITHUB",
          repoName: app.integrationKeyMap?.GITHUB?.repoName || "",
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
          description: message
            ? message
            : "Something went wrong. Please try again.",
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

  const githubAuth = async () => {
    setLoading(true);
    try {
      const { data, message, success } = await GetGithubAuth();
      if (success) {
        router.push(data);
      } else {
        setLoading(false);
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      toast({
        description: "Something went wrong, please try again.",
        typeof: "error",
      });
      setLoading(false);
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
          <h1 className="title"> Github</h1>
          <p className="sub-title">Export Bug reports as Github tickets</p>
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
                src={githubLogoColor}
                alt="Github"
                className="logo"
              />
              <span className="integration-message">
                {githubIntegrationDone
                  ? "Your integration is active"
                  : "Export bugs from Quash to Github Projects"}
              </span>
            </div>
            {!githubIntegrationDone ? (
              <Button
                className="integration-connect"
                onClick={() => {
                  githubAuth();
                }}
              >
                <Image
                  priority
                  width={32}
                  height={32}
                  src={githubButtonLogoColor}
                  alt="Github"
                  className="logo"
                />
                Integrate Github
              </Button>
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
          {!githubIntegrationDone && <hr className="divider" />}{" "}
          {!githubIntegrationDone && (
            <span className="integration-description">
              Authenticate with your Atlassian account and export bug tickets to
              your Github projects.
            </span>
          )}
        </div>
        {githubIntegrationDone && (
          <>
            <div className="integration-config-container">
              <span className="title">
                Select Github Repository for your applications.
              </span>
              <div className="integration-apps-container">
                {applications?.length > 0 &&
                  applications.map((app: App, index: number) => (
                    <div className="integration-app" key={index}>
                      <span className="app-name">{app?.appName}</span>
                      {loadingGithubData ? (
                        <div className="integration-dropdown-container">
                          <Skeleton className="integration-skeleton jira-project" />
                        </div>
                      ) : (
                        <div className="integration-dropdown-container">
                          <Select
                            defaultValue={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.GITHUB?.repoName
                                : ""
                            }
                            value={
                              app.integrationKeyMap
                                ? app?.integrationKeyMap?.GITHUB?.repoName
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];
                              temp = applications.map((item: App) => {
                                if (item.appId === app.appId) {
                                  item.integrationKeyMap = {
                                    ...item.integrationKeyMap,
                                    GITHUB: {
                                      integrationType: "GITHUB",
                                      repoName: e,
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
                                app?.integrationKeyMap?.GITHUB?.repoName
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              }`}
                            >
                              <SelectValue
                                placeholder="Select Repository on Github"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Select Repository on Github
                                </SelectItem>
                                {githubRepositories?.length > 0 &&
                                  githubRepositories?.map(
                                    (repo: GitHubRepo, index: number) => (
                                      <SelectItem
                                        value={repo.name}
                                        className="integration-dropdown-item "
                                        key={index}
                                      >
                                        {repo.name}
                                      </SelectItem>
                                    ),
                                  )}
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
      {githubIntegrationDone && (
        <div>
          <Button
            className="save-config"
            variant="default"
            disabled={loading || !showButton}
            onClick={() => {
              setGithubRepository();
            }}
          >
            {loading ? <SpinLoader /> : "Save Configuration"}
          </Button>
        </div>
      )}
    </div>
  );
};

export default Github;
