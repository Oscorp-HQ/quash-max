import Image from "next/image";
import Link from "next/link";

import Logo from "../../public/logo-yellow.svg";
import ForgotPasswordForm from "./forgot-password-form";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Enter Email - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the Forgot Password page.
 *
 * This component displays a form for users to reset their password. It includes a logo, form header, and a link to the login page.
 *
 * @returns {JSX.Element} The rendered Forgot Password page.
 */

export default function ForgotPassword() {
  return (
    <div className="forgot-password-form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Forgot Password ?</h1>
          <p className="sub-title">
            No worries, we’ll send you reset instructions.
          </p>
        </div>
        <ForgotPasswordForm />
        <p className="form-footer">
          Back to{" "}
          <Link href="/" className="link">
            Login
          </Link>
        </p>
      </div>
    </div>
  );
}
