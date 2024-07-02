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
import { Report } from "@/app/types/dashboard-types";

/**
 * DeleteReportDialog component displays an alert dialog to confirm deletion of a bug report.
 *
 * @param deleteAlert - Boolean indicating whether the delete alert dialog is open.
 * @param setDeleteAlert - Function to toggle the delete alert dialog.
 * @param selectedRow - The selected bug report to be deleted.
 * @param taskDeleteHandler - Function to handle the deletion of the bug report.
 * @param reportId - The ID of the bug report to be deleted.
 * @returns React component that renders the delete report alert dialog.
 */
const DeleteReportDialog = ({
  deleteAlert,
  setDeleteAlert,
  selectedRow,
  taskDeleteHandler,
  reportId,
}: {
  deleteAlert: boolean;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  selectedRow: Report;
  taskDeleteHandler: (id: string) => void;
  reportId: string;
}) => {
  return (
    <AlertDialog open={deleteAlert}>
      <AlertDialogContent className="alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Bug Report</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to delete the &quot;
            {selectedRow?.title}&quot; Bug Report? This will permanently erase
            the report.
            <X
              className="alert-icon"
              onClick={() => {
                setDeleteAlert(false);
              }}
              size={24}
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
              taskDeleteHandler(reportId);
            }}
          >
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default DeleteReportDialog;
