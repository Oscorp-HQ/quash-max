import { FilePdf } from "@phosphor-icons/react";
import React from "react";

/**
 * Functional component that renders a PDF icon along with a text label.
 */

const PdfComponent = React.memo(() => (
  <div className="flex gap-2 items-center cursor-pointer">
    <FilePdf size={40} />
    <span>PDF File</span>
  </div>
));

export default PdfComponent;
