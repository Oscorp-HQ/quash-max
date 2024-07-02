import React from "react";
import { priorities, statuses, types } from "../data/data";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import ReactAudioPlayer from "react-audio-player";
import { User } from "@/app/lib/icons";
import Image from "next/image";
import audioDelete from "../../../public/audio-delete.svg";
import { MediaData } from "@/app/types/dashboard-types";
import {
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";

/**
 * Renders the side pane information for a bug report.
 *
 * @param {Object} props - The props object containing the following properties:
 * @param {string} reportBy - The name of the user who reported the bug.
 * @param {Date} reportAt - The date and time when the bug was reported.
 * @param {Function} dateFormatter - The function to format the date.
 * @param {string} reportedDevice - The device on which the bug was reported.
 * @param {boolean} isEdit - Flag indicating if the bug report is being edited.
 * @param {string} selectedBugType - The selected bug type.
 * @param {Function} setSelectedBugType - Function to set the selected bug type.
 * @param {string} selectedBugPriority - The selected bug priority.
 * @param {Function} setSelectedBugPriority - Function to set the selected bug priority.
 * @param {string} selectedStatus - The selected bug status.
 * @param {Function} setSelectedStatus - Function to set the selected bug status.
 * @param {Function} updateReport - Function to update the bug report.
 * @param {string} reportId - The ID of the bug report.
 * @param {Object} audioFile - The audio file associated with the bug report.
 * @param {Function} setAudioFile - Function to set the audio file.
 * @param {Function} handleMediaDelete - Function to handle deletion of media files.
 * @param {string} description - The description of the bug report.
 * @param {Function} setDescription - Function to set the bug report description.
 *
 * @returns {JSX.Element} JSX element representing the side pane information for a bug report.
 */

interface SidePaneInfoProps {
  reportBy: string;
  reportAt: string;
  dateFormatter: Intl.DateTimeFormat;
  reportedDevice: string | undefined;
  isEdit: boolean;
  selectedBugType: string;
  setSelectedBugType: React.Dispatch<React.SetStateAction<string>>;
  selectedBugPriority: string;
  setSelectedBugPriority: React.Dispatch<React.SetStateAction<string>>;
  selectedStatus: string;
  setSelectedStatus: React.Dispatch<React.SetStateAction<string>>;
  updateReport: (id: string, bugStatus: string, update: string) => void;
  reportId: string;
  audioFile: MediaData | null;
  setAudioFile: React.Dispatch<React.SetStateAction<MediaData | null>>;
  handleMediaDelete: (img: MediaData | ThreadAttachmentLocal) => void;
  description: string;
  setDescription: React.Dispatch<React.SetStateAction<string>>;
}

const SidePaneInfo = ({
  reportBy,
  reportAt,
  dateFormatter,
  reportedDevice,
  isEdit,
  selectedBugType,
  setSelectedBugType,
  selectedBugPriority,
  setSelectedBugPriority,
  selectedStatus,
  setSelectedStatus,
  updateReport,
  reportId,
  audioFile,
  setAudioFile,
  handleMediaDelete,
  description,
  setDescription,
}: SidePaneInfoProps) => {
  return (
    <div className="side-pane-info-container">
      <div className="side-pane-report-details-container">
        <div className="side-pane-report-info">
          <span className="side-pane-report-details-text">Reported by</span>
          <div className="side-pane-reported-by-container">
            <User size={16} color="#BDBDBD" />
            <span className="side-pane-report-details-value">{reportBy}</span>
          </div>
        </div>
        <div className="side-pane-report-info">
          <span className="side-pane-report-details-text">Reported on</span>
          <span className="side-pane-report-details-value">
            {reportAt && dateFormatter.format(new Date(reportAt))}
          </span>
        </div>
        {reportedDevice && (
          <div className="side-pane-report-info">
            <span className="side-pane-report-details-text">Device</span>
            <span className="side-pane-report-details-value">
              {reportedDevice}
            </span>
          </div>
        )}
        <div className="side-pane-report-info">
          <span className="side-pane-report-details-text">Type</span>
          {!isEdit ? (
            <div className="flex gap-2 items-center">
              {types.find((label) => label.value === selectedBugType)?.icon}
              <p className="side-pane-report-details-value">
                {types.find((label) => label.value === selectedBugType)?.label}
              </p>
            </div>
          ) : (
            <Select
              disabled={!isEdit}
              defaultValue={selectedBugType}
              value={selectedBugType}
              onValueChange={(e) => {
                setSelectedBugType(e);
              }}
            >
              <SelectTrigger className="side-pane-dropdown-trigger">
                <SelectValue
                  placeholder="All Bugs"
                  className="side-pane-dropdown-item"
                />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {types.map((label, index) => (
                    <SelectItem value={label.value} key={index}>
                      <div className="side-pane-dropdown-item">
                        {label.icon}
                        <span className="side-pane-report-details-value">
                          {label.label}
                        </span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          )}
        </div>
        <div className="side-pane-report-info">
          <span className="side-pane-report-details-text">Priority</span>
          {!isEdit ? (
            <div className="flex gap-2 items-center">
              {
                priorities.find(
                  (priority) => priority.value === selectedBugPriority,
                )?.icon
              }

              <p className="side-pane-report-details-value">
                {
                  priorities.find(
                    (priority) => priority.value === selectedBugPriority,
                  )?.label
                }
              </p>
            </div>
          ) : (
            <Select
              disabled={!isEdit}
              defaultValue={selectedBugPriority}
              value={selectedBugPriority}
              onValueChange={(e) => {
                setSelectedBugPriority(e);
              }}
            >
              <SelectTrigger className="side-pane-dropdown-trigger">
                <SelectValue
                  placeholder="Priority"
                  className="side-pane-dropdown-item"
                />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {priorities.map((priority, index) => (
                    <SelectItem value={priority.value} key={index}>
                      <div className="side-pane-dropdown-item">
                        {priority.icon}
                        <span className="side-pane-report-details-value">
                          {priority.label}
                        </span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          )}
        </div>
        <div className="side-pane-report-info">
          <span className="side-pane-report-details-text">Status</span>
          <Select
            defaultValue={selectedStatus}
            value={selectedStatus}
            onValueChange={(e) => {
              setSelectedStatus(e);

              if (!isEdit) {
                updateReport(reportId, e, "status");
              }
            }}
          >
            <SelectTrigger className="side-pane-dropdown-trigger">
              <SelectValue
                placeholder="All Bugs"
                className="side-pane-dropdown-item"
              />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {statuses.map((status, index) => (
                  <SelectItem value={status.value} key={index}>
                    <div className="side-pane-dropdown-item">
                      {status.icon}
                      <span className="side-pane-report-details-value">
                        {status.label}
                      </span>
                    </div>
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
      </div>
      {audioFile && (
        <div className="audio-player-container">
          <ReactAudioPlayer
            src={audioFile?.mediaUrl}
            controls
            controlsList="nodownload noremoteplayback noplaybackrate foobar "
            autoPlay={false}
            className="audio-player"
          />
          {isEdit && (
            <div
              className="audio-player-cancel"
              onClick={() => {
                handleMediaDelete(audioFile);
                setAudioFile(null);
              }}
            >
              <Image
                width={11}
                height={10}
                alt="audio-delete"
                src={audioDelete}
              />
            </div>
          )}
        </div>
      )}

      {
        <div style={{ whiteSpace: "pre-wrap" }}>
          {isEdit ? (
            <textarea
              disabled={!isEdit}
              value={description}
              onChange={(e) => {
                setDescription(e.target.value);
              }}
              className={`side-pane-report-description-text-area`}
              style={{ whiteSpace: "pre-line" }}
            ></textarea>
          ) : (
            description && (
              <div
                className="side-pane-report-description"
                style={{ whiteSpace: "pre-wrap" }}
              >
                {description}
              </div>
            )
          )}
        </div>
      }
    </div>
  );
};

export default SidePaneInfo;
