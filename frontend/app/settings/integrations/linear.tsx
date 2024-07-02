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
  GetLinearAuth,
  GetLinearProjects,
  LinearCallback,
  SetProjectKeys,
} from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import Image from "next/image";
import { Skeleton } from "@/components/ui/skeleton";
import {
  LineaNode,
  LinearIntegrationProps,
  LinearIntegrationRequestBody,
  LinearProject,
  LinearTeam,
} from "@/app/types/linear-types";
import { App } from "@/app/types/application-types";

/**
 * Functional component representing the Linear integration settings page.
 * Handles authentication, fetching projects, setting project keys, and displaying UI elements.
 */

const Linear = ({
  setIntegrationSelected,
  fetchIntegrations,
  integrationsDone,
  showDeleteIntegration,
  applications,
  setApplications,
  setDeleteAlert,
}: LinearIntegrationProps) => {
  const [teamsOfLinear, setteamsOfLinear] = useState<LinearTeam[]>([]);
  const [loading, setLoading] = useState(false);
  const [showButton, setShowButton] = useState<boolean>(false);
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const { toast } = useToast();
  const router = useRouter();
  const linearIntegrationDone = integrationsDone.includes("linear");
  const [loadingLinearData, setLoadingLinearData] = useState(false);

  useEffect(() => {
    let temp = false;
    applications.map((app: App) => {
      if (app?.integrationKeyMap && app?.integrationKeyMap?.LINEAR) {
        if (app?.integrationKeyMap?.LINEAR?.teamId) {
          temp = true;
        } else {
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
        linearIntegrationDone && getUpdatedLinearProjects();
      }
    }
  }, []);

  const makeCallback = async (code: string) => {
    try {
      const res = await LinearCallback(code);
      if (res.success) {
        toast({
          description: res.message,
        });
        await fetchIntegrations();
        await getUpdatedLinearProjects();
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

  const getUpdatedLinearProjects = async () => {
    setLoadingLinearData(true);
    try {
      const { data, message, success } = await GetLinearProjects();
      if (success) {
        setteamsOfLinear([...data?.data?.teams?.nodes]);
        setLoadingLinearData(false);
      } else {
        setLoadingLinearData(false);
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      setLoadingLinearData(false);
    }
  };

  const setLinearProject = async () => {
    setLoading(true);
    let body: LinearIntegrationRequestBody[] = [];

    applications.map((app: App) => {
      if (app.integrationKeyMap && app.integrationKeyMap?.LINEAR?.teamId) {
        body.push({
          appId: app.appId,
          integrationType: "LINEAR",
          teamId: app.integrationKeyMap?.LINEAR?.teamId,
          projectId: app.integrationKeyMap?.LINEAR?.projectId,
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

  const linearAuth = async () => {
    setLoading(true);
    try {
      const { data, message, success } = await GetLinearAuth();
      if (success) {
        console.log("response is", data);
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
        description: "Something wen’t wrong, please try again.",
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
          <h1 className="title">Linear</h1>
          <p className="sub-title">Export bugs as Issues on Linear</p>
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
                src="/icons/linear.svg"
                alt="linear"
                className="logo"
              />
              <span className="integration-message">
                {linearIntegrationDone
                  ? "Your integration is active"
                  : "Export bugs as issues on Linear"}
              </span>
            </div>
            {!linearIntegrationDone ? (
              <Button
                className="integration-connect"
                onClick={() => {
                  linearAuth();
                }}
              >
                <Image
                  priority
                  width={32}
                  height={32}
                  src="/icons/linear.svg"
                  alt="linear"
                  className="bg-none"
                />
                Integrate Linear
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
          {!linearIntegrationDone && <hr className="divider" />}{" "}
          {!linearIntegrationDone && (
            <span className="integration-description">
              Authenticate with your Linear account and export bug tickets to
              your teams and projects.
            </span>
          )}
        </div>
        {linearIntegrationDone && (
          <>
            <div className="integration-config-container">
              <span className="title">
                Select Teams and Projects to export tickets
              </span>
              <div className="integration-apps-container">
                {applications?.length > 0 &&
                  applications.map((app: App, index: number) => (
                    <div className="integration-app" key={index}>
                      <span className="app-name">{app?.appName}</span>

                      {loadingLinearData ? (
                        <div className="integration-dropdown-container">
                          <Skeleton className="integration-skeleton linear-channel" />
                          <Skeleton className="integration-skeleton linear-channel" />
                        </div>
                      ) : (
                        <div className="integration-dropdown-container">
                          <Select
                            defaultValue={
                              app.integrationKeyMap
                                ? app.integrationKeyMap?.LINEAR?.teamId
                                : ""
                            }
                            value={
                              app.integrationKeyMap
                                ? app.integrationKeyMap?.LINEAR?.teamId
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];
                              temp = applications.map((item: App) => {
                                if (item.appId === app.appId) {
                                  item.integrationKeyMap = {
                                    ...item.integrationKeyMap,
                                    LINEAR: {
                                      teamId: e,
                                      projectId: "",
                                      integrationType: "LINEAR",
                                    },
                                  };
                                }
                                return item;
                              });
                              setApplications(temp.slice(0));
                            }}
                          >
                            <SelectTrigger
                              className={`integration-dropdown-trigger linear-channel ${
                                app?.integrationKeyMap?.LINEAR?.teamId
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              }`}
                            >
                              <SelectValue
                                placeholder="Select a Team"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Select a Team
                                </SelectItem>
                                {teamsOfLinear?.length > 0 &&
                                  teamsOfLinear?.map(
                                    (team: LinearTeam, index: number) => (
                                      <SelectItem
                                        value={team.id}
                                        className="integration-dropdown-item"
                                        key={index}
                                      >
                                        {team.name}
                                      </SelectItem>
                                    ),
                                  )}
                              </SelectGroup>
                            </SelectContent>
                          </Select>

                          <Select
                            defaultValue={
                              app.integrationKeyMap
                                ? app.integrationKeyMap?.LINEAR?.projectId
                                : ""
                            }
                            value={
                              app.integrationKeyMap
                                ? app.integrationKeyMap?.LINEAR?.projectId
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];

                              temp = applications.map((item: App) => {
                                if (
                                  item.appId === app.appId &&
                                  item.integrationKeyMap.LINEAR
                                ) {
                                  item.integrationKeyMap,
                                    (item.integrationKeyMap.LINEAR.projectId =
                                      e);
                                }
                                return item;
                              });
                              setApplications(temp.slice(0));
                            }}
                          >
                            <SelectTrigger
                              className={`integration-dropdown-trigger linear-channel ${
                                app?.integrationKeyMap?.LINEAR?.projectId
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              } `}
                            >
                              <SelectValue
                                placeholder=" Select Project"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Select Project
                                </SelectItem>

                                {app.integrationKeyMap &&
                                  app.integrationKeyMap?.LINEAR?.teamId &&
                                  teamsOfLinear
                                    .filter(
                                      (team: LinearTeam) =>
                                        team.id ===
                                        app.integrationKeyMap?.LINEAR?.teamId,
                                    )[0]
                                    ?.projects?.nodes?.map(
                                      (project: LineaNode, index: number) => (
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
                        </div>
                      )}
                    </div>
                  ))}
              </div>
            </div>
          </>
        )}
      </div>
      {linearIntegrationDone && (
        <div>
          <Button
            className="save-config"
            variant="default"
            disabled={loading || !showButton}
            onClick={() => {
              setLinearProject();
            }}
          >
            {loading ? <SpinLoader /> : "Save Configuration"}
          </Button>
        </div>
      )}
    </div>
  );
};

export default Linear;
