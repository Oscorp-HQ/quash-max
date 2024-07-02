import Loader from "@/components/ui/loader";
import {
  LoaderDialog,
  LoaderDialogContent,
} from "../dashboard/components/overlay-loader";

/**
 * Renders a loading component that displays a loader dialog with a loader animation.
 *
 * @returns {JSX.Element} The loading component JSX
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
