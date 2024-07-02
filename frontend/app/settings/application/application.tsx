"use client";
import React, { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { AndroidLogo, AppleLogo, Plus, Trash } from "../../lib/icons";
import { useRouter } from "next/navigation";
import { useToast } from "@/components/ui/use-toast";
import { capitalizeFirstCharacter } from "@/app/utils/helper";
import { deleteApp } from "@/app/apis/applicationapis";
import { signOut } from "next-auth/react";
import SuspenseWrapper from "@/app/dashboard/components/suspense-wrapper";
import {
  ApiError,
  Organisation,
  OrganisationApiResponse,
} from "@/app/types/organisation-types";
import { App } from "@/app/types/application-types";
import DeleteAppDialog from "./components/delete-app-dialog";

const Application = ({ orgData }: { orgData: OrganisationApiResponse }) => {
  const { toast } = useToast();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [deleteAlert, setDeleteAlert] = useState(false);
  const [organisationData, setOrganisationData] = useState<Organisation | null>(
    null,
  );
  const [applications, setApplications] = useState<App[]>([]);
  const [selectedApp, setSelectedApp] = useState<App | null>(null);
  const localApp =
    typeof window !== "undefined"
      ? window.localStorage.getItem("appselected")
      : null;

  useEffect(() => {
    const fetchOrgData = () => {
      try {
        const { data, success } = orgData;
        if (success) {
          setOrganisationData(data);
          setApplications(data?.organisationApps?.slice());
        }
      } catch (error) {
        handleSignOut();
        console.error(error);
      }
    };

    fetchOrgData();
  }, [orgData]);

  const deleteApplication = async (id: string) => {
    setLoading(true);
    try {
      const { message, success } = await deleteApp(id);
      if (success) {
        if (localApp) {
          {
            localApp === id && window.localStorage.removeItem("appselected");
          }
        }
        const updatedApplications = applications.filter(
          (app: App) => app.appId !== id,
        );
        setApplications(updatedApplications);
        toast({ description: message });
      } else {
        toast({
          description: message || "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error) {
      const apiError = error as ApiError;
      toast({
        description:
          apiError?.data?.message ?? "Something went wrong. Please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
      setDeleteAlert(false);
    }
  };

  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };

  const handleAddApplication = () => {
    router.push("/add-quash?source=settings");
  };

  return (
    <div className="application">
      <div className="head">
        <h1 className="title"> Applications</h1>
        <p className="sub-title">Manage and add your applications</p>
      </div>
      <div className="body">
        <div>
          <Button className="add-app" onClick={handleAddApplication}>
            <Plus />
            Add Application
          </Button>
        </div>

        {organisationData &&
          applications.length > 0 &&
          applications.map((app: App, index: number) => (
            <div className="app" key={index}>
              <div className="names ">
                <span className="app-name">{app.appName}</span>
                <span className="package-name">{app.packageName}</span>
              </div>
              <div className="app-type">
                {app.appType.toLowerCase() === "android" ? (
                  <AndroidLogo size={16} className="icon" />
                ) : (
                  <AppleLogo size={16} className="icon" />
                )}

                <span className="app-type-name">
                  {capitalizeFirstCharacter(app.appType)}
                </span>
              </div>
              <div
                className={`remove`}
                onClick={() => {
                  setSelectedApp(app);
                  setDeleteAlert(true);
                }}
              >
                <Trash size={16} className="icon" />
                <span className="error-text">Remove</span>
              </div>
            </div>
          ))}
      </div>

      {selectedApp && deleteAlert && (
        <SuspenseWrapper>
          <DeleteAppDialog
            deleteAlert={deleteAlert}
            selectedApp={selectedApp}
            setDeleteAlert={setDeleteAlert}
            deleteApplication={deleteApplication}
            loading={loading}
          />
        </SuspenseWrapper>
      )}
    </div>
  );
};

export default Application;
