import Image from "next/image";
import Link from "next/link";
import { getServerSession } from "next-auth/next";
import Logo from "../public/logo-yellow.svg";
import Login from "./login";
import { redirect } from "next/navigation";
import type { Metadata } from "next";
import { authOptions } from "./utils/authOptions";

export const metadata: Metadata = {
  title: "Login - Quash | Bug & Crash Reporting for Mobile Developers",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * The main component of the home page.
 * It checks the user's session and redirects the user based on their session status.
 * If the user is logged in and should navigate to the dashboard, they are redirected to the dashboard page.
 * If the user is logged in but has not completed the onboarding process, they are redirected to the onboarding page.
 * If the user is logged in but is not verified, they are redirected to the verification page.
 * If the user is not logged in, the login form is displayed along with a link to the signup page.
 */
export default async function Home() {
  const session = await getServerSession(authOptions);

  if (session) {
    if (session?.user?.shouldNavigateToDashboard) {
      redirect("/dashboard");
    } else {
      if (session?.user?.isVerified) {
        redirect("/onboarding");
      } else {
        redirect("/verify");
      }
    }
  }

  return (
    <div className="form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Welcome back to Quash</h1>
          <p className="sub-title">
            Quick and efficient bug reports for software testing
          </p>
        </div>
        <Login />
        <p className="form-footer">
          New user?{" "}
          <Link
            href={{
              pathname: "/signup",
              query: { source: "login" },
            }}
            className="link"
          >
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}
