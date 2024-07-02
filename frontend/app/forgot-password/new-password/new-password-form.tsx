"use client";

import React, { useState } from "react";
import { useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { LockKey } from "../../lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { UpdatePassword } from "@/app/apis/forgot-passwordapi";
import { useToast } from "@/components/ui/use-toast";

const validationSchema = z
  .object({
    password: z
      .string()
      .min(6, { message: "Password must be atleast 6 characters" }),
    confirmPassword: z
      .string()
      .min(1, { message: "Confirm Password is required" }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Password don't match",
  });

type ValidationSchema = z.infer<typeof validationSchema>;

/**
 * Renders a form for updating a user's password.
 *
 * This component uses the `useForm` hook from the `react-hook-form` library to handle form validation and submission.
 * It also utilizes the `zodResolver` from the `@hookform/resolvers/zod` package to validate the form data using a Zod schema.
 * The form fields include a "Password" field and a "Confirm Password" field, both of which are of type "password".
 * The form submission triggers an API call to update the user's password using the `UpdatePassword` function from the `forgot-passwordapi` module.
 * If the password update is successful, the user is redirected to the "/forgotpassword/password-updated" page.
 * If there is an error during the password update process, an error toast is displayed using the `useToast` hook.
 *
 * @returns {JSX.Element} The rendered NewPasswordForm component.
 */

export default function NewPasswordForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ValidationSchema>({
    resolver: zodResolver(validationSchema),
  });

  const searchParams = useSearchParams();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const onSubmit: SubmitHandler<ValidationSchema> = async (data) => {
    const { password } = data;

    const body = {
      newPassword: password,
    };
    setLoading(true);
    try {
      const authToken = searchParams.get("authToken");
      const changePasswordRes = await UpdatePassword(body, authToken);

      if (changePasswordRes.success) {
        router.push("/forgotpassword/password-updated");
      }
    } catch (error) {
      console.log(error);
      setLoading(false);
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    }
    setLoading(false);
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-field">
          <Label htmlFor="picture">Password</Label>
          <Input
            Icon={<LockKey size={16} className="icon" />}
            type="password"
            placeholder="**************"
            {...register("password")}
          />
          {errors.password && (
            <p className="error-text">{errors.password?.message}</p>
          )}
        </div>
        <div className="form-field">
          <Label htmlFor="picture">Confirm Password</Label>
          <Input
            Icon={<LockKey size={16} className="icon" />}
            type="password"
            placeholder="**************"
            {...register("confirmPassword")}
          />

          {errors.confirmPassword && (
            <p className="error-text">{errors.confirmPassword?.message}</p>
          )}
        </div>
        <Button className="update-password" disabled={loading}>
          {loading ? <SpinLoader /> : <p> Update Password</p>}
        </Button>
      </form>
    </div>
  );
}
