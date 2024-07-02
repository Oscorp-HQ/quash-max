import Image from "next/image";
import Logo from "../../../public/logo-yellow.svg";
import NewPasswordForm from "./new-password-form";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Set Password - Quash",
  description: "Bug & Crash Reporting for Mobile Developers",
};

/**
 * Renders the 'NewPassword' component.
 *
 * This component displays a form for setting a new password. It includes a logo, a title, and a sub-title.
 * The form itself is rendered using the 'NewPasswordForm' component.
 *
 * @returns {JSX.Element} The rendered 'NewPassword' component.
 */

export default function NewPassword() {
  return (
    <div className="forgot-password-form-layout">
      <div className="logo">
        <Image src={Logo} width={122} height={32} alt="Quash Logo" />
      </div>
      <div className="form-container">
        <div className="form-header">
          <h1 className="title">Set a new password</h1>
          <p className="sub-title">Make sure you keep a strong one.</p>
        </div>
        <NewPasswordForm />
      </div>
    </div>
  );
}
