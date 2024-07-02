import Api from "./api-config";

export const PostEmails = async (body: { emailList: string[] }) => {
  try {
    const result = await Api.post(`/api/team-members/invite`, body);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const getMembersList = async () => {
  try {
    const result = await Api.get(`/api/dashboard/organisation`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};

export const deleteMember = async (teamMemberId: string) => {
  try {
    const result = await Api.delete(`/api/team-members/${teamMemberId}`);
    return result.data;
  } catch (e) {
    return await Promise.reject(e);
  }
};
