import { BugBeetle, FileX, MagicWand } from "@/app/lib/icons";
import Image from "next/image";

export const types = [
  {
    value: "BUG",
    label: "Bug",
    icon: <BugBeetle size={16} />,
  },
  {
    value: "UI",
    label: "UI",
    icon: <MagicWand size={16} />,
  },
  {
    value: "CRASH",
    label: "Crash",
    icon: <FileX size={16} />,
  },
];

export const priorities = [
  {
    value: "HIGH",
    label: "High",
    icon: <Image src="/priority-high.svg" alt="high" width={13} height={12} />,
  },
  {
    value: "MEDIUM",
    label: "Medium",
    icon: (
      <Image src="/priority-medium.svg" alt="medium" width={13} height={12} />
    ),
  },
  {
    value: "LOW",
    label: "Low",
    icon: <Image src="/priority-low.svg" alt="low" width={13} height={12} />,
  },
  {
    value: "NOT_DEFINED",
    label: "Not Defined",
    icon: (
      <Image
        src="/priority-not-defined.svg"
        alt="not-defined"
        width={13}
        height={12}
      />
    ),
  },
];

export const exportValues = [
  {
    value: false,
    label: "Not Exported",
  },
  {
    value: true,
    label: "Exported",
  },
];

export const statuses = [
  {
    value: "OPEN",
    label: "Open",
    icon: <div className="rounded-full h-2 w-2 bg-[#DD6054]" />,
  },
  {
    value: "IN PROGRESS",
    label: "In Progress",
    icon: <div className="rounded-full h-2 w-2 bg-[#62A1F0]" />,
  },
  {
    value: "CLOSED",
    label: "Closed",
    icon: <div className="rounded-full h-2 w-2 bg-green-400" />,
  },
];
