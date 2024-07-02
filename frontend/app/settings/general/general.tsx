"use client";
import React, { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { useCopyToClipboard } from "@/hooks/use-copy-to-clip-board";
import { Copy } from "../../lib/icons";
import { useToast } from "@/components/ui/use-toast";
import { useSearchParams } from "next/navigation";
import { signOut } from "next-auth/react";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { PatchUser } from "@/app/apis/generalapi";
import SpinLoader from "@/components/ui/spinner";

import {
  TeamMemberDetails,
  userDataApiResponse,
} from "@/app/types/organisation-types";

const generalSettingsSchema = z.object({
  name: z.string().min(1, "Name must be atleast 1 character").max(100),
  role: z.string().min(1, "Role must be atleast 1 character").max(100),
});
type generalSettingsSchema = z.infer<typeof generalSettingsSchema>;

/**
 * Functional component General that handles user settings and workspace settings.
 * It uses various UI components like Input, Label, Button, and SpinLoader.
 * Manages form submission, form validation, user data fetching, and state management.
 * Utilizes custom hooks like useCopyToClipboard, useToast, and useForm from react-hook-form.
 */

const General = ({ data }: { data: userDataApiResponse }) => {
  const searchParams = useSearchParams();
  const { toast } = useToast();
  const [value, copy] = useCopyToClipboard();
  const [userData, setUserData] = useState<TeamMemberDetails | null>(null);
  const [buttonDisabled, setButtonDisabled] = useState(true);
  const [users, setUsers] = useState({ name: "", role: "" });
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<generalSettingsSchema>({
    resolver: zodResolver(generalSettingsSchema),
  });

  const onSubmit: SubmitHandler<generalSettingsSchema> = async (formData) => {
    const { name, role } = formData;
    const body = {
      fullName: name,
      userOrganisationRole: role,
    };
    setLoading(true);
    try {
      const userDetails: userDataApiResponse = await PatchUser(body);

      if (userDetails?.success) {
        toast({
          description: userDetails.message,
        });
      } else {
        toast({
          description: userDetails.message,
          typeof: "error",
        });
        setLoading(false);
      }
    } catch (error) {
      setLoading(false);
      console.log(error);
    }
    setLoading(false);
  };

  useEffect(() => {
    function getUserDetails() {
      const userDetails = data;
      if (userDetails?.success) {
        setUserData(userDetails?.data);
      }
    }
    if (data) {
      getUserDetails();
    } else {
      handleSignOut();
    }
  }, []);

  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };

  useEffect(() => {
    if (userData) {
      setUsers({
        name: userData?.user?.fullName,
        role: userData?.user?.userOrganisationRole,
      });
    }
  }, [userData?.user?.fullName, userData?.user?.userOrganisationRole]);

  useEffect(() => {
    reset(users);
  }, [users]);

  const handleButtonDisable = () => {
    setButtonDisabled(false);
  };

  return (
    <div className="general">
      <div className="head">
        <h1 className="title"> General</h1>
        <p className="sub-title">
          Customize your profile, workspace, and more{" "}
        </p>
      </div>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="general-form-container ">
          <div className="general-form ">
            <div className="section">
              <h1 className="section-title">User Settings</h1>
              <div className="general-form-field">
                <Label htmlFor="username" className="label">
                  User Name
                </Label>
                <Input
                  {...register("name", {
                    onChange: handleButtonDisable,
                  })}
                />
                {errors.name && (
                  <p className="text-xs error-text mt-2">
                    {errors.name?.message}
                  </p>
                )}
              </div>
              <div className="general-form-field">
                <Label htmlFor="email" className="label">
                  Email
                </Label>
                <Input
                  name="email"
                  defaultValue={userData?.user?.workEmail}
                  disabled
                />
              </div>
              <div className="general-form-field">
                <Label htmlFor="userrole" className="label">
                  User Role
                </Label>
                <Input
                  {...register("role", {
                    onChange: handleButtonDisable,
                  })}
                />
                {errors.role && (
                  <p className="text-xs error-text mt-2">
                    {errors.role?.message}
                  </p>
                )}
              </div>
            </div>

            <hr className={`divider`} />

            <div className="section">
              <h1 className="section-title">Workspace Settings</h1>
              <div className="general-form-field">
                <Label htmlFor="companyname" className="label">
                  Company Name
                </Label>
                <Input defaultValue={userData?.organisation?.name} disabled />
              </div>
              <div className="general-form-field">
                <Label htmlFor="applicationkey" className="label">
                  Application Key
                </Label>
                <div
                  className="application-key-container"
                  onClick={() => {
                    if (userData) {
                      copy(userData?.organisation?.orgUniqueKey);
                      toast({
                        description: "Key is copied to clipboard",
                      });
                    }
                  }}
                >
                  <div>
                    <Copy className="copy-icon " />
                  </div>
                  <p className="app-key">
                    {userData?.organisation?.orgUniqueKey}
                  </p>
                </div>
              </div>
            </div>

            <hr className={`divider`} />

            <Button
              className="save"
              disabled={buttonDisabled || loading}
              type="submit"
            >
              {loading == true ? <SpinLoader /> : null}
              {loading == false ? <p> Save Changes</p> : null}
            </Button>
          </div>
          <div className="general-form-footer">
            <p>
              For documentation, visit{" "}
              <a
                href="https://quash.notion.site/Quash-SDK-Developer-Documentation-534ebd4c995040b2ae536dd139609d47?pvs=4"
                target="_blank"
                rel="noopener noreferrer"
                className="link"
              >
                www.quashbugs.com/documentation
              </a>
            </p>

            <p>
              Contact us at{" "}
              <a href="mailto:hello@quashbugs.com" className="link">
                hello@quashbugs.com
              </a>
              , or reach out directly at +91-9599909197
            </p>
          </div>
        </div>
      </form>
    </div>
  );
};

export default General;
