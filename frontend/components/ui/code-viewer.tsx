"use client";

import React, { HTMLAttributes, PropsWithChildren } from "react";
import { cn } from "@/lib/utils";

interface CodeProps extends HTMLAttributes<HTMLDivElement> {
  codeString: string;
}

const Code: React.FC<PropsWithChildren<CodeProps>> = ({
  codeString,
  className,
  ...props
}) => {
  return (
    <pre
      className={cn(
        "bg-grey-800 text-white rounded-lg p-4 flex items-start text-start w-full",
        className,
      )}
    >
      <code className="overflow-x-scroll">{codeString}</code>
    </pre>
  );
};
Code.displayName = "Code";

export { Code };
