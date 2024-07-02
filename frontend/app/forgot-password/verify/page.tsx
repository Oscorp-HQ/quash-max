import Image from "next/image";
import Link from "next/link";
import Logo from "../../../public/logo-yellow.svg";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Verify Email - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the Verify component.
 *
 * This component displays a form layout with a logo, a header, an image, and some footer text.
 * It is used to inform the user that a password reset email has been sent to their email address.
 * If the email is not received, the user is advised to check their spam folder.
 * The user can also contact the support team or go back to the login page using the provided links.
 *
 * @returns {JSX.Element} The rendered Verify component.
 */

export default function Verify() {
  return (
    <div className="form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" priority />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Password Reset Email sent </h1>
          <p className="sub-title">
            Instructions to reset your password have been sent to your email
            address
          </p>
        </div>
        <Image
          src="/icons/verify.svg"
          width={268}
          height={258}
          alt="Verify Icon"
          loading="lazy"
        />
        <p className="form-footer">
          If you don’t receive it right away, check the spam folder.
        </p>
        <p className="form-footer">
          <a href="mailto:hello@quashbugs.com" className="link">
            Contact us
          </a>{" "}
          if you have an issue resetting your password.
        </p>
        <p className="form-footer">
          <Link href="/" className="link">
            Back to Login
          </Link>{" "}
        </p>
      </div>
    </div>
  );
}
