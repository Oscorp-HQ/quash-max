import Loader from "@/components/ui/loader";
import {
  LoaderDialog,
  LoaderDialogContent,
} from "../dashboard/components/overlay-loader";

/**
 * Renders a loading spinner overlay using the LoaderDialog, LoaderDialogContent, and Loader components.
 * @returns {JSX.Element} The loading spinner overlay.
 */
export default function Loading(): JSX.Element {
  return (
    <div className="loader-dialog-content container">
      <LoaderDialog open={true}>
        <LoaderDialogContent className="loader-dialog-content">
          <Loader />
        </LoaderDialogContent>
      </LoaderDialog>
    </div>
  );
}
