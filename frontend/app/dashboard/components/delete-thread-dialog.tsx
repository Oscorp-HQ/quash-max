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
import { X } from "@/app/lib/icons";

/**
 * DeleteThreadDialog component displays an alert dialog to confirm deletion of a thread.
 *
 * @param deleteThreadAlert - Boolean indicating whether the delete thread alert is open
 * @param setDeleteThreadAlert - Function to set the state of deleteThreadAlert
 * @param threadSelectedToDelete - The ID of the thread selected for deletion
 * @param setThreadSelectedToDelete - Function to set the state of threadSelectedToDelete
 * @param loading - Boolean indicating if the component is in a loading state
 * @param threadDeleteHandler - Function to handle the deletion of the selected thread
 * @returns React component representing the delete thread dialog
 */
const DeleteThreadDialog = ({
  deleteThreadAlert,
  setDeleteThreadAlert,
  threadSelectedToDelete,
  setThreadSelectedToDelete,
  loading,
  threadDeleteHandler,
}: {
  deleteThreadAlert: boolean;
  setDeleteThreadAlert: React.Dispatch<React.SetStateAction<boolean>>;
  threadSelectedToDelete: string;
  setThreadSelectedToDelete: React.Dispatch<React.SetStateAction<string>>;
  loading: boolean;
  threadDeleteHandler: (id: string) => void;
}) => {
  return (
    <AlertDialog open={deleteThreadAlert}>
      <AlertDialogContent className="alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Comment</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to delete your comment? You cannot undo this
            action.
            <X
              className="alert-icon"
              onClick={() => {
                setDeleteThreadAlert(false);
                setThreadSelectedToDelete("");
              }}
              size={24}
            />
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            className="alert-cancel"
            onClick={() => {
              setDeleteThreadAlert(false);
              setThreadSelectedToDelete("");
            }}
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            className="alert-action"
            disabled={loading}
            onClick={() => {
              threadDeleteHandler(threadSelectedToDelete);
            }}
          >
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default DeleteThreadDialog;
