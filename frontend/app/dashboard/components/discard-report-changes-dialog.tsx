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
 * Function component for displaying an alert dialog to discard report changes.
 *
 * @param discardChangesAltert - Boolean value to control the visibility of the alert dialog.
 * @param setDiscardChangesAltert - Function to set the state of discardChangesAltert.
 * @param setIsEdit - Function to set the state of whether editing is in progress.
 * @param discardChanges - Function to discard the changes made.
 * @param reportId - ID of the report being edited.
 * @param updateReport - Function to update the report with changes.
 * @param setSelectedRowValues - Function to set the selected row values.
 *
 * @returns React element representing the alert dialog for discarding report changes.
 */

const DiscardReportChangesDialog = ({
  discardChangesAltert,
  setDiscardChangesAltert,
  setIsEdit,
  discardChanges,
  reportId,
  updateReport,
  setSelectedRowValues,
}: {
  discardChangesAltert: boolean;
  setDiscardChangesAltert: React.Dispatch<React.SetStateAction<boolean>>;
  setIsEdit: React.Dispatch<React.SetStateAction<boolean>>;
  discardChanges: () => void;
  reportId: string;
  updateReport: (reportId: string) => void;
  setSelectedRowValues: () => void;
}) => {
  return (
    <AlertDialog open={discardChangesAltert}>
      <AlertDialogContent className="alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle>Unsaved changes</AlertDialogTitle>
          <AlertDialogDescription>
            If you leave page, any changes you have made will be lost.
            <X
              className="alert-icon"
              size={24}
              onClick={() => {
                setDiscardChangesAltert(false);
                setIsEdit(false);
                discardChanges();
              }}
            />
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            className="alert-cancel"
            onClick={() => {
              setDiscardChangesAltert(false);
              setIsEdit(false);
              discardChanges();
            }}
          >
            Discard changes
          </AlertDialogCancel>
          <AlertDialogAction
            className="alert-action"
            onClick={() => {
              updateReport(reportId);
              setDiscardChangesAltert(false);
              setSelectedRowValues();
            }}
          >
            Save changes
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default DiscardReportChangesDialog;
