import React, { Suspense } from "react";
import {
  LoaderDialog,
  LoaderDialogContent,
} from "../components/overlay-loader";
import Loader from "@/components/ui/loader";

/**
 * Functional component SuspenseWrapper that wraps its children with a Suspense component,
 * providing a fallback loader while the main content is loading.
 */

type SuspenseWrapperProps = {
  children: React.ReactNode;
};

const SuspenseWrapper: React.FC<SuspenseWrapperProps> = ({ children }) => {
  return (
    <Suspense
      fallback={
        <LoaderDialog open={true}>
          <LoaderDialogContent className="loader-dialog-content">
            <Loader />
          </LoaderDialogContent>
        </LoaderDialog>
      }
    >
      {children}
    </Suspense>
  );
};

export default SuspenseWrapper;
