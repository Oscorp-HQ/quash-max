import React, { useState, useMemo, useCallback } from "react";
import JsonLine from "./json-line";
import { JsonViewerProps } from "@/app/types/json-viewer-types";

const JsonViewer: React.FC<JsonViewerProps> = ({ data }) => {
  const initialCollapsedState = useMemo(() => {
    const state: Record<string, boolean> = {};
    const setInitialCollapsedState = (obj: Record<string, any>, path = "") => {
      for (const key in obj) {
        const newPath = path ? `${path}.${key}` : key;
        if (typeof obj[key] === "object" && obj[key] !== null) {
          state[newPath] = false;
          setInitialCollapsedState(obj[key], newPath);
        }
      }
    };
    setInitialCollapsedState(data);
    return state;
  }, [data]);

  const [collapsed, setCollapsed] = useState<Record<string, boolean>>(
    initialCollapsedState,
  );

  const toggleCollapse = useCallback((key: string) => {
    setCollapsed((prevCollapsed) => ({
      ...prevCollapsed,
      [key]: !prevCollapsed[key],
    }));
  }, []);

  const renderData = useMemo(
    () =>
      (
        data: Record<string, any>,
        currentIndent = 0,
        lineNumber = 1,
        keyPath = "root",
      ): { lines: React.ReactNode[]; lineNumber: number } => {
        let lines: React.ReactNode[] = [];
        let currentLine = lineNumber;

        if (typeof data !== "object" || data === null) {
          lines.push(
            <JsonLine
              key={currentLine}
              number={currentLine}
              isCollapsible={false}
              isCollapsed={false}
              onToggleCollapse={() => {}}
              displayIcon={false}
            >
              <span>{JSON.stringify(data, null, 2)}</span>
            </JsonLine>,
          );
          return { lines, lineNumber: currentLine + 1 };
        }

        const isArray = Array.isArray(data);
        const entries = Object.entries(data);

        const hasProperties = entries.length > 0;

        lines.push(
          <JsonLine
            key={currentLine}
            number={currentLine}
            isCollapsible={hasProperties}
            isCollapsed={collapsed[keyPath]}
            onToggleCollapse={() => toggleCollapse(keyPath)}
            displayIcon={false}
          >
            <div style={{ marginLeft: `${currentIndent * 20}px` }}>
              {isArray ? "[" : "{"}
              {collapsed[keyPath] && <span>...</span>}
            </div>
          </JsonLine>,
        );
        currentLine++;

        if (!collapsed[keyPath]) {
          for (const [key, value] of entries) {
            const newKeyPath = `${keyPath}.${key}`;
            const isCollapsible = typeof value === "object" && value !== null;

            lines.push(
              <JsonLine
                key={currentLine}
                number={currentLine}
                isCollapsible={isCollapsible}
                isCollapsed={collapsed[newKeyPath]}
                onToggleCollapse={() =>
                  isCollapsible && toggleCollapse(newKeyPath)
                }
                displayIcon={true}
              >
                <div style={{ marginLeft: `${(currentIndent + 1) * 20}px` }}>
                  {key}:{" "}
                  {isCollapsible &&
                    collapsed[newKeyPath] &&
                    (Array.isArray(value) ? "[...]" : "{...}")}
                  {!isCollapsible && JSON.stringify(value, null, 2)}
                </div>
              </JsonLine>,
            );
            currentLine++;

            if (isCollapsible && !collapsed[newKeyPath]) {
              const nestedResult = renderData(
                value,
                currentIndent + 1,
                currentLine,
                newKeyPath,
              );
              lines = [...lines, ...nestedResult.lines];
              currentLine = nestedResult.lineNumber;
            }
          }
        }

        lines.push(
          <JsonLine
            key={currentLine}
            number={currentLine}
            isCollapsible={hasProperties}
            isCollapsed={collapsed[keyPath]}
            onToggleCollapse={() => toggleCollapse(keyPath)}
            displayIcon={false}
          >
            <div style={{ marginLeft: `${currentIndent * 20}px` }}>
              {isArray ? "]" : "}"}
            </div>
          </JsonLine>,
        );
        currentLine++;

        return { lines, lineNumber: currentLine };
      },
    [collapsed, toggleCollapse],
  );

  const renderedData = renderData(data);

  return <div className="json-viewer ">{renderedData.lines}</div>;
};

export default JsonViewer;
