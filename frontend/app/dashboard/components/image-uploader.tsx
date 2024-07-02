// components/ImageUploader.js
import { UploadSimple, X } from "@/app/lib/icons";
import { ThreadAttachmentLocal } from "@/app/types/comment-types";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import Image from "next/image";
import React, { useState } from "react";
import { useDropzone } from "react-dropzone";

/**
 * Function component for an image uploader dialog.
 *
 * This component allows users to upload images or videos, preview the selected media, and upload it.
 *
 * @param {Function} onClose - Function to close the image uploader dialog.
 * @param {Function} onMediaUpload - Function to handle the upload of selected media.
 * @param {Function} setIsUploaderOpen - Function to set the state of the image uploader dialog.
 * @param {boolean} isUploaderOpen - Boolean indicating whether the image uploader dialog is open.
 *
 * @returns {JSX.Element} ImageUploader component with upload functionality and media preview.
 */

const ImageUploader = ({
  onClose,
  onMediaUpload,
  setIsUploaderOpen,
  isUploaderOpen,
}: {
  onClose: () => void;
  onMediaUpload: (imageFile: ThreadAttachmentLocal) => void;
  setIsUploaderOpen: React.Dispatch<React.SetStateAction<boolean>>;
  isUploaderOpen: boolean;
}): JSX.Element => {
  const [selectedMedia, setSelectedMedia] =
    useState<ThreadAttachmentLocal | null>(null);
  const { getRootProps, getInputProps } = useDropzone({
    accept: {
      "image/jpeg": [],
      "image/png": [],
      "image/gif": [],
      "video/mp4": [],
      "video/avi": [],
    },
    onDrop: (acceptedFiles) => {
      const mediaFile = acceptedFiles[0];
      setSelectedMedia(mediaFile);
    },
  });

  const isImage = selectedMedia?.type.startsWith("image");
  const isVideo = selectedMedia?.type.startsWith("video");

  return (
    <Dialog open={isUploaderOpen}>
      <DialogContent className="media-upload-dialog-content">
        <DialogTitle className="media-upload-dialog-title">
          UPLOAD IMAGE
        </DialogTitle>

        <X
          className="media-upload-dialog-close"
          onClick={() => {
            setIsUploaderOpen(false);
          }}
          size={24}
        />
        <div className="media-uploader-container">
          {selectedMedia ? (
            <div className="uploaded-media-view">
              {isImage ? (
                <Image
                  src={URL.createObjectURL(selectedMedia)}
                  alt="Selected"
                  width={237}
                  height={513}
                />
              ) : isVideo ? (
                <video controls width={237} height={513}>
                  <source
                    src={URL.createObjectURL(selectedMedia)}
                    type={selectedMedia.type}
                  />
                  Your browser does not support the video tag.
                </video>
              ) : null}
            </div>
          ) : (
            <div {...getRootProps()} className="media-drop-zone">
              <input {...getInputProps()} />
              <p className="media-drop-zone-text ">
                Drag and drop a file from your desktop
              </p>
              <p className="media-drop-zone-or">or</p>
              <button className="media-drop-zone-upload">
                Upload Media
                <UploadSimple size={16} />
              </button>
            </div>
          )}

          <div className="media-guidelines">
            <span className="media-guidelines-title">Guidelines</span>
            <ul>
              <li className="media-guidelines-text">Size guidelines : 15MB</li>
              {/* <li className="media-guidelines-text">Ratio guidelines</li> */}
            </ul>
          </div>
          <div className="media-upload-footer">
            <button className="media-upload-cancel" onClick={onClose}>
              Cancel
            </button>
            <button
              disabled={!selectedMedia}
              className={`media-upload-save `}
              onClick={() => {
                if (selectedMedia) onMediaUpload(selectedMedia);
              }}
            >
              Upload
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ImageUploader;
