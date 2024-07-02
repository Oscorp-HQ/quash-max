import Image from "next/image";
import { getServerSession } from "next-auth/next";
import Logo from "../../public/logo-yellow.svg";
import { redirect } from "next/navigation";
import VerifyPage from "./verify-page";
import SignUp from "./signup-button";
import { Metadata } from "next";
import { authOptions } from "../utils/authOptions";

export const metadata: Metadata = {
  title: "Verify Email - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the Verify component.
 *
 * This component is responsible for rendering the verification page. It checks if the user is already verified and redirects them to the appropriate page. If the user is not verified, it displays a form for the user to check their inbox and confirm their email address.
 *
 * @returns {JSX.Element} The rendered Verify component.
 */

export default async function Verify() {
  const session = await getServerSession(authOptions);

  if (session) {
    if (session?.user?.isVerified) {
      if (!session?.user?.shouldNavigateToDashboard) {
        redirect("/onboarding");
      }
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
          <h1 className="title">Check your inbox</h1>
          <p className="sub-title">
            We’ve emailed a special link to {session?.user?.email}. Click the
            link to confirm your address and get started.
          </p>
        </div>
        <Image
          src="/icons/verify.svg"
          width={268}
          height={258}
          alt="Verify Icon"
        />
        <p className="form-footer">
          Wrong email? <SignUp /> with another account
        </p>
        <VerifyPage />
      </div>
    </div>
  );
}
