import React, { useEffect, useState } from "react";
import { patchReport } from "@/app/apis/dashboardapis";
import { useCustomToast } from "./custom-use-toast";
import SidePaneHeader from "./side-pane-header";
import SidePaneInfo from "./side-pane-info";
import Comments from "./comments";
import SidePaneTabs from "./side-pane-tabs";
import {
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";
import { MediaData, Report, ToastObject } from "@/app/types/dashboard-types";
import { Organisation } from "@/app/types/organisation-types";

/**
 * SidePane component renders a side pane for displaying and editing report details.
 *
 * @param {Object} props - The props object containing various properties:
 *   - open: boolean indicating if the side pane is open
 *   - setOpen: function to set the open state of the side pane
 *   - setIsEdit: function to set the edit state of the side pane
 *   - setSelectedRow: function to set the selected row
 *   - isEdit: boolean indicating if the side pane is in edit mode
 *   - reportExported: boolean indicating if the report is exported
 *   - setReportExported: function to set the report exported state
 *   - setDeleteAlert: function to set the delete alert state
 *   - savingData: boolean indicating if data is being saved
 *   - integrationsDone: boolean indicating if integrations are done
 *   - exportHandler: function to handle export of report
 *   - setDiscardChangesAltert: function to set the discard changes alert state
 *   - dateFormatter: function for date formatting
 *   - deleteAlert: boolean indicating if delete alert is active
 *   - selectedRow: the selected row object
 *   - taskDeleteHandler: function to handle task deletion
 *   - discardChangesAltert: boolean indicating if discard changes alert is active
 *   - setToastObject: function to set the toast object
 *   - setSavingData: function to set the saving data state
 *   - bugListOriginal: the original list of bugs
 *   - setBugListOriginal: function to set the original bug list
 *   - toastDismiss: function to dismiss the toast
 *   - setRowActionEdit: function to set the row action edit state
 *   - rowActionEdit: boolean indicating if row action edit is active
 *   - organisationData: data related to the organization
 *
 * @returns {JSX.Element} A JSX element representing the SidePane component
 */

interface SidePaneProps {
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setIsEdit: React.Dispatch<React.SetStateAction<boolean>>;
  setSelectedRow: React.Dispatch<React.SetStateAction<Report | null>>;
  isEdit: boolean;
  reportExported: boolean;
  setReportExported: React.Dispatch<React.SetStateAction<boolean>>;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  savingData: boolean;
  integrationsDone: string[];
  exportHandler: (list: string[], type: string) => void;
  setDiscardChangesAltert: React.Dispatch<React.SetStateAction<boolean>>;
  dateFormatter: Intl.DateTimeFormat;
  deleteAlert: boolean;
  selectedRow: Report;
  taskDeleteHandler: (id: string) => void;
  discardChangesAltert: boolean;
  setToastObject: React.Dispatch<React.SetStateAction<ToastObject>>;
  setSavingData: React.Dispatch<React.SetStateAction<boolean>>;
  bugListOriginal: Report[];
  setBugListOriginal: React.Dispatch<React.SetStateAction<Report[]>>;
  toastDismiss: () => void;
  setRowActionEdit: React.Dispatch<React.SetStateAction<boolean>>;
  rowActionEdit: boolean;
  organisationData: Organisation | null;
}

const SidePane = ({
  open,
  setOpen,
  setIsEdit,
  setSelectedRow,
  isEdit,
  reportExported,
  setReportExported,
  setDeleteAlert,
  savingData,
  integrationsDone,
  exportHandler,
  setDiscardChangesAltert,
  dateFormatter,
  deleteAlert,
  selectedRow,
  taskDeleteHandler,
  discardChangesAltert,
  setToastObject,
  setSavingData,
  bugListOriginal,
  setBugListOriginal,
  toastDismiss,
  setRowActionEdit,
  rowActionEdit,
  organisationData,
}: SidePaneProps): JSX.Element => {
  const [selectedCrashLog, setSelectedCrashLog] = useState<string | null>(null);
  const [logs, setLogs] = useState<string>("");
  const [selectedMedia, setSelectedMedia] = useState("");
  const [audioFile, setAudioFile] = useState<MediaData | null>(null);
  const [mediaArray, setMediaArray] = useState<
    (ThreadAttachment | ThreadAttachmentLocal | MediaData)[]
  >([]);
  const [newMediaArray, setNewMediaArray] = useState<ThreadAttachmentLocal[]>(
    [],
  );
  const [mediaToRemoveIds, setMediaToRemoveIds] = useState<string[]>([]);
  const [bugTitle, setBugTitle] = useState<string>("");
  const [reportId, setReportId] = useState<string>("");
  const [reportAt, setReportAt] = useState<string>("");
  const [reportBy, setReportBy] = useState<string>("");
  const [reportedDevice, setReportedDevice] = useState<string | undefined>("");
  const [selectedStatus, setSelectedStatus] = useState<string>("");
  const [selectedBugType, setSelectedBugType] = useState<string>("");
  const [selectedBugPriority, setSelectedBugPriority] = useState<string>("");
  const [description, setDescription] = useState<string>("");
  const [gifCreated, setGifCreated] = useState<boolean | null>(null);
  const [gifStatus, setGifStatus] = useState<string | null>(null);
  const { toastCustom, dismiss } = useCustomToast();

  useEffect(() => {
    if (rowActionEdit) {
      setSelectedRowValues();
      setRowActionEdit(false);
    } else {
      if (!isEdit) {
        if (selectedRow) {
          setSelectedRowValues();
        }
      } else {
        setDiscardChangesAltert(true);
      }
    }
  }, [selectedRow]);

  useEffect(() => {
    if (open) {
      document.body.classList.add("lock-scroll");
    } else {
      document.body.classList.remove("lock-scroll");
    }

    return () => {
      document.body.classList.remove("lock-scroll");
    };
  }, [open]);

  const rowUpdateHandler = (row: Report) => {
    setReportId(row?.id);
    setReportAt(row?.createdAt);
    setReportBy(row?.reportedBy?.fullName);
    setReportedDevice(row?.deviceMetadata?.device);
    setReportExported(row?.exportedOn !== null ? true : false);
    if (row?.listOfMedia && row?.listOfMedia?.length > 0) {
      let audio = null;
      let temp: MediaData[] = [];
      row?.listOfMedia?.map((media: MediaData, index: number) => {
        if (media.mediaType === "AUDIO") {
          audio = { ...media, index: index };
        }
      });
      temp = row?.listOfMedia?.filter((media: MediaData) => {
        return media.mediaType !== "AUDIO";
      });
      setAudioFile(audio);
      if (temp.length > 0) {
        setSelectedMedia(temp[0]?.mediaUrl ? temp[0]?.mediaUrl : "");
        setMediaArray(temp.slice(0));
      }
    } else {
      setSelectedMedia("");
      setMediaArray([]);
    }
    setSelectedStatus(row?.status);
    setSelectedBugType(row?.type);

    setSelectedBugPriority(row?.priority ? row?.priority : "NOT_DEFINED");
    setDescription(row?.description);
    setBugTitle(row?.title);
    if (
      row?.type === "CRASH" &&
      (row?.gifStatus !== null || row?.gifStatus !== undefined)
    ) {
      setGifStatus(row?.gifStatus);
      if (row?.gifStatus === "COMPLETED" || row?.gifStatus === "DELETED") {
        setGifCreated(true);
      } else if (
        row?.gifStatus === "NOT_INITIATED" ||
        row?.gifStatus === "PROCESSING" ||
        row?.gifStatus === "FAILED"
      ) {
        setGifCreated(false);
      }
    } else {
      setGifCreated(null);
    }
    if (
      row?.type === "CRASH" &&
      row?.crashLog &&
      row?.crashLog?.logUrl !== null
    ) {
      setSelectedCrashLog(row?.crashLog?.logUrl);
    } else {
      setSelectedCrashLog(null);
    }
  };

  const setSelectedRowValues = (row = selectedRow) => {
    rowUpdateHandler(row);
  };

  const handleMediaDelete = (img: MediaData | any, index?: number) => {
    let temp = [];

    temp = mediaArray.filter((media, inx) => inx !== index);
    setMediaArray(temp.slice(0));

    if ("mediaUrl" in img) {
      setMediaToRemoveIds([...mediaToRemoveIds, img.id]);
    } else {
      let temp = [];

      temp = newMediaArray.filter((media: any) => {
        return media.path !== img.path;
      });
      setNewMediaArray(temp.slice(0));
    }
  };

  const discardChanges = (row = selectedRow) => {
    rowUpdateHandler(row);

    setNewMediaArray([]);
    setMediaToRemoveIds([]);
  };

  useEffect(() => {
    async function fetchExternalTextFile() {
      if (!selectedCrashLog) {
        return;
      }
      try {
        const response = await fetch(selectedCrashLog);
        const data = await response.text();
        setLogs(data);
      } catch (error) {}
    }
    fetchExternalTextFile();
  }, [selectedCrashLog]);

  const updateReport = async (
    reportId: string,
    bugStatus = selectedStatus,
    update = "full",
  ) => {
    setSavingData(true);
    toastCustom({
      description: "",
    });
    setToastObject({
      message: "Saving changes",
      type: "load",
    });
    const formData = new FormData();
    if (selectedRow?.title !== bugTitle) {
      formData.append("title", bugTitle);
    }
    if (selectedRow?.description !== description) {
      formData.append("description", description);
    }
    formData.append("status", bugStatus);
    if (selectedRow?.type !== selectedBugType) {
      formData.append("type", selectedBugType);
    }

    if (selectedRow?.priority !== selectedBugPriority) {
      formData.append("priority", selectedBugPriority);
    }

    newMediaArray.forEach((item) => formData.append("newMediaFiles", item));
    mediaToRemoveIds.forEach((item) =>
      formData.append("mediaToRemoveIds", item),
    );

    try {
      const { data, message, success } = await patchReport(reportId, formData);
      if (success) {
        let temp: Report[] = [];
        temp = bugListOriginal.map((bug: Report) => {
          if (reportId === bug.id) {
            return {
              ...data,
              reportedByName: data?.reportedBy?.fullName,
              exported: data?.exportedOn === null ? false : true,
            };
          } else {
            return bug;
          }
        });
        if (selectedRow.id === reportId && update === "full") {
          setSelectedRowValues(data);
        }
        setSavingData(false);
        setToastObject({
          message: "Saved",
          type: "success",
        });
        toastDismiss();
        setBugListOriginal([...temp]);
        setIsEdit(false);
        setNewMediaArray([]);
        setMediaToRemoveIds([]);
      } else {
        setSavingData(false);
        setToastObject({
          message: "Could not save the changes",
          type: "error",
        });
        toastDismiss();
      }
      return;
    } catch (error) {
      setSavingData(false);
      setToastObject({
        message: "Could not save the changes",
        type: "error",
      });
      toastDismiss();
      console.log(error);
    }
  };

  return (
    <div className={`side-pane-container relative ${open ? "open" : "close"}`}>
      <SidePaneHeader
        reportId={reportId}
        bugTitle={bugTitle}
        isEdit={isEdit}
        savingData={savingData}
        deleteAlert={deleteAlert}
        setDeleteAlert={setDeleteAlert}
        setDiscardChangesAltert={setDiscardChangesAltert}
        updateReport={updateReport}
        setOpen={setOpen}
        setSelectedRow={setSelectedRow}
        reportExported={reportExported}
        integrationsDone={integrationsDone}
        exportHandler={exportHandler}
        setBugTitle={setBugTitle}
        selectedRow={selectedRow}
        taskDeleteHandler={taskDeleteHandler}
        setIsEdit={setIsEdit}
        discardChangesAltert={discardChangesAltert}
        discardChanges={discardChanges}
        setSelectedRowValues={setSelectedRowValues}
      />
      <div className="side-pane-content-container">
        <div className="side-pane-content">
          <div className="side-pane-info-section">
            <SidePaneInfo
              reportBy={reportBy}
              reportAt={reportAt}
              dateFormatter={dateFormatter}
              reportedDevice={reportedDevice}
              isEdit={isEdit}
              selectedBugType={selectedBugType}
              setSelectedBugType={setSelectedBugType}
              selectedBugPriority={selectedBugPriority}
              setSelectedBugPriority={setSelectedBugPriority}
              selectedStatus={selectedStatus}
              setSelectedStatus={setSelectedStatus}
              updateReport={updateReport}
              reportId={reportId}
              audioFile={audioFile}
              setAudioFile={setAudioFile}
              handleMediaDelete={handleMediaDelete}
              description={description}
              setDescription={setDescription}
            />
            {organisationData && (
              <div>
                <Comments
                  key={reportId}
                  reportId={reportId}
                  organisationData={organisationData}
                  setToastObject={setToastObject}
                  toastDismiss={toastDismiss}
                />
              </div>
            )}
          </div>
          <SidePaneTabs
            selectedBugType={selectedBugType}
            reportId={reportId}
            setMediaArray={setMediaArray}
            mediaArray={mediaArray}
            newMediaArray={newMediaArray}
            setNewMediaArray={setNewMediaArray}
            selectedCrashLog={selectedCrashLog}
            selectedMedia={selectedMedia}
            setSelectedMedia={setSelectedMedia}
            isEdit={isEdit}
            handleMediaDelete={handleMediaDelete}
            selectedRow={selectedRow}
            gifCreated={gifCreated}
            setGifCreated={setGifCreated}
            bugListOriginal={bugListOriginal}
            setBugListOriginal={setBugListOriginal}
            gifStatus={gifStatus}
            setSelectedRowValues={setSelectedRowValues}
            logs={logs}
          />
        </div>
      </div>
    </div>
  );
};

export default SidePane;
