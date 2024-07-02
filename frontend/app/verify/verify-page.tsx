"use client";
import { useState, useEffect } from "react";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { GetVerifyUser, resendEmail } from "../apis/authapis";
import { useToast } from "@/components/ui/use-toast";

/**
 * VerifyPage component.
 *
 * This component is responsible for rendering the email verification page.
 * It uses the useSession, useRouter, and useToast hooks from the next-auth/react and next/navigation packages.
 * The component fetches the user's verification status using the GetVerifyUser API function and updates the session accordingly.
 * It also provides a button to resend the verification email using the resendEmail API function.
 * If the user is already verified, it redirects to the onboarding page.
 *
 * @returns {ReactElement} The VerifyPage component.
 */

export default function VerifyPage() {
  const { data: session, update } = useSession();
  const router = useRouter();
  const { toast } = useToast();

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchData() {
      try {
        if (session) {
          const result = await GetVerifyUser();
          if (result.data) {
            await update({
              ...session,
              user: {
                ...session?.user,
                isVerified: true,
              },
              data: {
                ...session?.data,
                isVerified: true,
              },
            });
          }
        }
      } catch (error) {
        toast({
          description: "Something went wrong. please try again",
          typeof: "error",
        });
      }
    }
    fetchData();
  }, [session?.user?.isVerified]);

  useEffect(() => {
    if (session?.user.isVerified) {
      router.push("/onboarding");
    }
  }, [router, session?.user.isVerified]);

  const handleResendEmail = async (token: string | null | undefined) => {
    try {
      setLoading(true);
      const sendEmailRes = await resendEmail(token);
      if (sendEmailRes.status === 200) {
        toast({
          description: "Successfully email sent.",
        });
      }
      setLoading(false);
    } catch (error) {
      setLoading(false);
      toast({
        description: "Something went wrong. please try again",
        typeof: "error",
      });
    }
  };

  return (
    <div className="verify-footer">
      <p>Haven’t received an email? Check the spam folder.</p>
      <p>
        Still nothing?{" "}
        {loading ? (
          <p>loading...</p>
        ) : (
          <Button
            className="link"
            variant="link"
            onClick={() => handleResendEmail(session?.data?.token)}
          >
            Re-send email
          </Button>
        )}
      </p>
    </div>
  );
}
