import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { ReactNode } from "react";

interface TooltipIconButtonProps {
  tooltipText: string;
  triggerComponent: ReactNode;
  triggerClassName?: string;
  contentClassName?: string;
}

/**
 * TooltipIconButton component that renders a tooltip with the provided text when triggered by a component.
 *
 * @param tooltipText The text to display in the tooltip
 * @param triggerComponent The component that triggers the tooltip
 * @param triggerClassName Optional class name for the trigger component
 * @param contentClassName Optional class name for the tooltip content
 * @returns JSX element representing the TooltipIconButton component
 */

export const TooltipIconButton = ({
  tooltipText,
  triggerComponent,
  triggerClassName = "",
  contentClassName = "",
}: TooltipIconButtonProps) => {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger className={triggerClassName}>
          {triggerComponent}
        </TooltipTrigger>
        <TooltipContent className={contentClassName}>
          <p>{tooltipText}</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
};
