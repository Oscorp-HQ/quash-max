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
import { App } from "@/app/types/application-types";
import { X } from "../../../lib/icons";

const DeleteAppDialog = ({
  deleteAlert,
  selectedApp,
  setDeleteAlert,
  deleteApplication,
  loading,
}: {
  deleteAlert: boolean;
  selectedApp: App;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  deleteApplication: (id: string) => Promise<void>;
  loading: boolean;
}) => {
  return (
    <AlertDialog open={deleteAlert}>
      <AlertDialogContent className="alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Application</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to delete application {selectedApp.appName}
            .
            <X
              size={24}
              className="alert-icon"
              onClick={() => {
                setDeleteAlert(false);
              }}
            />
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            className="alert-cancel"
            onClick={() => {
              setDeleteAlert(false);
            }}
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            className="alert-action"
            onClick={() => {
              deleteApplication(selectedApp.appId);
            }}
            disabled={loading}
          >
            {loading ? <SpinLoader /> : "Delete"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default DeleteAppDialog;
