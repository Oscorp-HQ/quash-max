"use client";
import { signOut } from "next-auth/react";
import { useRouter } from "next/navigation";

/**
 * Function component for the SignUp button.
 *
 * This component renders a button labeled "Sign up" and handles the sign out functionality.
 * When the button is clicked, it triggers the handleSignOut function, which signs the user out
 * and redirects them to the home page ("/").
 *
 * @returns The rendered SignUp button.
 */

export default function SignUp() {
  const router = useRouter();

  const handleSignOut = async () => {
    await signOut();
    router.push("/");
  };

  return (
    <button className="sign-up-link" onClick={handleSignOut}>
      Sign up
    </button>
  );
}
