import React from "react";
import LineNumber from "./line-number";
import { SourceCode } from "./fonts";
import { JsonLineProps } from "@/app/types/json-viewer-types";

const JsonLine: React.FC<JsonLineProps> = React.memo(
  ({
    children,
    number,
    isCollapsible,
    isCollapsed,
    onToggleCollapse,
    displayIcon,
  }) => (
    <div
      className="json-line relative"
      style={{ display: "flex" }}
      onClick={onToggleCollapse}
    >
      {number !== null && <LineNumber number={number} />}
      {isCollapsible && (
        <div className="line-number-arrow hover:cursor-pointer absolute left-4">
          {displayIcon && (
            <div
              style={{
                transform: isCollapsed ? "rotate(-90deg)" : "none",
                transition: "transform 0.2s ease",
              }}
            >
              ▼
            </div>
          )}
        </div>
      )}
      <div
        className={`${SourceCode.className} json-line-content`}
        style={{ flex: 1 }}
      >
        {children}
      </div>
    </div>
  ),
);

export default JsonLine;
