"use client";
import { useState } from "react";
import { SubmitHandler, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Envelope } from "../lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { useRouter, useSearchParams } from "next/navigation";
import { forgotPassword } from "../apis/authapis";
import { useToast } from "@/components/ui/use-toast";
import { ApiError } from "../types/organisation-types";

const forgotPasswordSchema = z.object({
  email: z.string().min(1, { message: "Email is required" }).email({
    message: "Must be a valid email",
  }),
});
type forgotPasswordSchema = z.infer<typeof forgotPasswordSchema>;

/**
 * Renders a form for resetting a forgotten password.
 *
 * This component uses the `useRouter` and `useSearchParams` hooks from the `next/navigation` package,
 * as well as the `useState`, `useEffect`, and `useToast` hooks from React.
 *
 * The form includes an input field for the user's work email and a submit button.
 * When the form is submitted, the `handleForgotPassword` function is called.
 * This function sends a request to the server to reset the password using the `forgotPassword` API.
 * If the request is successful (status code 200), the user is redirected to the "/forgot-password/verify" page.
 * If there is an error, a toast notification is displayed with the error message.
 *
 * @returns {JSX.Element} The rendered form component.
 */

export default function ForgotPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<forgotPasswordSchema>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const handleForgotPassword: SubmitHandler<forgotPasswordSchema> = async (
    data,
  ) => {
    const { email } = data;
    setLoading(true);
    try {
      const forgotPasswordRes = await forgotPassword(email);
      if (forgotPasswordRes.status === 200) {
        router.push("/forgot-password/verify");
      }
    } catch (error) {
      const apiError = error as ApiError;
      toast({
        description:
          apiError?.data?.message ?? "Something went wrong. Please try again.",
        typeof: "error",
      });
      console.log(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit(handleForgotPassword)}>
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

        <Button className="w-full" disabled={loading}>
          {loading ? <SpinLoader /> : "Reset Password"}
        </Button>
      </form>
    </div>
  );
}
