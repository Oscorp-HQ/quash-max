import { createAppAsyncThunk } from "@/lib/redux/create-app-async-thunk";
import { GenerateGifByReportId } from "@/app/apis/dashboardapis";
import { ApiError } from "@/app/types/organisation-types";

export const fetchGifSessionAsync = createAppAsyncThunk(
  "generateGif/fetchGifSessionAsync",
  async (reportId: string, thunkAPI) => {
    try {
      const response = await GenerateGifByReportId(reportId);
      if (response.success) {
        return { ...response, reportId: reportId };
      } else {
        // If response.success is not true, throw an error
        throw new Error(response.message || "Failed to fetch gif session");
      }
    } catch (error) {
      const apiError = error as ApiError;
      // Throw the error to reject the thunk action
      throw new Error(apiError?.message ?? "Failed to fetch gif session");
    }
  },
);
