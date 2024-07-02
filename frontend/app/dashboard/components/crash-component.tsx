import { FileText } from "@phosphor-icons/react";
import React from "react";

/**
 * React component that displays a crash file icon along with the text "Crash File".
 * This component is memoized for performance optimization.
 */

const CrashComponent = React.memo(() => (
  <div className="flex gap-2 items-center cursor-pointer">
    <FileText size={40} />
    <span>Crash File</span>
  </div>
));

export default CrashComponent;
