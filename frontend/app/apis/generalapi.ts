import Api from "./api-config";

export interface PatchUserRequestBody {
  fullName: string;
  userOrganisationRole: string;
}

export const PatchUser = async (body: PatchUserRequestBody) => {
  try {
    const result = await Api.patch(`/api/users/update-user`, body);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const GetUsers = async () => {
  try {
    const result = await Api.get(`api/team-members`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};
