import { GetNetworkLogs } from "@/app/apis/dashboardapis";
import { NetworkLog } from "@/app/types/dashboard-types";
import { ApiError } from "@/app/types/organisation-types";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import CopyButton from "@/components/ui/copy-button";
import JsonViewer from "@/components/ui/json-viewer";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/use-toast";
import React, { useEffect, useState } from "react";

const options: Intl.DateTimeFormatOptions = {
  day: "2-digit",
  month: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
  hour12: false,
};

interface ParsedData {
  [key: string]: string;
}

/**
 * Function component for displaying network logs in a table format.
 * Fetches network logs based on the provided reportId using GetNetworkLogs API.
 * Parses multipart form data and displays request and response details using Accordion, JsonViewer, CopyButton components.
 * Utilizes Skeleton component for loading state.
 * @param {string} reportId - The ID of the report to fetch network logs for.
 * @returns {JSX.Element} - The JSX element representing the network table component.
 */
export function NetworkTable({ reportId }: { reportId: string }): JSX.Element {
  const [networkLogs, setNetworkLogs] = useState([]);
  const [loadingLogs, setloadingLogs] = useState(false);
  const { toast } = useToast();

  function parseMultipartFormData(formData: string): ParsedData {
    const parts = formData.split(/\r\n/);

    const parsedData: ParsedData = {};

    parts.forEach((part: string, index: number) => {
      // Filter out any empty lines
      const nonEmptyLines = part.trim() !== "" ? part.split("\r\n") : [];

      if (nonEmptyLines.length > 0) {
        const entry = nonEmptyLines.join("\r\n");
        const [key, value] = entry.split(": ");

        try {
          const parsedEntry = JSON.parse(entry);

          // If parsing results in an object, merge it into the result object
          Object.assign(parsedData, parsedEntry);
          // parsedData[] = entry;
        } catch (error) {
          // If parsing fails, keep the original entry
          parsedData[index.toString()] = entry;
        }
      }
    });

    return parsedData;
  }

  useEffect(() => {
    setloadingLogs(true);
    async function fetchExternalTextFile() {
      try {
        const { data, success, message } = await GetNetworkLogs(reportId);
        setNetworkLogs(data);
        setloadingLogs(false);
      } catch (error) {
        const apiError = error as ApiError;

        setloadingLogs(false);
        setNetworkLogs([]);
        toast({
          description: apiError?.data?.message
            ? apiError?.data.message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
        console.log(error);
      }
    }
    if (reportId) {
      fetchExternalTextFile();
    }
  }, [reportId]);
  return (
    <div className="network-table">
      <div className="network-table-header">
        <span className="head icon"></span>
        <div className="network-header-grid ">
          <span className="head time ">TIME</span>
          <span className="head path">PATH</span>
          <span className="head response ">RESPONSE</span>
          <span className="head duration ">DURATION</span>
        </div>
      </div>
      <div>
        {!loadingLogs ? (
          Boolean(networkLogs?.length) &&
          networkLogs.map((log: NetworkLog) => (
            <Accordion type="single" collapsible>
              <AccordionItem value={log.requestUrl} className="w-full">
                <AccordionTrigger className="network-table-accordian-trigger">
                  <div className="network-table-accordian grid grid-cols-12">
                    <span className="cell time ">
                      {log?.timeStamp
                        ? new Date(log.timeStamp).toLocaleString(
                            "en-GB",
                            options,
                          )
                        : "--"}
                    </span>
                    <span className="cell path">
                      {log?.requestMethod}/{log?.requestUrl}
                    </span>
                    <span
                      className={`cell response  ${
                        log?.responseCode === 200 ? "success" : "error"
                      }`}
                    >
                      {log?.responseCode}
                    </span>
                    <span className="cell duration">
                      {log?.durationMs ? log?.durationMs + "ms" : "--"}
                    </span>
                  </div>
                </AccordionTrigger>
                <AccordionContent className="network-table-accordian-content">
                  <div className="network-table-vertical-line-container">
                    <div className="network-table-vertical-line" />
                  </div>
                  <div className="network-table-accordian-content-container">
                    <div className="network-table-logs-container">
                      <span className="network-logs-title">Request</span>
                      <div className="network-logs-container">
                        <div className="network-table-logs">
                          {(!log?.requestHeaders ||
                            Object.keys(log.requestHeaders).length === 0) &&
                          (!log?.requestBody ||
                            Object.keys(log.requestBody).length === 0) ? (
                            <div className="logs-empty-text">
                              Request body is empty
                            </div>
                          ) : (
                            <>
                              <JsonViewer
                                data={{
                                  ...log.requestHeaders,
                                  requestBody:
                                    typeof log.requestBody === "string"
                                      ? log.requestBody.includes(
                                          "Content-Disposition: form-data",
                                        )
                                        ? parseMultipartFormData(
                                            log.requestBody,
                                          )
                                        : JSON.parse(log.requestBody)
                                      : log.requestBody,
                                }}
                              />

                              <CopyButton
                                className="network-logs-copy"
                                data={{
                                  ...log.requestHeaders,
                                  requestBody:
                                    typeof log.requestBody === "string"
                                      ? log.requestBody.includes(
                                          "Content-Disposition: form-data",
                                        )
                                        ? parseMultipartFormData(
                                            log.requestBody,
                                          )
                                        : JSON.parse(log.requestBody)
                                      : log.requestBody,
                                }}
                              />
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="network-table-logs-container">
                      <span className="network-logs-title">Response</span>
                      <div className="network-logs-container">
                        <div className="network-table-logs">
                          {(!log?.responseHeaders ||
                            Object.keys(log.responseHeaders).length === 0) &&
                          (!log?.responseBody ||
                            Object.keys(log.responseBody).length === 0) ? (
                            <div className="logs-empty-text">
                              Response body is empty
                            </div>
                          ) : (
                            <>
                              <JsonViewer
                                data={{
                                  ...log.responseHeaders,
                                  responseBody:
                                    typeof log.responseBody === "string"
                                      ? log.responseBody.includes(
                                          "Content-Disposition: form-data",
                                        )
                                        ? parseMultipartFormData(
                                            log.responseBody,
                                          )
                                        : JSON.parse(log.responseBody)
                                      : log.responseBody,
                                }}
                              />

                              <CopyButton
                                className="network-logs-copy"
                                data={{
                                  ...log.responseHeaders,
                                  responseBody:
                                    typeof log.responseBody === "string"
                                      ? log.responseBody.includes(
                                          "Content-Disposition: form-data",
                                        )
                                        ? parseMultipartFormData(
                                            log.responseBody,
                                          )
                                        : JSON.parse(log.responseBody)
                                      : log.responseBody,
                                }}
                              />
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          ))
        ) : (
          <Skeleton className="network-table-skeleton h-8 w-full mt-4" />
        )}
      </div>
    </div>
  );
}
