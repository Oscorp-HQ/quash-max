import React, { useState } from "react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { X } from "../../../lib/icons";
import { RequestIntegration } from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import SpinLoader from "@/components/ui/spinner";

interface RequestIntegrationDialogProps {
  requestForm: boolean;
  setRequestForm: React.Dispatch<React.SetStateAction<boolean>>;
}

const RequestIntegrationDialog = ({
  requestForm,
  setRequestForm,
}: RequestIntegrationDialogProps) => {
  const [loading, setLoading] = useState(false);
  const [integrationRequest, setIntegrationRequest] = useState("");
  const { toast } = useToast();

  const requestIntegration = async (request: string) => {
    let body = {
      featureRequest: request,
    };
    setLoading(true);
    try {
      const { data, message, success } = await RequestIntegration(body);

      if (success) {
        toast({
          description: message,
        });
      } else {
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);

      toast({
        description: "Something went wrong, please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
      setRequestForm(false);
      setIntegrationRequest("");
    }
  };
  return (
    <AlertDialog open={requestForm}>
      <AlertDialogContent className="request-alert-dialog">
        <div className="request-alert-dialog-content">
          <AlertDialogTitle>Request Integration</AlertDialogTitle>
          <AlertDialogDescription className="description">
            Let us know what integrations you require, so that we can build them
            on priority.
            <textarea
              placeholder="We want a space rocket like SpaceX"
              value={integrationRequest}
              onChange={(e) => {
                setIntegrationRequest(e.target.value);
              }}
              className="integration-text-area"
            />
            <X
              size={24}
              onClick={() => {
                setRequestForm(false);
                setIntegrationRequest("");
              }}
              className="icon"
            />
          </AlertDialogDescription>
        </div>
        <AlertDialogFooter className="request-alert-dialog-footer">
          <AlertDialogAction
            className="request-integration-submit"
            onClick={() => {
              requestIntegration(integrationRequest);
            }}
            disabled={loading || !integrationRequest}
          >
            {loading ? <SpinLoader /> : "Submit"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default RequestIntegrationDialog;
