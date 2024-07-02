import React, { useState } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Gear, SignOut, User } from "@/app/lib/icons";
import { signOut, useSession } from "next-auth/react";
import { ModeToggle } from "@/components/ui/mode-toggle";
import SuspenseWrapper from "./suspense-wrapper";

const LogoutAlert = React.lazy(() => import("./logout-alert"));

/**
 * Header component for the top bar of the application.
 * This component displays the logo, user profile dropdown menu, settings, and logout options.
 * It also includes functionality to handle user sign out and theme toggling.
 *
 * @returns {JSX.Element} The JSX element representing the Header component.
 */

const Header = () => {
  const { data: session } = useSession();
  const [logOutAlert, setLogOutAlert] = useState(false);
  const [savingData, setSavingData] = useState(false);
  const router = useRouter();
  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };
  return (
    <div className="header">
      <Image
        width={96}
        height={24}
        src={"/logo-yellow.svg"}
        className="header-quash-logo"
        alt={"quash-logo"}
        onClick={() => {
          router.push("/dashboard");
        }}
      />
      <div className="header-dropdown-container">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button className="header-dropdown">
              <div className="header-dropdown-icon-container p-1">
                <User size={16} color="#FFFFFF" />
              </div>
              <span className="header-dropdown-value">
                {session?.user?.fullName
                  ? session?.user?.fullName
                  : session?.user?.name}
              </span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="header-dropdown-content">
            <DropdownMenuGroup>
              <DropdownMenuItem
                className="header-dropdown-item"
                onClick={() => {
                  router.push(`/settings/general?source=dashboard`);
                }}
              >
                <Gear size={16} />

                <span>Settings</span>
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => {
                  setLogOutAlert(true);
                }}
                className="header-dropdown-item"
              >
                <SignOut className="header-signout-icon" />
                <span className="header-signout-text">Log out</span>
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
        <ModeToggle />
      </div>
      {logOutAlert && (
        <SuspenseWrapper>
          <LogoutAlert
            logOutAlert={logOutAlert}
            setLogOutAlert={setLogOutAlert}
            savingData={savingData}
            handleSignOut={handleSignOut}
            setSavingData={setSavingData}
          />
        </SuspenseWrapper>
      )}
    </div>
  );
};

export default Header;
