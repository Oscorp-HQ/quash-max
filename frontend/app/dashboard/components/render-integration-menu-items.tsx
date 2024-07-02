import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import React from "react";

/**
 * Renders a list of integration menu items based on the provided integrationsDone array.
 * Only integrations that are marked as 'done' will be displayed.
 *
 * @param {Object} props - The props object containing handleExport function and integrationsDone array.
 * @param {Function} props.handleExport - The function to handle the export action when an integration is clicked.
 * @param {Array} props.integrationsDone - The array containing the keys of integrations that are marked as 'done'.
 *
 * @returns {Array} An array of DropdownMenuItem components for each 'done' integration, with a label and onClick handler.
 */

const RenderIntegrationMenuItems = ({
  handleExport,
  integrationsDone,
}: {
  handleExport: (integration: string) => void;
  integrationsDone: string[];
}) => {
  const integrations = [
    { key: "jira", label: "Jira", done: integrationsDone.includes("jira") },
    {
      key: "linear",
      label: "Linear",
      done: integrationsDone.includes("linear"),
    },
    {
      key: "github",
      label: "Github",
      done: integrationsDone.includes("github"),
    },
    {
      key: "slack",
      label: "Slack",
      done: integrationsDone.includes("slack"),
    },
  ];

  return integrations
    .filter((integration) => integration.done)
    .map((integration) => (
      <DropdownMenuItem
        key={integration.key}
        onClick={() => handleExport(integration.key)}
      >
        <span>{integration.label}</span>
      </DropdownMenuItem>
    ));
};

export default RenderIntegrationMenuItems;
