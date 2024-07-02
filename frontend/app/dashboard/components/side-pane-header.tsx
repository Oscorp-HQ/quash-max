import React from "react";
import { TooltipIconButton } from "./tool-tip-button";
import Image from "next/image";
import closeSidePane from "../../../public/close-side-pane.svg";
import reportExportedIcon from "../../../public/report-exported.svg";
import { PencilSimple, Trash } from "@/app/lib/icons";
import { Button } from "@/components/ui/button";
import ExportButton from "./export-button";
import DeleteReportDialog from "./delete-report-dialog";
import DiscardReportChangesDialog from "./discard-report-changes-dialog";

/**
 * Functional component for rendering the side pane header.
 * Handles various functionalities like exporting, editing, deleting, and saving reports.
 * Utilizes components like TooltipIconButton, Image, Button, ExportButton, Trash, PencilSimple.
 *
 * @param reportId The ID of the report
 * @param bugTitle The title of the report
 * @param isEdit Flag indicating if the report is in edit mode
 * @param savingData Flag indicating if data is being saved
 * @param setDeleteAlert Function to set delete alert state
 * @param setDiscardChangesAltert Function to set discard changes alert state
 * @param updateReport Function to update the report
 * @param handleCloseClick Function to handle close click event
 * @param handleEditClick Function to handle edit click event
 * @param reportExported Flag indicating if the report has been exported
 * @param integrationsDone List of integrations that are done
 * @param exportHandler Function to handle export action
 * @param setBugTitle Function to set the bug title
 */

interface SidePaneHeaderProps {
  reportId: string;
  bugTitle: string;
  setBugTitle: React.Dispatch<React.SetStateAction<string>>;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setIsEdit: React.Dispatch<React.SetStateAction<boolean>>;
  setSelectedRow: any;
  setSelectedRowValues: () => void;
  isEdit: boolean;
  reportExported: boolean;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  savingData: boolean;
  integrationsDone: string[];
  exportHandler: (list: string[], type: string) => void;
  setDiscardChangesAltert: React.Dispatch<React.SetStateAction<boolean>>;
  deleteAlert: boolean;
  selectedRow: any;
  taskDeleteHandler: (id: string) => void;
  updateReport: (id: string) => void;
  discardChangesAltert: boolean;
  discardChanges: () => void;
}

const SidePaneHeader = ({
  reportId,
  bugTitle,
  isEdit,
  savingData,
  deleteAlert,
  setDeleteAlert,
  setDiscardChangesAltert,
  updateReport,
  reportExported,
  integrationsDone,
  exportHandler,
  setBugTitle,
  selectedRow,
  taskDeleteHandler,
  setIsEdit,
  discardChangesAltert,
  discardChanges,
  setSelectedRowValues,
  setOpen,
  setSelectedRow,
}: SidePaneHeaderProps) => {
  const handleCloseClick = () => {
    setOpen(false);
    setIsEdit(false);
  };

  const handleEditClick = () => {
    setIsEdit(true);
  };

  const handleExport = (integration: string) => {
    exportHandler([reportId], integration);
  };
  return (
    <div className="side-pane-header">
      <div className="side-pane-header-left">
        <TooltipIconButton
          tooltipText="Collapse window"
          triggerClassName="side-pane-close-trigger"
          contentClassName=""
          triggerComponent={
            <div className="side-pane-close" onClick={handleCloseClick}>
              <Image
                src={closeSidePane}
                alt="success icon"
                width={11}
                height={11}
                priority
              />
            </div>
          }
        />

        <div className="side-pane-report-meta">
          <span className="side-pane-report-id">{reportId}</span>
          <input
            className="side-pane-report-title"
            value={bugTitle}
            onChange={(e) => {
              setBugTitle(e.target.value);
            }}
            disabled={!isEdit}
          />
        </div>
      </div>
      <div className="side-pane-header-right flex gap-3 items-start h-8">
        {!isEdit ? (
          <>
            {reportExported && (
              <div className="side-pane-exported-container flex gap-1 h-full items-center">
                <Image
                  src={reportExportedIcon}
                  alt="success icon"
                  width={16}
                  height={16}
                  priority
                />
                <span className="side-pane-exported">Exported</span>
              </div>
            )}
            <div
              className="side-pane-delete"
              onClick={() => {
                setDeleteAlert(true);
              }}
            >
              <Trash />
            </div>

            <TooltipIconButton
              tooltipText="Edit Report"
              triggerClassName=""
              contentClassName=""
              triggerComponent={
                <Button
                  variant="outline"
                  disabled={savingData}
                  className="side-pane-edit-container"
                  onClick={handleEditClick}
                >
                  <span className="side-pane-edit-text">Edit</span>
                  <PencilSimple />
                </Button>
              }
            />
            <ExportButton
              integrationsDone={integrationsDone}
              handleExport={handleExport}
            />
          </>
        ) : (
          <>
            <Button
              disabled={savingData}
              variant="outline"
              className="side-pane-edit-cancel"
              onClick={() => {
                setDiscardChangesAltert(true);
              }}
            >
              <span>Cancel</span>
            </Button>

            <Button
              disabled={savingData}
              className="side-pane-edit-save"
              onClick={() => {
                updateReport(reportId);
              }}
            >
              Save
            </Button>
          </>
        )}
      </div>

      <DeleteReportDialog
        reportId={reportId}
        deleteAlert={deleteAlert}
        setDeleteAlert={setDeleteAlert}
        selectedRow={selectedRow}
        taskDeleteHandler={taskDeleteHandler}
      />
      <DiscardReportChangesDialog
        discardChangesAltert={discardChangesAltert}
        setDiscardChangesAltert={setDiscardChangesAltert}
        setIsEdit={setIsEdit}
        discardChanges={discardChanges}
        reportId={reportId}
        updateReport={updateReport}
        setSelectedRowValues={setSelectedRowValues}
      />
    </div>
  );
};

export default SidePaneHeader;
