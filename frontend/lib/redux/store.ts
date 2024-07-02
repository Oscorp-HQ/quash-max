/* Core */
import {
  configureStore,
  type ThunkAction,
  type Action,
} from "@reduxjs/toolkit";
import {
  useSelector as useReduxSelector,
  useDispatch as useReduxDispatch,
  type TypedUseSelectorHook,
} from "react-redux";

/* Instruments */
import { reducer } from "./root-reducer";
import { middleware } from "./middleware";

/**
 * Defines a Redux store with configured reducers, middleware, and hooks for useDispatch and useSelector.
 *
 * @remarks
 * - The Redux store is created using configureStore from "@reduxjs/toolkit".
 * - The reducers are imported from "./rootReducer".
 * - The middleware is imported from "./middleware" and added to the store.
 * - Hooks for useDispatch and useSelector are provided for interacting with the store.
 * - Type definitions for ReduxStore, ReduxState, ReduxDispatch, and ReduxThunkAction are exported.
 */

export const reduxStore = configureStore({
  reducer,
  middleware: (getDefaultMiddleware) => {
    return getDefaultMiddleware().concat(middleware);
  },
});
export const useDispatch = () => useReduxDispatch<ReduxDispatch>();
export const useSelector: TypedUseSelectorHook<ReduxState> = useReduxSelector;

/* Types */
export type ReduxStore = typeof reduxStore;
export type ReduxState = ReturnType<typeof reduxStore.getState>;
export type ReduxDispatch = typeof reduxStore.dispatch;
export type ReduxThunkAction<ReturnType = void> = ThunkAction<
  ReturnType,
  ReduxState,
  unknown,
  Action
>;
