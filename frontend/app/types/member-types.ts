export interface Member {
  id: string;
  name: string;
  email: string;
  teamMemberId: string;
  hasAcceptedInvite: boolean;
  admin: boolean;
}
