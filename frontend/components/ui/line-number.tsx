import React from "react";
import { SourceCode } from "./fonts";
import { LineNumberProps } from "@/app/types/json-viewer-types";

const LineNumber: React.FC<LineNumberProps> = React.memo(({ number }) => (
  <span
    className={`line-number ${SourceCode.className}`}
    style={{
      marginRight: "15px",
    }}
  >
    {String(number).padStart(3, " ")}
  </span>
));

export default LineNumber;
