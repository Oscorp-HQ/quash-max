import React from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Plus, UploadSimple } from "@/app/lib/icons";
import RenderIntegrationMenuItems from "./render-integration-menu-items";
import Link from "next/link";

/**
 * Renders an export button with a dropdown menu for exporting data to different integrations.
 *
 * @param {Object} props - The props for the ExportButton component.
 * @param {Array} props.integrationsDone - An array of integrations that have been completed.
 * @param {Function} props.handleExport - A function to handle the export action.
 * @returns {JSX.Element} A React element representing the ExportButton component.
 */

const ExportButton = ({
  integrationsDone,
  handleExport,
}: {
  integrationsDone: string[];
  handleExport: (integration: string) => void;
}): JSX.Element => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button className="toolbar-export-dropdown-trigger">
          <span>Export</span>
          <UploadSimple size={16} />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="toolbar-export-dropdown-content">
        {integrationsDone?.length > 0 ? (
          <>
            <DropdownMenuLabel>Export to</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <RenderIntegrationMenuItems
                integrationsDone={integrationsDone}
                handleExport={handleExport}
              />

              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <Link
                  href={"/settings/integrations"}
                  className="toolbar-add-integration"
                >
                  <Plus size={16} />
                  <span>Add Integration</span>
                </Link>
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        ) : (
          <Link href={"/settings/integrations"}>
            <Button className="toolbar-set-integration" variant="ghost">
              <Plus size={16} />
              <span>Set up Integrations</span>
            </Button>
          </Link>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ExportButton;
