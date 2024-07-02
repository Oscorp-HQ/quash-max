"use client";
import { Table } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { types, statuses, exportValues, priorities } from "../data/data";
import { DataTableFacetedFilter } from "./data-table-faceted-filter";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./calender-select";
import { ArrowsCounterClockwise } from "@/app/lib/icons";
import React from "react";
import ExportButton from "./export-button";

const dateFilterList = [
  "Today",
  "Yesterday",
  "Last 7 days",
  "Last 15 days",
  "Last 30 days",
  "Last 6 Months",
];

interface DataTableToolbarProps<TData> {
  table: Table<TData>;
  dateFilterHandler: any;
  selectedDateFilter: string;
  setSelectedDateFilter: React.Dispatch<React.SetStateAction<string>>;
  exportHandler: any;
  integrationsDone: string[];
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
 * Function that renders the toolbar for a data table, including date filters, refresh button, export options, and faceted filters.
 *
 * @param table The table data to be displayed
 * @param selectedDateFilter The selected date filter value
 * @param setSelectedDateFilter Function to set the selected date filter
 * @param exportHandler Function to handle export actions
 * @param integrationsDone List of integrations that are already set up
 * @param applicationSelected The selected application
 * @param pageIndex The current page index
 * @param pageSize The page size
 * @param organisationKey The key of the organization
 * @param getReports Function to get reports based on selected filters
 */

export function DataTableToolbar<TData>({
  table,
  selectedDateFilter,
  setSelectedDateFilter,
  exportHandler,
  integrationsDone,
  applicationSelected,
  pageIndex,
  pageSize,
  organisationKey,
  getReports,
}: DataTableToolbarProps<TData>) {
  const handleDateFilterChange = (value: string) => {
    setSelectedDateFilter(value);
  };

  const handleRefreshClick = () => {
    getReports(applicationSelected, pageIndex, pageSize, organisationKey);
  };

  const handleExport = (integration: string) => {
    if (table.getFilteredSelectedRowModel().rows.length > 0) {
      exportHandler(
        table
          .getFilteredSelectedRowModel()
          .rows.map((row: any) => row.original.id),
        integration,
      );
    }
  };

  const renderFacetedFilters = () => {
    const columns = [
      { key: "exported", title: "All Bugs", options: exportValues },
      { key: "type", title: "Bug Type", options: types },
      { key: "priority", title: "Priority", options: priorities },
      { key: "reportedByName", title: "Reported By", options: [] },
      { key: "source", title: "Platform", options: [] },
      { key: "status", title: "Status", options: statuses },
    ];

    return columns.map(
      ({ key, title, options }) =>
        table.getColumn(key) && (
          <DataTableFacetedFilter
            key={key}
            column={table.getColumn(key)}
            title={title}
            options={options}
          />
        ),
    );
  };

  return (
    <div className="data-table-toolbar-conatiner">
      <div className="data-table-toolbar-content">{renderFacetedFilters()}</div>
      <div className="data-table-toolbar-content">
        <Select
          value={selectedDateFilter}
          onValueChange={handleDateFilterChange}
        >
          <SelectTrigger className="date-select-dropdown-trigger">
            <SelectValue placeholder="All Bugs" className="" />
          </SelectTrigger>
          <SelectContent className="date-select-dropdown-content">
            <SelectGroup>
              {dateFilterList.map((el: string, index) => (
                <SelectItem key={index} value={el}>
                  {el}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
        <Button
          className="toolbar-refresh-button"
          variant="outline"
          onClick={handleRefreshClick}
        >
          <ArrowsCounterClockwise size={16} />
        </Button>

        <ExportButton
          integrationsDone={integrationsDone}
          handleExport={handleExport}
        />
      </div>
    </div>
  );
}
