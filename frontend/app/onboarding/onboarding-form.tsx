"use client";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { signOut, useSession } from "next-auth/react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import SpinLoader from "@/components/ui/spinner";
import { useEffect } from "react";
import { createOrganization, updateUserDetails } from "../apis/authapis";
import { useToast } from "@/components/ui/use-toast";
import PhoneInput from "@/components/ui/phone-input";

const validationSchema = z.object({
  name: z.string().min(1, "Name must be atleast 1 character").max(100),
  role: z.string().min(1, "Role must be atleast 1 character").max(100),
  company: z
    .string()
    .min(1, { message: "Company name must be atleast 1 character" })
    .max(200),
  phoneNumber: z
    .string()
    .min(1, "Phone number must be atleast 1 character")
    .max(100),
});

type ValidationSchema = z.infer<typeof validationSchema>;

const validationOnBoardingExistingOrganizationSchema = z.object({
  name: z.string().min(1, "Name must be atleast 1 character").max(100),
  role: z.string().min(1, "Role must be atleast 1 character").max(100),
});

type validationOnBoardingExistingOrganizationSchema = z.infer<
  typeof validationOnBoardingExistingOrganizationSchema
>;

export default function OnBoardingForm({
  isOrganizationPresent,
}: {
  isOrganizationPresent: boolean;
}) {
  const { data: session, update } = useSession();
  const [loading, setLoading] = useState(false);
  const [countryCode, setCountryCode] = useState("+91");
  const [shouldRedirect, setShouldRedirect] = useState(false);
  const [shouldRedirectOrganization, setShouldRedirectOrganization] =
    useState(false);
  const router = useRouter();
  const { toast } = useToast();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ValidationSchema>({
    resolver: zodResolver(validationSchema),
  });

  const {
    register: registerOrganization,
    handleSubmit: handleOrganizationSubmit,
    formState: { errors: organizationErrors },
  } = useForm<validationOnBoardingExistingOrganizationSchema>({
    resolver: zodResolver(validationOnBoardingExistingOrganizationSchema),
  });

  const handleSignOut = async () => {
    await signOut();
  };

  useEffect(() => {
    if (shouldRedirect === true) {
      router.push("/add-quash");
    }
    if (shouldRedirectOrganization === true) {
      router.push("/dashboard");
    }
  }, [shouldRedirect, shouldRedirectOrganization, router]);

  const onSubmitOrganization: SubmitHandler<
    validationOnBoardingExistingOrganizationSchema
  > = async (data) => {
    const { name, role } = data;
    setLoading(true);

    try {
      const userDetails = await updateUserDetails({
        fullName: name,
        userOrganisationRole: role,
      });

      if (userDetails && userDetails.status === 200) {
        setShouldRedirectOrganization(true);
        await update({
          ...session,
          user: {
            ...session?.user,
            shouldNavigateToDashboard: true,
            fullName: userDetails.data.data.fullName,
          },
          data: {
            ...session?.data,
            shouldNavigateToDashboard: true,
            fullName: userDetails.data.data.fullName,
          },
        });
      }
    } catch (error: any) {
      setLoading(false);
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    }
    setLoading(false);
  };

  const onSubmit: SubmitHandler<ValidationSchema> = async (data) => {
    const { name, role, company, phoneNumber } = data;
    const fullPhoneNumber = `${countryCode}${phoneNumber}`;

    setLoading(true);

    try {
      const res = await createOrganization({
        fullName: name,
        organisationRole: role,
        organisationName: company,
        phoneNumber: fullPhoneNumber,
      });
      const properties = {
        company: company,
        role: role,
      };
      if (res.status === 200) {
        setShouldRedirect(true);
        await update({
          ...session,
          user: {
            ...session?.user,
            shouldNavigateToDashboard: true,
            fullName: res.data.data.createdBy.fullName,
          },
          data: {
            ...session?.data,
            shouldNavigateToDashboard: true,
            fullName: res.data.data.createdBy.fullName,
          },
        });
      }
    } catch (error: any) {
      setLoading(false);

      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    }
    setLoading(false);
  };

  if (!isOrganizationPresent) {
    return (
      <div className="onboarding-form-container">
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="form-field">
            <Label>Name*</Label>
            <Input {...register("name")} placeholder="e.g. John Doe" />
            {errors.name && (
              <p className="error-text">{errors.name?.message}</p>
            )}
          </div>

          <div className="form-field">
            <Label>Role*</Label>
            <Input {...register("role")} placeholder="e.g. Developer" />
            {errors.role && (
              <p className="error-text">{errors.role?.message}</p>
            )}
          </div>

          <div className="form-field">
            <Label>Company*</Label>
            <Input {...register("company")} placeholder="e.g. Quash" />
            {errors.company && (
              <p className="error-text">{errors.company?.message}</p>
            )}
          </div>
          <PhoneInput
            register={register}
            errors={errors}
            setCountryCode={setCountryCode}
            countryCode={countryCode}
          />

          <Button className="submit" type="submit" disabled={loading}>
            {loading == true ? <SpinLoader /> : null}
            {loading == false ? <p>Generate Key</p> : null}
          </Button>
        </form>

        <p className="onboarding-form-footer">
          I don’t want to continue here{" "}
          <Link href="/" className="link" onClick={handleSignOut}>
            Sign me out
          </Link>
        </p>
      </div>
    );
  } else {
    return (
      <div className="onboarding-form-container">
        <form onSubmit={handleOrganizationSubmit(onSubmitOrganization)}>
          <div className="form-field">
            <Label htmlFor="picture">Name</Label>
            <Input
              {...registerOrganization("name")}
              placeholder="e.g. John Doe"
            />
            {organizationErrors.name && (
              <p className="error-text">{organizationErrors.name?.message}</p>
            )}
          </div>

          <div className="form-field">
            <Label htmlFor="picture">Role</Label>
            <Input
              {...registerOrganization("role")}
              placeholder="e.g. Developer"
            />
            {organizationErrors.role && (
              <p className="error-text">{organizationErrors.role?.message}</p>
            )}
          </div>

          <Button className="submit" type="submit" disabled={loading}>
            {loading == true ? <SpinLoader /> : null}
            {loading == false ? <p>Finish</p> : null}
          </Button>
        </form>
        <p className="onboarding-form-footer">
          Don't want to join this team?{" "}
          <Link href="/" className="link" onClick={handleSignOut}>
            Sign in
          </Link>{" "}
          with a different account.
        </p>
      </div>
    );
  }
}
