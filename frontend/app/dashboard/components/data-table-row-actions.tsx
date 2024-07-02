"use client";
import { DotsVerticalIcon } from "@radix-ui/react-icons";
import { Row } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { PencilSimple, Plus, Trash, UploadSimple } from "@/app/lib/icons";
import Link from "next/link";
import { useRouter } from "next/navigation";
import RenderIntegrationMenuItems from "./render-integration-menu-items";
import { Report } from "@/app/types/dashboard-types";

interface DataTableRowActionsProps<TData> {
  row: Row<TData>;
  setSelectedRow: any;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setDeleteAlert: React.Dispatch<React.SetStateAction<boolean>>;
  exportHandler: any;
  bugListOriginal: Report[];
  setIsEdit: React.Dispatch<React.SetStateAction<boolean>>;
  integrationsDone: string[];
  setRowActionEdit: React.Dispatch<React.SetStateAction<boolean>>;
}

/**
 * Component that renders a dropdown menu with row actions for a data table row item.
 * It includes options to export, edit, and delete the row item, as well as manage integrations.
 * Uses various UI components like Button, Tooltip, DropdownMenu, etc.
 *
 * @param {DataTableRowActionsProps} props - Props for the DataTableRowActions component.
 * @returns {JSX.Element} - The rendered dropdown menu with row actions.
 */

export function DataTableRowActions<TData extends Report>({
  row,
  setSelectedRow,
  setOpen,
  setDeleteAlert,
  exportHandler,
  bugListOriginal,
  setIsEdit,
  integrationsDone,
  setRowActionEdit,
}: DataTableRowActionsProps<TData>): JSX.Element {
  const task: Report = row.original;
  const router = useRouter();

  const handleExport = (integration: string) => {
    exportHandler([task.id], integration, bugListOriginal);
  };

  const handleEdit = () => {
    setRowActionEdit(true);
    setSelectedRow(task);
    setIsEdit(true);
    setOpen(true);
  };

  const handleDelete = () => {
    setSelectedRow(task);
    setTimeout(() => {
      setDeleteAlert(true);
    }, 50);
  };

  return (
    <DropdownMenu>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                className="flex h-8 w-8 p-0 data-[state=open]:bg-muted"
              >
                <DotsVerticalIcon className="h-4 w-4" />
                <span className="sr-only">Open menu</span>
              </Button>
            </DropdownMenuTrigger>
          </TooltipTrigger>
          <TooltipContent>
            <p>More</p>
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>

      <DropdownMenuContent align="end" className="row-action-content-conatiner">
        <DropdownMenuSub>
          <DropdownMenuSubTrigger className="row-action-edit">
            <UploadSimple size={16} className="row-action-edit-icon" />
            <span>Export</span>
          </DropdownMenuSubTrigger>
          <DropdownMenuPortal>
            <DropdownMenuSubContent>
              {integrationsDone?.length > 0 ? (
                <>
                  <RenderIntegrationMenuItems
                    integrationsDone={integrationsDone}
                    handleExport={handleExport}
                  />

                  <DropdownMenuSeparator />
                  <DropdownMenuItem>
                    <div
                      onClick={() => {
                        router.push("/settings/integrations");
                      }}
                      className="row-action-add-integration"
                    >
                      <Plus size={16} />
                      <span>Add Integration</span>
                    </div>
                  </DropdownMenuItem>
                </>
              ) : (
                <Link href={"/settings/integrations"}>
                  <Button
                    className="row-action-set-integration"
                    variant="ghost"
                  >
                    <Plus size={16} />
                    <span>Set up Integrations</span>
                  </Button>
                </Link>
              )}
            </DropdownMenuSubContent>
          </DropdownMenuPortal>
        </DropdownMenuSub>
        <DropdownMenuItem className="row-action-edit" onClick={handleEdit}>
          <PencilSimple size={16} className="row-action-edit-icon" />
          <span>Edit</span>
        </DropdownMenuItem>
        <DropdownMenuItem className="row-action-delete" onClick={handleDelete}>
          <Trash size={16} />
          <span>Delete</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
