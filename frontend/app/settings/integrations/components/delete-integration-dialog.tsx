import React, { useEffect, useState } from "react";
import { X } from "../../../lib/icons";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import SpinLoader from "@/components/ui/spinner";
import { IntegrationDelete } from "@/app/apis/integrationsapi";
import { useToast } from "@/components/ui/use-toast";
import { IntegrationKeyMapLocal } from "@/app/types/integration-key-map-types";
import { IntegrationLocal } from "@/app/types/integration-types";
import { capitalizeFirstWord } from "@/app/utils/helper";

interface DeleteIntegrationDialogProps {
  deleteAlert: boolean;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  integrationsDone: string[];
  setIntegrationsDone: React.Dispatch<React.SetStateAction<string[]>>;
  integrationData: IntegrationKeyMapLocal;
  integrationSelected: IntegrationLocal;
}

interface IntegrationItem {
  id: string | undefined;
  value: string;
}

const DeleteIntegrationDialog = ({
  deleteAlert,
  setDeleteAlert,
  integrationsDone,
  setIntegrationsDone,
  integrationData,
  integrationSelected,
}: DeleteIntegrationDialogProps) => {
  const [loading, setLoading] = useState(false);
  const [integrationArray, setIntegrationArray] = useState<
    IntegrationItem[] | null
  >(null);

  const [integrationId, setIntegrationId] = useState<string>("");

  const { toast } = useToast();

  const label = capitalizeFirstWord(integrationSelected.value);
  useEffect(() => {
    if (integrationData !== undefined) {
      const integrationArrayTemp: IntegrationItem[] = Object.keys(
        integrationData,
      ).map((key) => ({
        id: integrationData[key as keyof typeof integrationData]!.id,
        value: key,
      }));
      setIntegrationArray(integrationArrayTemp);
    }
  }, [integrationData]);

  useEffect(() => {
    if (integrationArray && integrationArray.length > 0) {
      const temp = integrationArray.find(
        (item) => item.value === integrationSelected.value,
      );
      if (temp && temp.id) {
        setIntegrationId(temp.id);
      }
    }
  }, [integrationArray, integrationSelected.value]);

  const deleteIntegration = async (id: string) => {
    setLoading(true);
    try {
      const { data, message, success } = await IntegrationDelete(id);

      if (success) {
        let temp = [];
        temp = integrationsDone.filter(
          (item: string) => item !== integrationSelected.value,
        );
        setIntegrationsDone(temp.slice(0));

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
      setDeleteAlert(false);
    }
  };

  return (
    <AlertDialog open={deleteAlert}>
      <AlertDialogContent className="integration-alert-content">
        <AlertDialogHeader>
          <AlertDialogTitle className="integration-alert-title">
            {` Remove ${label} Integration`}
          </AlertDialogTitle>
          <AlertDialogDescription className="integration-alert-description">
            <span>Are you sure you want to remove this integration?</span>
            <span>
              {`Tickets exported to your projects on ${label} will remain unchanged.`}
            </span>
            <X
              size={24}
              className="alert-icon"
              onClick={() => {
                setDeleteAlert(false);
              }}
            />
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter className="integration-alert-footer">
          <AlertDialogCancel
            className="alert-cancel integration"
            onClick={() => {
              setDeleteAlert(false);
            }}
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            className="alert-action integration"
            onClick={() => {
              deleteIntegration(integrationId);
            }}
            disabled={loading}
          >
            {loading ? <SpinLoader /> : "Delete"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default DeleteIntegrationDialog;
