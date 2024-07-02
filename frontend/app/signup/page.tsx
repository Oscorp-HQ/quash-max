import Image from "next/image";
import Link from "next/link";
import { redirect } from "next/navigation";
import { getServerSession } from "next-auth/next";
import SignUpForm from "./signup-form";
import Logo from "../../public/logo-yellow.svg";
import type { Metadata } from "next";
import { authOptions } from "../utils/authOptions";

export const metadata: Metadata = {
  title: "Sign Up - Quash | Bug & Crash Reporting for Mobile Developers",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the sign-up page.
 *
 * If the user is already authenticated, it redirects them to the appropriate page based on their session data.
 * If the user is not authenticated, it displays the sign-up form.
 *
 * @returns {JSX.Element} The sign-up page component.
 */

export default async function SignUp() {
  const session = await getServerSession(authOptions);

  if (session) {
    if (!session?.user?.shouldNavigateToDashboard) {
      if (!session?.user?.isVerified) {
        // navigate to verify page
        redirect("/verify");
      } else {
        redirect("/onboarding");
      }
    } else {
      redirect("/dashboard");
    }
  }

  return (
    <div className="form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Welcome to Quash</h1>
          <p className="sub-title">
            Quick and efficient bug reports for software testing
          </p>
        </div>
        <SignUpForm />
        <p className="form-footer">
          Already a user?{" "}
          <Link
            href={{
              pathname: "/",
              query: { source: "signup" },
            }}
            className="link"
          >
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
