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
  GetChannels,
  GetSlackAuth,
  SetProjectKeys,
  SlackCallback,
} from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import Image from "next/image";
import { Skeleton } from "@/components/ui/skeleton";
import {
  SlackChannel,
  SlackIntegrationProps,
  SlackIntegrationRequestBody,
} from "@/app/types/slack-types";
import { App } from "@/app/types/application-types";

/**
 * Component for managing Slack integration settings.
 * Handles authentication, fetching channels, setting project keys, and UI interactions.
 */

const Slack = ({
  setIntegrationSelected,
  fetchIntegrations,
  showDeleteIntegration,
  integrationsDone,
  applications,
  setApplications,
  setDeleteAlert,
}: SlackIntegrationProps) => {
  const [channels, setChannels] = useState<SlackChannel[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingChannels, setLoadingChannels] = useState(false);
  const [showButton, setShowButton] = useState<boolean>(false);
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const { toast } = useToast();
  const router = useRouter();
  const slackIntegrationDone = integrationsDone.includes("slack");

  useEffect(() => {
    if (applications && slackIntegrationDone) {
      getChannels(applications[0]?.appId);
      const temp = applications
        ?.map((app: App) => {
          return {
            ...app,
          };
        })
        .slice(0);
      setApplications(temp.slice(0));
    }
  }, [applications, slackIntegrationDone]);

  useEffect(() => {
    if (code) {
      makeCallback(code);
    }
  }, [code]);

  useEffect(() => {
    let temp = false;

    applications.forEach((app: App) => {
      const integrationKeyMap = app?.integrationKeyMap?.SLACK;
      if (integrationKeyMap) {
        if (integrationKeyMap.channelId) {
          temp = true;
        }
      }
    });

    setShowButton(temp);
  }, [applications]);

  const slackAuth = async () => {
    setLoading(true);
    try {
      const { data, message, success } = await GetSlackAuth();
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

  const getChannels = async (id: string) => {
    setLoadingChannels(true);
    try {
      const { data, message, success } = await GetChannels(id);
      if (success) {
        setChannels([...data]);
        setLoadingChannels(false);
      } else {
        setLoadingChannels(false);
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      setLoadingChannels(false);
    }
  };

  const makeCallback = async (code: string) => {
    try {
      const res = await SlackCallback(code);

      if (res.success) {
        toast({
          description: res.message,
        });
        await fetchIntegrations();
        await getChannels(applications[0]?.appId);
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

  const setSlackProject = async () => {
    setLoading(true);
    let body: SlackIntegrationRequestBody[] = [];
    applications.map((app: App) => {
      if (app.integrationKeyMap && app?.integrationKeyMap?.SLACK?.channelId) {
        body.push({
          appId: app.appId,
          integrationType: "SLACK",
          channelId: app?.integrationKeyMap?.SLACK?.channelId,
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
          <h1 className="title">Slack</h1>
          <p className="sub-title">Get notified of new bugs on slack</p>
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
                src="/icons/slack.svg"
                alt="slack"
                className="logo"
              />
              <span className="integration-message">
                {slackIntegrationDone
                  ? "Your integration is active"
                  : "Get instant alerts on Slack"}
              </span>
            </div>
            {!slackIntegrationDone ? (
              <Button
                className="integration-connect"
                disabled={loading}
                onClick={() => {
                  slackAuth();
                }}
              >
                <Image
                  priority
                  width={32}
                  height={32}
                  src="/icons/slack.svg"
                  alt="slack"
                  className="logo"
                />
                {loading ? <SpinLoader /> : "Integrate Slack"}
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
          <hr className="divider" />
          <span className="integration-description">
            {slackIntegrationDone ? (
              <>
                If you can't view channels, make sure to add <b>Heimdall</b> to
                your channel so it appears in the list.{" "}
                <Link
                  aria-label="Click here if you are facing issues in selecting channels."
                  href="https://quash.notion.site/Slack-Integration-Guide-aa8f6976cdce4f6d95a43f951b0308ee?pvs=74"
                  className=" text-[#3762BB] hover:text-[#3740bb]"
                  target="_blank"
                >
                  Click here to know more.
                </Link>
              </>
            ) : (
              "Get a detailed notification instantly whenever a bug is reported."
            )}
          </span>
        </div>
        {slackIntegrationDone && (
          <>
            <div className="integration-config-container">
              <span className="title">
                Select Channels to get updates on Slack
              </span>
              <div className="integration-apps-container">
                {applications?.length > 0 &&
                  applications?.map((app: App, index: number) => (
                    <div className="integration-app" key={index}>
                      <span className="app-name">{app?.appName}</span>
                      <div className="integration-dropdown-container">
                        {loadingChannels ? (
                          <Skeleton className="integration-skeleton slack-channel" />
                        ) : (
                          <Select
                            defaultValue={
                              app?.integrationKeyMap
                                ? app?.integrationKeyMap?.SLACK?.channelId || ""
                                : ""
                            }
                            value={
                              app?.integrationKeyMap
                                ? app?.integrationKeyMap?.SLACK?.channelId || ""
                                : ""
                            }
                            onValueChange={(e: string) => {
                              let temp: App[] = [];
                              temp = applications.map((item: App) => {
                                if (item.appId === app.appId) {
                                  item.integrationKeyMap = {
                                    SLACK: {
                                      channelId: e,
                                      integrationType: "SLACK",
                                    },
                                  };
                                }
                                return item;
                              });
                              setApplications(temp.slice(0));
                            }}
                          >
                            <SelectTrigger
                              className={`integration-dropdown-trigger slack-channel ${
                                app?.integrationKeyMap?.SLACK?.channelId
                                  ? "integration-text-main"
                                  : "integration-sub-text"
                              }`}
                            >
                              <SelectValue
                                placeholder="Select a Channel"
                                className="integration-dropdown-item"
                              />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup className="flex max-h-56 overflow-y-scroll flex-col">
                                <SelectItem
                                  value=""
                                  className="integration-dropdown-placeholder"
                                >
                                  Select a Channel
                                </SelectItem>
                                {channels?.length > 0 &&
                                  channels?.map(
                                    (channel: SlackChannel, index: number) => (
                                      <SelectItem
                                        value={channel.channelId}
                                        className="integration-dropdown-item"
                                        key={index}
                                      >
                                        {channel.channelName}
                                      </SelectItem>
                                    ),
                                  )}
                              </SelectGroup>
                            </SelectContent>
                          </Select>
                        )}
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          </>
        )}
      </div>
      {slackIntegrationDone && (
        <div>
          <Button
            className="save-config"
            variant="default"
            disabled={loading || !showButton}
            onClick={() => {
              setSlackProject();
            }}
          >
            {loading ? <SpinLoader /> : "Save Configuration"}
          </Button>
        </div>
      )}
    </div>
  );
};

export default Slack;
