import React, { useEffect, useState } from "react";
import { PlusCircle, X } from "@/app/lib/icons";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { NetworkTable } from "./network-table";
import CopyButton from "@/components/ui/copy-button";
import GenerateSessionButton from "./generate-session-button ";
import Image from "next/image";
import { Dialog, DialogContent } from "@/components/ui/dialog";
const ImageUploader = React.lazy(
  () => import("@/app/dashboard/components/image-uploader"),
);
import { TooltipIconButton } from "./tool-tip-button";
import SuspenseWrapper from "./suspense-wrapper";
import {
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";
import { MediaData, Report } from "@/app/types/dashboard-types";
import EmptyMediaState from "./empty-media-state";

/**
 * SidePaneTabs function displays different tabs based on the selectedBugType and reportId.
 * It manages state for selectedTab, thumbs up/down clicks, uploader visibility, and full media viewer.
 * Handles tab changes, thumbs up/down clicks, uploader opening/closing, image uploads, and media deletion.
 * Renders different content based on the selected tab, such as media, logs, network.
 * Utilizes various components like Tabs, TabsList, TabsTrigger, TabsContent, NetworkTable, ImageUploader, and Dialog.
 * @param selectedBugType The type of bug selected.
 * @param reportId The ID of the report.
 * @param setMediaArray Function to set the media array.
 * @param mediaArray Array of media items.
 * @param newMediaArray Array of newly added media items.
 * @param setNewMediaArray Function to set the new media array.
 * @param selectedCrashLog The selected crash log.
 * @param selectedMedia The selected media item.
 * @param setSelectedMedia Function to set the selected media item.
 * @param isEdit Flag indicating if in edit mode.
 * @param handleMediaDelete Function to handle media deletion.
 * @param selectedRow The selected row.
 * @param gifCreated Flag indicating if GIF is created.
 * @param setGifCreated Function to set the GIF creation status.
 * @param bugListOriginal The original list of bugs.
 * @param setBugListOriginal Function to set the original bug list.
 * @param gifStatus The status of the GIF.
 * @param setSelectedRowValues Function to set selected row values.
 */

interface SidePaneTabsProps {
  selectedBugType: string;
  reportId: string;
  setMediaArray: React.Dispatch<
    React.SetStateAction<
      (ThreadAttachment | ThreadAttachmentLocal | MediaData)[]
    >
  >;
  mediaArray: (ThreadAttachment | ThreadAttachmentLocal | MediaData)[];
  newMediaArray: ThreadAttachmentLocal[];
  setNewMediaArray: React.Dispatch<
    React.SetStateAction<ThreadAttachmentLocal[]>
  >;
  selectedCrashLog: string | null;
  selectedMedia: string;
  setSelectedMedia: React.Dispatch<React.SetStateAction<string>>;
  isEdit: boolean;
  handleMediaDelete: (media: MediaData, index: number) => void;
  selectedRow: Report;
  gifCreated: boolean | null;
  setGifCreated: React.Dispatch<React.SetStateAction<boolean | null>>;
  bugListOriginal: Report[];
  setBugListOriginal: React.Dispatch<React.SetStateAction<Report[]>>;
  gifStatus: string | null;
  setSelectedRowValues: () => void;
  logs: string;
}

const SidePaneTabs = ({
  selectedBugType,
  reportId,
  setMediaArray,
  mediaArray,
  newMediaArray,
  setNewMediaArray,
  selectedCrashLog,
  selectedMedia,
  setSelectedMedia,
  isEdit,
  handleMediaDelete,
  selectedRow,
  gifCreated,
  setGifCreated,
  bugListOriginal,
  setBugListOriginal,
  gifStatus,
  setSelectedRowValues,
  logs,
}: SidePaneTabsProps) => {
  const [selectedTab, setSelectedTab] = useState("media");
  const [isThumbsUpClicked, setThumbsUpClicked] = useState(false);
  const [isThumbsDownClicked, setThumbsDownClicked] = useState(false);
  const [isUploaderOpen, setIsUploaderOpen] = useState(false);
  const [fullMediaViewer, setFullMediaViewer] = useState(false);

  useEffect(() => {
    if (selectedBugType !== "CRASH") {
      setSelectedTab("media");
    }
  }, [reportId, selectedBugType]);

  const handleTabChange = (newValue: string) => {
    setSelectedTab(newValue);
  };

  const handleThumbsUpClick = () => {
    setThumbsUpClicked(!isThumbsUpClicked);
    setThumbsDownClicked(false);
  };

  const handleThumbsDownClick = () => {
    setThumbsUpClicked(false);
    setThumbsDownClicked(!isThumbsDownClicked);
  };

  const openUploader = () => {
    setIsUploaderOpen(true);
  };

  const closeUploader = () => {
    setIsUploaderOpen(false);
  };

  const handleImageUpload = (imageFile: ThreadAttachmentLocal) => {
    setMediaArray([...mediaArray, imageFile]);
    setNewMediaArray([...newMediaArray, imageFile]);
    closeUploader();
  };

  return (
    <>
      <Tabs
        value={selectedTab}
        onValueChange={handleTabChange}
        defaultValue="media"
        className="side-pane-report-tabs"
      >
        <TabsList className="side-pane-report-tabs-list">
          <TabsTrigger value="media" className="side-pane-report-tabs-trigger">
            Media
          </TabsTrigger>
          {selectedBugType === "CRASH" && selectedCrashLog && (
            <TabsTrigger value="logs" className="side-pane-report-tabs-trigger">
              Logs
            </TabsTrigger>
          )}
          <TabsTrigger
            value="network"
            className="side-pane-report-tabs-trigger"
          >
            Network
          </TabsTrigger>
        </TabsList>
        <TabsContent value="media">
          {mediaArray.length === 0 ? (
            <EmptyMediaState isEdit={isEdit} onClick={openUploader} />
          ) : (
            <div className="side-pane-report-media-container">
              {mediaArray?.length > 0 &&
                mediaArray.map(
                  (img: any, index: number) =>
                    img.mediaUrl !== null && (
                      <div className="relative" key={index}>
                        <div className="side-pane-report-media">
                          {img.mediaUrl !== undefined ? (
                            img.mediaType === "IMAGE" ||
                            img.mediaType === "GIF" ? (
                              <Image
                                src={img.mediaUrl}
                                alt="Picture Preview"
                                fill={true}
                                className="hover:cursor-pointer"
                                onClick={() => {
                                  setSelectedMedia(img.mediaUrl);
                                  setFullMediaViewer(true);
                                }}
                              />
                            ) : (
                              img.mediaType === "VIDEO" && (
                                <video
                                  src={img.mediaUrl}
                                  controls
                                  width="100%"
                                  height="100%"
                                />
                              )
                            )
                          ) : img?.type?.startsWith("image") ? (
                            <Image
                              src={URL.createObjectURL(img)}
                              alt="Picture Preview"
                              fill={true}
                              className="hover:cursor-pointer"
                              onClick={() => {
                                setSelectedMedia(URL.createObjectURL(img));
                                setFullMediaViewer(true);
                              }}
                            />
                          ) : (
                            <video
                              src={URL.createObjectURL(img)}
                              controls
                              width="100%"
                              height="100%"
                            />
                          )}
                        </div>
                        {isEdit && (
                          <div
                            className="side-pane-report-media-delete"
                            onClick={() => {
                              handleMediaDelete(img, index);
                            }}
                          >
                            <X
                              className="side-pane-report-media-delete-icon"
                              size={16}
                            />
                          </div>
                        )}
                      </div>
                    ),
                )}
              {selectedRow?.listOfGif !== undefined &&
                selectedRow?.listOfGif !== null &&
                !isEdit &&
                gifCreated !== null &&
                !gifCreated && (
                  <GenerateSessionButton
                    setMediaArray={setMediaArray}
                    reportId={selectedRow.id}
                    mediaArray={mediaArray}
                    setGifCreated={setGifCreated}
                    bugListOriginal={bugListOriginal}
                    setBugListOriginal={setBugListOriginal}
                    gifCreated={gifCreated}
                    gifStatus={gifStatus}
                    setSelectedRowValues={setSelectedRowValues}
                  />
                )}

              {isEdit && (
                <div
                  className={`side-pane-report-media-add`}
                  onClick={openUploader}
                >
                  <div className="add-media-text-container">
                    <PlusCircle
                      className="side-pane-report-media-add-icon"
                      size={32}
                    />
                    <p>Add Media</p>
                  </div>
                </div>
              )}
            </div>
          )}
        </TabsContent>
        {selectedBugType === "CRASH" && selectedCrashLog && (
          <TabsContent value="logs">
            <div className="side-pane-report-crash-logs-content">
              <div className="side-pane-report-crash-logs-container">
                <pre
                  style={{ whiteSpace: "pre-line" }}
                  className="side-pane-report-crash-logs"
                >
                  {logs}
                </pre>
              </div>

              <TooltipIconButton
                tooltipText="Copy to clipboard"
                triggerClassName=""
                contentClassName=""
                triggerComponent={
                  <CopyButton
                    className="side-pane-report-logs-copy"
                    data={logs}
                    toastMessage="Logs copied to clipboard"
                  />
                }
              />
            </div>
          </TabsContent>
        )}
        <TabsContent value="network">
          <NetworkTable reportId={reportId} />
        </TabsContent>
      </Tabs>

      <SuspenseWrapper>
        <ImageUploader
          onClose={closeUploader}
          onMediaUpload={handleImageUpload}
          setIsUploaderOpen={setIsUploaderOpen}
          isUploaderOpen={isUploaderOpen}
        />
      </SuspenseWrapper>

      <Dialog open={fullMediaViewer}>
        <DialogContent className="full-media-view-content">
          <X
            className="full-media-view-close"
            onClick={() => {
              setFullMediaViewer(false);
            }}
            size={24}
          />
          <Image
            src={selectedMedia}
            objectFit="contain"
            layout="fill"
            alt="Picture Preview"
          />
        </DialogContent>
      </Dialog>
    </>
  );
};

export default SidePaneTabs;
