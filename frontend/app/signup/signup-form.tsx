"use client";
import React, { useState, useEffect } from "react";
import { signIn } from "next-auth/react";
import Image from "next/image";
import { useRouter, useSearchParams } from "next/navigation";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { HorizontalDevider } from "@/components/ui/horizontal-devider";
import { Envelope, LockKey } from "../lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { signUpCall } from "../apis/authapis";
import { useToast } from "@/components/ui/use-toast";
import { ApiError } from "../types/organisation-types";

const validationSchema = z.object({
  email: z.string().min(1, { message: "Email is required" }).email({
    message: "Must be a valid email",
  }),
  password: z
    .string()
    .min(6, { message: "Password must be atleast 6 characters" }),
});

type ValidationSchema = z.infer<typeof validationSchema>;

/**
 * A component that renders a sign-up form.
 *
 * This component is responsible for rendering a form where users can sign up with their work email and password. It also provides an option to sign up using Google Workspace.
 *
 * @returns {JSX.Element} The sign-up form component.
 */

export default function SignUpForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ValidationSchema>({
    resolver: zodResolver(validationSchema),
  });

  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const onSubmit: SubmitHandler<ValidationSchema> = async (data) => {
    const { email, password } = data;

    setLoading(true);
    try {
      const userData = await signUpCall({
        username: "",
        workEmail: email,
        password: password,
        profileImage: "",
        coverImage: "",
        signUpType: "CREDENTIALS",
      });

      if (userData.status === 201) {
        await signIn("credentials", {
          redirect: false,
          email,
          password,
          callbackUrl: "/verify",
        });
        router.push("/verify");
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
    }
  };

  const signInWithGoogle = async () => {
    try {
      const res = await signIn("google", {
        redirect: false,
      });
      if (res?.error) {
        const errorData = JSON.parse(res?.error);

        toast({
          description: errorData?.errors.message,
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
    }
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-field">
          <Label htmlFor="picture">Work Email</Label>
          <Input
            Icon={<Envelope size={16} className="icon" />}
            placeholder="john.doe@company.com"
            {...register("email")}
          />
          {errors.email && (
            <p className="error-text">{errors.email?.message}</p>
          )}
        </div>
        <div className="form-field">
          <Label htmlFor="picture">Password</Label>
          <Input
            Icon={<LockKey size={16} className="icon" />}
            placeholder="**************"
            type="password"
            className="w-full"
            {...register("password")}
          />
          {errors.password && (
            <p className="error-text">{errors.password?.message}</p>
          )}
        </div>
        <Button className="auth-button" disabled={loading}>
          {loading ? <SpinLoader /> : <p>Sign up</p>}
        </Button>
      </form>

      <div className="divider">
        <HorizontalDevider />
      </div>

      <Button
        variant="outline"
        className="google-auth"
        onClick={() => signInWithGoogle()}
      >
        <Image src="/icons/google.svg" alt="google" height="20" width="20" />{" "}
        Continue with Google
      </Button>
    </div>
  );
}
