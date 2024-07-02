import React from "react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import SpinLoader from "@/components/ui/spinner";
import { X } from "@/app/lib/icons";

/**
 * Functional component for a logout alert dialog.
 * Renders an AlertDialog with custom content for logging out.
 * Handles log out confirmation and cancellation actions.
 *
 * @param logOutAlert - Boolean indicating if the logout alert is open.
 * @param setLogOutAlert - Function to set the state of the logout alert.
 * @param savingData - Boolean indicating if data is being saved.
 * @param handleSignOut - Function to handle the sign out action.
 * @param setSavingData - Function to set the state of data saving.
 * @returns React element representing the logout alert dialog.
 */

const LogoutAlert = ({
  logOutAlert,
  setLogOutAlert,
  savingData,
  handleSignOut,
  setSavingData,
}: {
  logOutAlert: boolean;
  setLogOutAlert: React.Dispatch<React.SetStateAction<boolean>>;
  savingData: boolean;
  handleSignOut: () => void;
  setSavingData: React.Dispatch<React.SetStateAction<boolean>>;
}) => {
  return (
    <AlertDialog open={logOutAlert}>
      <AlertDialogContent className="alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle>Log Out</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to log out?
            <X
              size={24}
              className="alert-icon"
              onClick={() => {
                setLogOutAlert(false);
              }}
            />
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            disabled={savingData}
            className="alert-cancel"
            onClick={() => {
              setLogOutAlert(false);
            }}
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            disabled={savingData}
            className="alert-action"
            onClick={() => {
              handleSignOut();
              setSavingData(true);
            }}
          >
            {savingData ? <SpinLoader /> : "Log out"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default LogoutAlert;
