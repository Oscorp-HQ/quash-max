/**
 * Exporting reducers for generated gifs.
 */
import { fetchGenerateGifSlice } from "./slices";

export const reducer = {
  generateGif: fetchGenerateGifSlice.reducer,
};
