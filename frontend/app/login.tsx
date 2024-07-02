"use client";
import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Image from "next/image";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { signIn } from "next-auth/react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { HorizontalDevider } from "@/components/ui/horizontal-devider";
import { Envelope, LockKey } from "./lib/icons";

import Link from "next/link";
import { useToast } from "@/components/ui/use-toast";
import SpinLoader from "@/components/ui/spinner";
import { ApiError } from "./types/organisation-types";

const loginSchema = z.object({
  email: z.string().min(1, { message: "Email is required" }).email({
    message: "Must be a valid email",
  }),
  password: z
    .string()
    .min(6, { message: "Password must be atleast 6 characters" }),
});
type loginSchema = z.infer<typeof loginSchema>;

/**
 * Renders a login form with email and password fields.
 * Handles form submission and authentication using NextAuth.
 * Displays error messages and loading spinner.
 *
 * @returns {JSX.Element} The rendered login form.
 */

export default function Login() {
  const searchParams = useSearchParams();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<loginSchema>({
    resolver: zodResolver(loginSchema),
  });
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const onSubmit: SubmitHandler<loginSchema> = async (data) => {
    const { email, password } = data;

    setLoading(true);
    try {
      const res = await signIn("credentials", {
        redirect: false,
        email,
        password,
      });

      if (res?.error) {
        const errorData = JSON.parse(res?.error);
        toast({
          description:
            errorData.errors.message ||
            "Something went wrong. Please try again.",
          typeof: "error",
        });
      } else {
        window.location.reload();
      }
    } catch (error: unknown) {
      console.log(error);
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
          apiError?.response?.data?.message ??
          "Something went wrong. Please try again.",
        typeof: "error",
      });
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
          <div className="flex justify-between items-center">
            <Label htmlFor="picture">Password</Label>
          </div>

          <Input
            Icon={<LockKey size={16} className="icon" />}
            placeholder="**************"
            type="password"
            {...register("password")}
          />
          {errors.password && (
            <p className="error-text">{errors.password?.message}</p>
          )}
        </div>
        <div className="forgot-password">
          <Link
            href={{
              pathname: "/forgot-password",
              query: { source: "login" },
            }}
            className="link"
          >
            Forgot Password
          </Link>
        </div>
        <Button className="auth-button" disabled={loading}>
          {loading ? <SpinLoader /> : <p>Sign in</p>}
        </Button>
      </form>
      <div className="divider">
        <HorizontalDevider />
      </div>

      <Button
        variant="outline"
        className="google-auth "
        onClick={() => signInWithGoogle()}
      >
        <Image src="/icons/google.svg" alt="google" height="20" width="20" />{" "}
        Continue with Google
      </Button>
    </div>
  );
}
