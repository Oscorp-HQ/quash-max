import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from "@radix-ui/react-icons";
import { Table } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import React, { useEffect } from "react";

const rowsLimits = [10, 20, 30, 40, 50];

interface DataTablePaginationProps<TData> {
  table: Table<TData>;
  setPageSize: React.Dispatch<React.SetStateAction<number>>;
  setPageIndex: React.Dispatch<React.SetStateAction<number>>;
  CurrentPageCount: number;
}
/**
 * Function component for rendering pagination controls for a data table.
 * It includes buttons for navigating to the first, previous, next, and last pages,
 * a select dropdown for choosing the number of rows per page, and displays the current page count.
 * Uses icons from @radix-ui/react-icons and interacts with a Table component from @tanstack/react-table.
 *
 * @param table The Table component instance for which pagination is being handled
 * @param setPageSize Function to set the number of rows per page
 * @param setPageIndex Function to set the current page index
 * @param CurrentPageCount The current page count for display purposes
 * @returns JSX element containing the pagination controls
 */
export function DataTablePagination<TData>({
  table,
  setPageSize,
  setPageIndex,
  CurrentPageCount,
}: DataTablePaginationProps<TData>) {
  useEffect(() => {
    setPageIndex(table.getState().pagination.pageIndex);
  }, [table.getState().pagination.pageIndex]);

  return (
    <div className="data-table-pagination-container">
      <div className="data-table-pagination-rows-selected">
        {table.getFilteredSelectedRowModel().rows.length === 0 ? (
          <>
            Showing {table.getFilteredRowModel().rows.length} of{" "}
            {table.getFilteredRowModel().rows.length} row(s).
          </>
        ) : (
          <>
            {table.getFilteredSelectedRowModel().rows.length} of{" "}
            {table.getFilteredRowModel().rows.length} row(s) selected.
          </>
        )}
      </div>
      <div className="data-table-pagination-values-container space-x-6 lg:space-x-8">
        <div className="data-table-pagination-rows-dropdown space-x-2">
          <p className="data-table-pagination-rows-dropdown-title">
            Rows per page
          </p>
          <Select
            value={`${table.getState().pagination.pageSize}`}
            onValueChange={(value: string) => {
              table.setPageSize(Number(value));
              setPageSize(Number(value));
            }}
          >
            <SelectTrigger className="data-table-pagination-rows-dropdown-trigger h-8 w-[70px]">
              <SelectValue placeholder={table.getState().pagination.pageSize} />
            </SelectTrigger>
            <SelectContent side="top">
              {rowsLimits.map((pageSize) => (
                <SelectItem key={pageSize} value={`${pageSize}`}>
                  {pageSize}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="data-table-pagination-rows-values flex w-[100px] items-center justify-center text-sm font-medium">
          Page {table.getPageCount() === 0 ? 0 : CurrentPageCount + 1} of{" "}
          {table.getPageCount()}
        </div>
        <div className="data-table-pagination-button-container flex items-center space-x-2">
          <Button
            variant="outline"
            className="data-table-pagination-button hidden h-8 w-8 p-0 lg:flex"
            onClick={() => {
              table.setPageIndex(0);
            }}
            disabled={!table.getCanPreviousPage()}
          >
            <span className="sr-only">Go to first page</span>
            <DoubleArrowLeftIcon className="data-table-pagination-arrow-icon h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="data-table-pagination-button h-8 w-8 p-0"
            onClick={() => {
              table.previousPage();
            }}
            disabled={!table.getCanPreviousPage()}
          >
            <span className="sr-only">Go to previous page</span>
            <ChevronLeftIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="data-table-pagination-button h-8 w-8 p-0"
            onClick={() => {
              table.nextPage();
            }}
            disabled={!table.getCanNextPage()}
          >
            <span className="sr-only">Go to next page</span>
            <ChevronRightIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="data-table-pagination-button hidden h-8 w-8 p-0 lg:flex"
            onClick={() => {
              table.setPageIndex(table.getPageCount() - 1);
            }}
            disabled={!table.getCanNextPage()}
          >
            <span className="sr-only">Go to last page</span>
            <DoubleArrowRightIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
