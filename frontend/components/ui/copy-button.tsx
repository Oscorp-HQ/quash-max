import React from "react";
import { Button } from "./button";
import { useCopyToClipboard } from "@/hooks/use-copy-to-clip-board";
import { useToast } from "./use-toast";
import { ClipboardText } from "@/app/lib/icons";

const CopyButton = ({
  data,
  className,
  toastMessage,
}: {
  data: string;
  className: string;
  toastMessage?: string;
}) => {
  const [value, copy] = useCopyToClipboard();
  const { toast } = useToast();

  return (
    <Button
      variant="outline"
      size="icon"
      className={`${className} copy-button rounded-[8px] border-[1px] border-solid shadow-[0px 2px 4px 0px rgba(0, 0, 0, 0.12)]`}
      onClick={async () => {
        if (await copy(JSON.stringify(data))) {
          toast({
            description: toastMessage,
          });
        }
      }}
    >
      <ClipboardText className="h-4 w-4" />
    </Button>
  );
};

export default CopyButton;
