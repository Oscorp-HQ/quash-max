import { Button } from "@/components/ui/button";
import Image from "next/image";
import Link from "next/link";
import Logo from "../../../public/logo-yellow.svg";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Password Updated - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the UpdatedPassword component.
 *
 * This component displays a form layout for a password update confirmation. It includes a logo, a title, a subtitle, an image, and a button to navigate back to the login page.
 *
 * @returns {JSX.Element} The rendered UpdatedPassword component.
 */

export default function UpdatedPassword(): JSX.Element {
  return (
    <div className="forgot-password-form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Password updated!</h1>
          <p className="sub-title">Please log in again to continue.</p>
        </div>
        <Image
          src="/icons/updated.svg"
          width={230.497}
          height={257.618}
          priority
          alt="Quash Logo"
        />
        <Link href="/">
          <Button className="back-to-login">Back to Login</Button>
        </Link>
      </div>
    </div>
  );
}
