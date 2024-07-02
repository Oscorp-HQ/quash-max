import Image from "next/image";
import React from "react";
import empty_media from "../../../public/empty-media.svg";
import { Button } from "@/components/ui/button";
import { Plus } from "@phosphor-icons/react";

const EmptyMediaState = ({
  isEdit,
  onClick,
}: {
  isEdit: boolean;
  onClick: () => void;
}) => {
  return (
    <div className="empty-media-state-container">
      <div className="empty-media-state">
        <Image
          height={90}
          width={80}
          src={empty_media}
          alt="Empty Media State"
        />
        <div className="empty-media-state-text">
          <p>No media files are attached to this ticket.</p>
          {!isEdit && <p>To add media, click on the Edit button.</p>}
        </div>
        {isEdit && (
          <Button
            onClick={onClick}
            variant="outline"
            className="empty-media-state-add-media"
          >
            <Plus size={16} />
            Add Media
          </Button>
        )}
      </div>
    </div>
  );
};

export default EmptyMediaState;
