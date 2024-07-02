export interface LineNumberProps {
  number: number;
}

export interface JsonLineProps {
  number: number | null;
  isCollapsible: boolean;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  children?: React.ReactNode;
  displayIcon: boolean;
}

export interface JsonViewerProps {
  data: Record<string, any>;
}
