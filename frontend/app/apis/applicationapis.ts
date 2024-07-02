import Api from "./api-config";

export const deleteApp = async (appId: string) => {
  try {
    const result = await Api.delete(`/api/app/${appId}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};
