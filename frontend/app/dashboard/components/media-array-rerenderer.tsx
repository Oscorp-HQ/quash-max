import React, { useMemo } from "react";
import Image from "next/image";
import { FileAudio, FilePdf, FileText, X } from "@/app/lib/icons";
import { ThreadAttachmentLocal } from "@/app/types/comment-types";

const MediaPreview = React.memo(
  ({
    img,
    index,
    onDelete,
  }: {
    img: ThreadAttachmentLocal;
    index: number;
    onDelete: (img: ThreadAttachmentLocal, index: number) => void;
  }) => {
    const mediaUrl = useMemo(() => URL.createObjectURL(img), [img]);

    const handleClick = () => {
      window.open(mediaUrl, "_blank");
    };

    const handleDelete = (
      event: React.MouseEvent<HTMLDivElement, MouseEvent>,
    ) => {
      event.stopPropagation(); // Prevent the event from bubbling up to the click handler of the container
      onDelete(img, index);
    };

    return (
      <div className="relative" key={index}>
        <div
          className="h-10 w-10 rounded-[4px] overflow-hidden relative flex justify-center items-center cursor-pointer"
          onClick={handleClick}
        >
          {img?.type.startsWith("image") ? (
            <Image
              src={mediaUrl}
              alt="Picture Preview"
              fill={true}
              className="hover:cursor-pointer"
            />
          ) : img?.type.startsWith("audio") ? (
            <FileAudio size={40} />
          ) : img?.type.includes("pdf") ? (
            <FilePdf size={40} />
          ) : img?.type.startsWith("video") ? (
            <video src={mediaUrl} width="100%" height="100%" />
          ) : (
            img?.type.startsWith("text") && <FileText size={40} />
          )}
        </div>
        <div
          className="h-3 w-3 rounded-full bg-[#606060] absolute -top-[2px] -right-[2px] cursor-pointer flex justify-center items-center"
          onClick={handleDelete}
        >
          <X className="text-black" size={8} />
        </div>
      </div>
    );
  },
);

/**
 * React component that renders a preview of different types of media (image, audio, pdf, video, text) with options to open the media in a new tab and delete it.
 *
 * @param img The media object to display
 * @param index The index of the media object in the array
 * @param onDelete Function to handle deletion of the media object
 * @returns JSX element representing the media preview with options to interact with it
 */
export const MediaArrayRenderer = ({
  mediaArray,
  handleMediaDelete,
}: {
  mediaArray: ThreadAttachmentLocal[];
  handleMediaDelete: (img: ThreadAttachmentLocal, index: number) => void;
}) => {
  return (
    <div className="flex gap-2 items-center">
      {mediaArray?.length > 0 &&
        mediaArray.map((img: ThreadAttachmentLocal, index: number) => (
          <MediaPreview
            key={index}
            img={img}
            index={index}
            onDelete={handleMediaDelete}
          />
        ))}
    </div>
  );
};

export default MediaArrayRenderer;
