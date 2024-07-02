import Loader from "@/components/ui/loader";
import {
  LoaderDialog,
  LoaderDialogContent,
} from "../dashboard/components/overlay-loader";
export default function Loading() {
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
