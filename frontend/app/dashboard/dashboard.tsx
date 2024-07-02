"use client";
import { DataTable } from "./components/data-table";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useEffect, useMemo, useState } from "react";
import { Plus, User } from "../lib/icons";
import { types, statuses, priorities } from "./data/data";
import { ColumnDef, Row } from "@tanstack/react-table";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { DataTableColumnHeader } from "./components/data-table-column-header";
import { DataTableRowActions } from "./components/data-table-row-actions";
import { useAutoAnimate } from "@formkit/auto-animate/react";
import { Skeleton } from "@/components/ui/skeleton";
import {
  DeleteReport,
  ExportReports,
  GetReports,
  GetReportsById,
} from "../apis/dashboardapis";
import { filterData } from "./components/filter-data-on-date";
import { signOut } from "next-auth/react";
import { useCustomToast } from "./components/custom-use-toast";
import NoSDKState from "./components/no-sdk";
import EmptySkeleton from "./components/empty-skeleton";
import MobileScreen from "./components/mobile-screen";
import React from "react";
import { capitalizeFirstCharacter } from "../utils/helper";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { CustomToaster } from "./components/cutom-toaster";
import SuspenseWrapper from "./components/suspense-wrapper";
import Image from "next/image";
import success from "../../public/success.svg";
import {
  Organisation,
  OrganisationApiResponse,
} from "../types/organisation-types";
import { App } from "../types/application-types";
import { Report, ReportsByIdApiResponse } from "../types/dashboard-types";
const SidePane = React.lazy(() => import("./components/side-pane"));
const IntegrationBanner = React.lazy(
  () => import("./components/integration-banner"),
);
/**
 * This code snippet defines a functional component called Dashboard, which renders a dashboard interface.
 * It includes various UI components like DataTable, Select, Tooltip, etc., and handles state for managing bug reports.
 * The component fetches data, filters, exports, and displays bug reports based on user interactions.
 */

export default function Dashboard({
  orgData,
  inviteMember,
  integration,
  integrationsDone,
}: {
  orgData: OrganisationApiResponse;
  inviteMember: boolean;
  integration: boolean;
  integrationsDone: string[];
}) {
  const [open, setOpen] = useState(false);
  const [bugList, setBugList] = useState<Report[]>([]);
  const [bugListOriginal, setBugListOriginal] = useState<Report[]>([]);
  const [exportList, setExportList] = useState<Row<Report>[]>([]);
  const [isEdit, setIsEdit] = useState(false);
  const [reportExported, setReportExported] = useState(false);
  const [noResults, setNoResults] = useState(false);
  const [noReports, setNoReports] = useState(false);
  const [savingData, setSavingData] = useState(false);
  const [fetchingReports, setFetchingReports] = useState(false);
  const [discardChangesAltert, setDiscardChangesAltert] = useState(false);
  const [deleteAlert, setDeleteAlert] = useState(false);
  const [appVerified, setAppVerified] = useState(false);
  const { toastCustom, dismiss } = useCustomToast();
  const [selectedRow, setSelectedRow] = useState<Report | null>(null);
  const [organisationData, setOrganisationData] = useState<Organisation | null>(
    null,
  );
  const [organisationKey, setOrganisationKey] = useState<string>("");
  const [applicationSelected, setApplicationSelected] = useState<string>("");
  const [parent, enableAnimations] = useAutoAnimate(/* optional config */);
  const [pageSize, setPageSize] = useState(10);
  const [pageIndex, setPageIndex] = useState(0);
  const [totalPageCount, setTotalPageCount] = useState(0);
  const [CurrentPageCount, setCurrentPageCount] = useState(0);
  const [selectedDateFilter, setSelectedDateFilter] = useState("Last 30 days");
  const [showBanner, setShowBanner] = useState(false);
  const [rowActionEdit, setRowActionEdit] = useState(false);
  const [toastObject, setToastObject] = useState({
    message: "",
    type: "",
  });
  const router = useRouter();
  const searchParams = useSearchParams();

  const ticketNumber = searchParams.get("ticketNumber");

  useEffect(() => {
    if (ticketNumber && bugListOriginal.length > 0) {
      const row = bugListOriginal.find(
        (bug: Report) => bug.id === ticketNumber,
      );

      if (row) {
        setSelectedRow(row);
        setOpen(true);
      } else {
        GetReportsById(ticketNumber)
          .then((row: ReportsByIdApiResponse) => {
            if (row.success) {
              setSelectedRow(row.data);
              setOpen(true);
            } else {
              setToastObject({
                message: "Something went wrong!",
                type: "error",
              });
              toastDismiss();
            }
          })
          .catch((error: unknown) => {
            setToastObject({
              message: "Something went wrong!",
              type: "error",
            });
            toastDismiss();
            console.error("Error fetching data:", error);
          });
      }
    }
  }, [ticketNumber, bugListOriginal]);

  useEffect(() => {
    if (integration || inviteMember) {
      setShowBanner(true);
    }
    getOrg();
  }, []);

  const toastDismiss = () => {
    setTimeout(() => {
      dismiss();
    }, 2000);
  };

  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };

  useEffect(() => {
    if (applicationSelected) {
      getReports(applicationSelected, pageIndex, pageSize, organisationKey);
    }
  }, [pageIndex, pageSize]);

  useEffect(() => {
    if (bugListOriginal.length > 0) {
      dateFilterHandler(selectedDateFilter, bugListOriginal);
    } else {
      setBugList([]);
      setNoReports(true);
    }
  }, [selectedDateFilter, bugListOriginal]);

  const dateFilterHandler = (filter: string, list: Report[]) => {
    let temp = filterData(filter, list);
    if (temp.length === 0) {
      setNoResults(true);
    }

    setBugList(temp.slice(0));
    setFetchingReports(false);
  };

  const dateFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat("en-IN", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
      }),
    [],
  );

  const getReports = async (
    app_id: string,
    page_index: number,
    page_size: number,
    org_key: string,
  ) => {
    setOpen(false);

    setFetchingReports(true);
    setBugList([]);
    setNoResults(false);
    setNoReports(false);
    try {
      const { data, success } = await GetReports(
        app_id,
        page_index,
        page_size,
        org_key,
      );
      if (success) {
        if (data?.reports.length === 0) {
          setFetchingReports(false);
          setNoReports(true);
        } else {
          setTotalPageCount(data?.meta?.totalPages);
          setCurrentPageCount(data?.meta?.currentPage);
          let tasks = data?.reports?.map((report: Report) => ({
            ...report,
            reportedByName: report.reportedBy.fullName,
            exported: report.exportedOn === null ? false : true,
            source: capitalizeFirstCharacter(report.source),
            priority: report?.priority ? report?.priority : "NOT_DEFINED",
          }));

          setBugListOriginal(tasks.slice(0));
          dateFilterHandler(selectedDateFilter, tasks.slice(0));
        }
        return data;
      }
    } catch (error) {
      setFetchingReports(false);
      setNoReports(true);
    }
  };

  const getOrg = async () => {
    try {
      const { data, success } = await orgData;
      if (success) {
        setOrganisationKey(data?.organisationKey);
        setOrganisationData(data);
        if (data.organisationApps.length > 0) {
          setAppVerified(true);
          let app =
            window.localStorage.getItem("appselected") ||
            data?.organisationApps[0]?.appId;

          setApplicationSelected(app);
          window.localStorage.setItem("appselected", app);
          getReports(app, pageIndex, pageSize, data?.organisationKey);
        }
      }
    } catch (error) {
      handleSignOut();
      console.log(error);
    }
  };

  const exportHandler = async (
    list: string[],
    type: string,
    listOriginal: Report[] = bugListOriginal,
  ) => {
    const numberOfTickets = list.length;
    let numberOfBugTickets: number = 0;
    let numberOfCrashTickets: number = 0;
    let numberOfUiTickets: number = 0;
    exportList.map((ticket: Row<Report>) => {
      if (ticket.original.type === "CRASH") {
        numberOfCrashTickets = numberOfCrashTickets + 1;
      } else if (ticket.original.type === "BUG") {
        numberOfBugTickets = numberOfBugTickets + 1;
      } else if (ticket.original.type === "UI") {
        numberOfUiTickets = numberOfUiTickets + 1;
      }
    });

    toastCustom({
      description: "",
    });
    setToastObject({
      message: "Exporting tickets",
      type: "load",
    });
    const body = {
      issues: list,
    };

    try {
      const { success } = await ExportReports(body, type);
      if (success) {
        let temp: Report[] = listOriginal.slice(0);

        list?.map((id: string) => {
          temp = temp.map((bug: Report) => {
            if (id === bug.id) {
              return {
                ...bug,
                exported: true,
                exportedOn: new Date().toString(),
              };
            } else {
              return bug;
            }
          });
        });
        if (list.length === 1) {
          setReportExported(true);
        }

        setBugListOriginal([...temp]);
        setToastObject({
          message: `Exported`,
          type: "success",
        });
        toastDismiss();
      } else {
        setToastObject({
          message: "Could not export the tickets",
          type: "error",
        });
        toastDismiss();
      }
    } catch (error) {
      setToastObject({
        message: "Could not export the tickets",
        type: "error",
      });
      toastDismiss();
      console.log(error);
    }
  };

  const taskDeleteHandler = async (id: string) => {
    setDeleteAlert(false);
    toastCustom({
      description: "",
    });
    setToastObject({
      message: "Deleting ticket",
      type: "load",
    });
    try {
      const { success } = await DeleteReport(id);
      if (success) {
        let temp = bugList.filter((bug: Report) => bug.id !== id);
        setBugListOriginal([...temp]);
        setOpen(false);

        setToastObject({
          message: "Deleted",
          type: "success",
        });
        toastDismiss();
      } else {
        setToastObject({
          message: "Could not delete the ticket",
          type: "error",
        });
        toastDismiss();
      }
    } catch (error) {
      console.log(error);

      setToastObject({
        message: "Could not delete the ticket",
        type: "error",
      });
      toastDismiss();
    }
  };

  const columns: ColumnDef<Report>[] = useMemo(
    () => [
      {
        id: "select",
        header: ({ table }) => (
          <Checkbox
            required={table.getIsAllPageRowsSelected()}
            name="header"
            checked={
              table.getIsAllPageRowsSelected() ||
              Boolean(table.getFilteredSelectedRowModel().rows.length)
            }
            onCheckedChange={(value) =>
              table.toggleAllPageRowsSelected(!!value)
            }
            aria-label="Select all"
            className="data-table-column-check-box"
          />
        ),
        cell: ({ row }) => (
          <Checkbox
            name="column"
            checked={row.getIsSelected()}
            onCheckedChange={(value) => row.toggleSelected(!!value)}
            aria-label="Select row"
            className="data-table-column-check-box translate-y-[2px] "
          />
        ),
        enableSorting: false,
        enableHiding: false,
      },
      {
        accessorKey: "exported",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="" />
        ),
        cell: ({ row }: { row: Row<Report> }) => (
          <div className="data-table-column-exported">
            {row.getValue("exported") ? (
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger>
                    <Image
                      src={success}
                      alt="success icon"
                      width={16}
                      height={16}
                      priority
                    />
                  </TooltipTrigger>
                  <TooltipContent>
                    <p className="hover:cursor-none">
                      Exported on{" "}
                      {row.original.exportedOn &&
                        dateFormatter.format(new Date(row.original.exportedOn))}
                    </p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            ) : (
              ""
            )}
          </div>
        ),
        filterFn: (row, id, value) => {
          return value.includes(row.getValue("exported"));
        },
        enableSorting: false,
        enableHiding: false,
      },
      {
        accessorKey: "id",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="BUG ID" />
        ),
        cell: ({ row }) => {
          const [showTooltip, setShowTooltip] = useState(false);

          const handleMouseEnter = (e: React.MouseEvent<HTMLElement>) => {
            const element = e.currentTarget;
            if (element.scrollWidth > element.clientWidth) {
              setShowTooltip(true);
            }
          };

          const handleMouseLeave = () => {
            setShowTooltip(false);
          };
          return (
            <TooltipProvider>
              <Tooltip open={showTooltip}>
                <TooltipTrigger className="flex">
                  <div
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                    className="data-table-column-bug-id"
                  >
                    {row.getValue("id")}
                  </div>
                </TooltipTrigger>
                <TooltipContent className="">
                  {row.getValue("id")}
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          );
        },
        enableSorting: true,
        enableHiding: false,
      },
      {
        accessorKey: "title",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="TITLE" />
        ),
        cell: ({ row }) => {
          const [showTooltip, setShowTooltip] = useState(false);

          const handleMouseEnter = (e: React.MouseEvent<HTMLElement>) => {
            const element = e.currentTarget;
            if (element.scrollWidth > element.clientWidth) {
              setShowTooltip(true);
            }
          };

          const handleMouseLeave = () => {
            setShowTooltip(false);
          };
          return (
            <TooltipProvider>
              <Tooltip open={showTooltip}>
                <TooltipTrigger className="flex">
                  <div
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                    className="data-table-column-title"
                  >
                    {row.getValue("title")}
                  </div>
                </TooltipTrigger>
                {
                  <TooltipContent className="">
                    {row.getValue("title")}
                  </TooltipContent>
                }
              </Tooltip>
            </TooltipProvider>
          );
        },
      },
      {
        accessorKey: "type",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="TYPE" />
        ),
        cell: ({ row }) => {
          const label = types.find(
            (label) => label.value === row.original.type,
          );

          return (
            <div className="data-table-column-bug-type">
              {label && (
                <Badge variant="outline" className="data-table-column-badge">
                  {label.icon}
                  <span>{label.label}</span>
                </Badge>
              )}
            </div>
          );
        },
        filterFn: (row, id, value) => {
          return value.includes(row.getValue("type"));
        },
        enableSorting: false,
      },
      {
        accessorKey: "priority",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="PRIORITY" />
        ),
        cell: ({ row }) => {
          const priority = priorities.find((priority) => {
            return row.original.priority
              ? priority.value === row.original.priority
              : priority.value === "NOT_DEFINED";
          });

          return (
            <div className="flex ">
              {priority && (
                <div className="data-table-column-priority">
                  {priority.icon}
                  <span>{priority.label}</span>
                </div>
              )}
            </div>
          );
        },
        filterFn: (row, id, value) => {
          return value.includes(row.getValue("priority"));
        },
        enableSorting: true,
        enableHiding: false,
      },
      {
        accessorKey: "source",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="PLATFORM" />
        ),
        cell: ({ row }) => {
          return (
            <div className="data-table-column-platform">
              <span className="data-table-column-platform-value">
                {row.getValue("source")}
              </span>
            </div>
          );
        },
        filterFn: (row, id, value) => {
          return value.includes(row.getValue("source"));
        },
        enableSorting: false,
      },
      {
        accessorKey: "reportedByName",
        header: ({ column }) => (
          <DataTableColumnHeader
            column={column}
            title="REPORTED BY"
            className="min-w-[100px]"
          />
        ),
        cell: ({ row }) => {
          return (
            <div className="data-table-column-reported-by">
              <User size={16} color="#BDBDBD" />
              <span className="data-table-column-reported-by-value">
                {row.getValue("reportedByName")}
              </span>
            </div>
          );
        },
        filterFn: (row: Row<Report>, id, value) => {
          return value.includes(row.original.reportedBy?.fullName);
        },
        enableSorting: false,
      },
      {
        accessorKey: "createdAt",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="REPORTED ON" />
        ),
        cell: ({ row }) => {
          return (
            <div className="data-table-column-reported-on">
              <span className="data-table-column-reported-on-value">
                {dateFormatter.format(new Date(row.getValue("createdAt")))}
              </span>
            </div>
          );
        },
      },
      {
        accessorKey: "status",
        header: ({ column }) => (
          <DataTableColumnHeader column={column} title="STATUS" />
        ),
        cell: ({ row }) => {
          const status = statuses.find(
            (status) => status.value === row.getValue("status"),
          );

          if (!status) {
            return null;
          }

          return (
            <div className="data-table-column-status">
              {status.icon && status.icon}
              <span>{status.label}</span>
            </div>
          );
        },
        filterFn: (row, id, value) => {
          return value.includes(row.getValue("status"));
        },
        enableSorting: false,
      },
      {
        id: "actions",
        cell: ({ row }) => (
          <DataTableRowActions
            row={row}
            setSelectedRow={setSelectedRow}
            setOpen={setOpen}
            setDeleteAlert={setDeleteAlert}
            exportHandler={exportHandler}
            bugListOriginal={bugListOriginal}
            setIsEdit={setIsEdit}
            integrationsDone={integrationsDone}
            setRowActionEdit={setRowActionEdit}
          />
        ),
      },
    ],
    [bugListOriginal],
  );

  return (
    organisationKey && (
      <div className="dashboard-container" ref={parent}>
        <div className="dashboard-content-container">
          {!appVerified ? (
            <div className="dashboard-skeleton-container">
              <EmptySkeleton />
              <NoSDKState />
            </div>
          ) : (
            <div className="dashboard-table-container">
              {showBanner && (
                <SuspenseWrapper>
                  <IntegrationBanner
                    setShowBanner={setShowBanner}
                    showInviteMember={inviteMember}
                    integrationsDone={integrationsDone}
                  />
                </SuspenseWrapper>
              )}
              <div className="dashboard-app-container">
                {organisationKey ? (
                  <Select
                    defaultValue={applicationSelected}
                    value={applicationSelected}
                    onValueChange={(e: string) => {
                      setOpen(false);
                      window.localStorage.setItem("appselected", e);
                      setApplicationSelected(e);
                      getReports(e, 0, pageSize, organisationKey);
                    }}
                  >
                    <SelectTrigger className="dashboard-app-dropdown">
                      <SelectValue
                        placeholder="Application Name"
                        className=""
                      />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {organisationData &&
                          organisationData?.organisationApps.map(
                            (app: App, index: number) => (
                              <SelectItem
                                value={app.appId}
                                className="dashboard-app-dropdown-item"
                                key={index}
                              >
                                <div className="dashboard-app-name">
                                  {app.appName}
                                  <span className="dashboard-package-name">
                                    {app.packageName}
                                  </span>
                                </div>
                              </SelectItem>
                            ),
                          )}
                        <Button
                          variant="ghost"
                          className="dashboard-add-app"
                          onClick={() => {
                            router.push("/add-quash");
                          }}
                        >
                          <Plus size={16} className="text-custom" /> New
                          Application
                        </Button>
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                ) : (
                  <div className="dashboard-app-skeleton-container">
                    <Skeleton className="dashboard-app-skeleton-app" />
                    <Skeleton className="dashboard-app-skeleton-package" />
                  </div>
                )}
              </div>
              <DataTable
                data={bugList}
                columns={columns}
                setOpen={setOpen}
                setSelectedRow={setSelectedRow}
                selectedRow={selectedRow}
                noResults={noResults}
                noReports={noReports}
                setPageIndex={setPageIndex}
                setPageSize={setPageSize}
                totalPageCount={totalPageCount}
                CurrentPageCount={CurrentPageCount}
                dateFilterHandler={dateFilterHandler}
                setSelectedDateFilter={setSelectedDateFilter}
                selectedDateFilter={selectedDateFilter}
                setExportList={setExportList}
                exportHandler={exportHandler}
                integrationsDone={integrationsDone}
                fetchingReports={fetchingReports}
                applicationSelected={applicationSelected}
                pageIndex={pageIndex}
                pageSize={pageSize}
                organisationKey={organisationKey}
                getReports={getReports}
              />
            </div>
          )}
        </div>

        {selectedRow && (
          <SuspenseWrapper>
            <SidePane
              open={open}
              setOpen={setOpen}
              setIsEdit={setIsEdit}
              setSelectedRow={setSelectedRow}
              isEdit={isEdit}
              reportExported={reportExported}
              setReportExported={setReportExported}
              setDeleteAlert={setDeleteAlert}
              savingData={savingData}
              integrationsDone={integrationsDone}
              exportHandler={exportHandler}
              setDiscardChangesAltert={setDiscardChangesAltert}
              dateFormatter={dateFormatter}
              deleteAlert={deleteAlert}
              selectedRow={selectedRow}
              taskDeleteHandler={taskDeleteHandler}
              discardChangesAltert={discardChangesAltert}
              setToastObject={setToastObject}
              setSavingData={setSavingData}
              bugListOriginal={bugListOriginal}
              setBugListOriginal={setBugListOriginal}
              toastDismiss={toastDismiss}
              rowActionEdit={rowActionEdit}
              setRowActionEdit={setRowActionEdit}
              organisationData={organisationData}
            />
          </SuspenseWrapper>
        )}
        <CustomToaster toastObject={toastObject} />
        <MobileScreen />
      </div>
    )
  );
}
