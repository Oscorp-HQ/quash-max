/* Core */
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { fetchGifSessionAsync } from "./thunks";

const initialState: GifSessionSliceState = {
  data: {}, // Use a unique identifier (e.g., ticket ID) as the key
};

export const fetchGenerateGifSlice = createSlice({
  name: "generateGif",
  initialState,
  reducers: {
    updateGenerateGifState: (state, action: PayloadAction<any>) => {
      const { id } = action.payload;

      state.data[id] = {
        ...state.data[id],
        gifData: {},
        loading: "pending",
      };
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchGifSessionAsync.pending, (state, action: any) => {
        const ticketId = action.meta.arg;
        console.log("consoling in pending async case", action);

        // Set loading state for the specific ticket to pending when fetching
        state.data[ticketId] = {
          ...state.data[ticketId],
          loading: "pending",
        };
      })
      .addCase(fetchGifSessionAsync.fulfilled, (state, action) => {
        const { reportId } = action.payload;

        console.log("consoling in fulfilled async case", action);

        // Set loading state for the specific ticket to success when fetching is completed

        state.data[reportId] = {
          gifData: action.payload,
          loading: "success",
        };
      })
      .addCase(fetchGifSessionAsync.rejected, (state, action: any) => {
        const ticketId = action.meta.arg;

        // Set loading state for the specific ticket to failed when fetching is rejected
        state.data[ticketId] = {
          gifData: {},
          loading: "failed",
        };
      });
  },
});

/* Types */
export interface GifSessionSliceState {
  data: { [key: string]: GenerateGifLoadingState }; // Use a unique identifier as the key (e.g., ticket ID)
}

interface GenerateGifLoadingState {
  loading: "idle" | "pending" | "success" | "failed";
  gifData?: any; // Replace 'any' with the actual type of your solution data
  // Other ticket data fields
}
