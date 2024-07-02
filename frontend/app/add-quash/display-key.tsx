"use client";
import { useCallback, useState } from "react";
import { useCopyToClipboard } from "@/hooks/use-copy-to-clip-board";
import { Button } from "@/components/ui/button";
import { Copy } from "../lib/icons";
import SpinLoader from "@/components/ui/spinner";
import { useToast } from "@/components/ui/use-toast";
import { checkVerifyConnection } from "../apis/authapis";
import { ApiError } from "../types/organisation-types";

type DisplayKeyProps = {
  appToken: string;
};

/**
 * Renders a component that displays a key and allows the user to copy it to the clipboard and verify the connection.
 *
 * @param {string} appToken - The token used for verification.
 * @returns {JSX.Element} - The rendered component.
 */
export function DisplayKey({ appToken }: DisplayKeyProps): JSX.Element {
  const [loading, setLoading] = useState(false);
  const [value, copy] = useCopyToClipboard();
  const { toast } = useToast();

  const verifyConnection = useCallback(async () => {
    try {
      setLoading(true);

      const res = await checkVerifyConnection(appToken);

      toast({
        description: res?.message,
      });
    } catch (error) {
      const apiError = error as ApiError;
      toast({
        description:
          apiError?.data?.message ?? "Something went wrong. Please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
    }
  }, [setLoading, toast, checkVerifyConnection, appToken]);

  const handleCopy = () => {
    copy(appToken);
    toast({
      description: "Key is copied to clipboard",
    });
  };

  return (
    <div className="display-key">
      <div className="key-container" onClick={handleCopy}>
        <Copy className="copy-icon" />
        <p>{appToken}</p>
      </div>
      <Button
        className="verify-connection"
        onClick={verifyConnection}
        disabled={loading}
      >
        {loading ? <SpinLoader /> : "Verify Connection"}
      </Button>
    </div>
  );
}
