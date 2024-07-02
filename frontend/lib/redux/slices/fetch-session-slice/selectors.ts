/* Instruments */
import type { ReduxState } from "@/lib/redux";

// Selector to fetch ticket solution data for a specific ticket ID
export const selectGeneratedGif = (
  state: ReduxState,
  ticketId: string,
): any => {
  return state.generateGif.data[ticketId];
};
