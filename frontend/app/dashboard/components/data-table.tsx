"use client";

import * as React from "react";
import {
  ColumnDef,
  ColumnFiltersState,
  Row,
  SortingState,
  VisibilityState,
  flexRender,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getSortedRowModel,
  useReactTable,
} from "@tanstack/react-table";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

import { DataTablePagination } from "./data-table-pagination";
import { DataTableToolbar } from "./data-table-toolbar";
import { useEffect } from "react";
import EmptyState from "./empty-state";
import EmptySkeleton from "./empty-skeleton";
interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setSelectedRow: any;
  selectedRow: any;
  noResults: boolean;
  setPageSize: React.Dispatch<React.SetStateAction<number>>;
  setPageIndex: React.Dispatch<React.SetStateAction<number>>;
  totalPageCount: number;
  CurrentPageCount: number;
  dateFilterHandler: any;
  selectedDateFilter: string;
  setSelectedDateFilter: React.Dispatch<React.SetStateAction<string>>;
  setExportList: React.Dispatch<React.SetStateAction<Row<TData>[]>>;
  exportHandler: any;
  integrationsDone: string[];
  noReports: boolean;
  fetchingReports: boolean;
  applicationSelected: string;
  pageIndex: number;
  pageSize: number;
  organisationKey: string;
  getReports: (
    app_id: string,
    page_index: number,
    page_size: number,
    org_key: string,
  ) => Promise<void>;
}

/**
 * Component for rendering a data table with pagination and toolbar.
 *
 * @param {DataTableProps} props - The props for the DataTable component.
 * @returns {JSX.Element} - The rendered DataTable component.
 */

export function DataTable<TData, TValue>({
  columns,
  data,
  setOpen,
  setSelectedRow,
  selectedRow,
  noResults,
  setPageSize,
  setPageIndex,
  totalPageCount,
  CurrentPageCount,
  dateFilterHandler,
  selectedDateFilter,
  setSelectedDateFilter,
  setExportList,
  exportHandler,
  integrationsDone,
  noReports,
  fetchingReports,
  applicationSelected,
  pageIndex,
  pageSize,
  organisationKey,
  getReports,
}: DataTableProps<TData, TValue>) {
  const [rowSelection, setRowSelection] = React.useState({});
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    [],
  );
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const table = useReactTable({
    data,
    columns,
    state: {
      sorting,
      columnVisibility,
      rowSelection,
      columnFilters,
    },
    manualPagination: true,
    pageCount: totalPageCount,
    enableRowSelection: true,
    onRowSelectionChange: setRowSelection,
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFacetedRowModel: getFacetedRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),
  });

  useEffect(() => {
    if (table.getFilteredSelectedRowModel().rows.length > 0) {
      setExportList(table.getFilteredSelectedRowModel().rows);
    }
  }, [
    table.getFilteredSelectedRowModel(),
    table.getFilteredSelectedRowModel().rows,
  ]);

  return (
    <div className="dashboard-data-table-conatiner">
      <div className="dashboard-data-table-toolbar-conatiner">
        <DataTableToolbar
          table={table}
          dateFilterHandler={dateFilterHandler}
          selectedDateFilter={selectedDateFilter}
          setSelectedDateFilter={setSelectedDateFilter}
          exportHandler={exportHandler}
          integrationsDone={integrationsDone}
          applicationSelected={applicationSelected}
          pageIndex={pageIndex}
          pageSize={pageSize}
          organisationKey={organisationKey}
          getReports={getReports}
        />
      </div>

      <Table>
        <TableHeader>
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map((header) => {
                return (
                  <TableHead
                    key={header.id}
                    className="dashboard-data-table-head"
                  >
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext(),
                        )}
                  </TableHead>
                );
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map((row) => (
              <TableRow
                key={row.id}
                data-state={
                  (row.getIsSelected() || selectedRow?.id === row.id) &&
                  "selected"
                }
                className="dashboard-data-table-row"
              >
                {row.getVisibleCells().map((cell) => (
                  <TableCell
                    key={cell.id}
                    className={`dashboard-data-table-cell ${
                      cell.column.id === "select" && "select"
                    }  ${
                      (cell.column.id === "title" || cell.column.id === "id") &&
                      "title"
                    }`}
                    onClick={() => {
                      if (
                        cell.column.id === "title" ||
                        cell.column.id === "id"
                      ) {
                        setSelectedRow(row.original);
                        setOpen(true);
                      }
                    }}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell
                colSpan={columns.length}
                className="dashboard-data-table-skeleton-cell"
              >
                {noReports ? (
                  <div className="dashboard-data-table-skeleton-container">
                    <EmptySkeleton />
                    <EmptyState />
                  </div>
                ) : fetchingReports ? (
                  <div className="dashboard-data-table-skeleton-container">
                    <EmptySkeleton />
                  </div>
                ) : (
                  <div className="dashboard-data-table-skeleton-container">
                    <div className="data-table-no-result-container">
                      <span className="data-table-no-result-title">
                        No Matching Search Results
                      </span>
                      <span className="data-table-no-result-description">
                        Clear some filters to show results
                      </span>
                    </div>
                  </div>
                )}
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
      <DataTablePagination
        table={table}
        setPageSize={setPageSize}
        setPageIndex={setPageIndex}
        CurrentPageCount={CurrentPageCount}
      />
    </div>
  );
}
