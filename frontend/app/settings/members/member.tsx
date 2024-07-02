"use client";
import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Plus, User, Envelope, Trash } from "../../lib/icons";
import { signOut, useSession } from "next-auth/react";
import SuspenseWrapper from "@/app/dashboard/components/suspense-wrapper";
import {
  Organisation,
  OrganisationApiResponse,
} from "@/app/types/organisation-types";
import { Member } from "@/app/types/member-types";
const InviteMembersDialog = React.lazy(
  () => import("./components/invite-members-dialog"),
);
const DeleteMembersDialog = React.lazy(
  () => import("./components/delete-members-dialog"),
);

/**
 * Function component for managing members within an organization.
 *
 * @param {Object} orgData - Data related to the organization.
 * @returns {JSX.Element} A JSX element representing the members management interface.
 */

export default function Members({
  orgData,
}: {
  orgData: OrganisationApiResponse;
}) {
  const { data: session } = useSession();
  const searchParams = useSearchParams();
  const [members, setMembers] = useState<Member[]>([]);
  const [activeMember, setActiveMember] = useState<Member | null>(null);
  const [deleteOpen, setDeleteOpen] = useState<boolean>(false);
  const [inviteOpen, setInviteOpen] = useState<boolean>(false);
  const [shouldShowAdminView, setShouldShowAdminView] = useState(false);
  const [organisationData, setOrganisationData] = useState<Organisation | null>(
    null,
  );

  useEffect(() => {
    getOrg();
  }, []);

  const getOrg = async () => {
    try {
      const { data, success } = await orgData;
      if (success) {
        setOrganisationData(data);
      }
    } catch (error) {
      console.log(error);

      handleSignOut();
    }
  };

  useEffect(() => {
    const getMembers = async () => {
      try {
        const members = await orgData?.data.orgMembers;
        const currentUserEmail = session?.user?.email;
        const currentMember = members.find((member: Member) => {
          return member?.email === currentUserEmail;
        });
        if (currentMember && currentMember.admin === true) {
          setShouldShowAdminView(true);
        }
        setMembers(members);
        return members;
      } catch (error) {
        console.log(error);
      }
    };
    getMembers();
  }, [session?.data?.token]);

  const handleSignOut = async () => {
    await signOut();
    window.localStorage.removeItem("appselected");
  };

  return (
    <div className="members">
      <div className="head">
        <h1 className="title "> Members</h1>
        <p className="sub-title">View and add members to collaborate </p>
      </div>

      <div className="add-members-dialog">
        <Button
          className="add-members"
          onClick={() => {
            setInviteOpen(true);
          }}
        >
          <Plus />
          Add members
        </Button>
      </div>

      <div className="members-container">
        {members.map((user: Member) => (
          <div key={user.id} className="member-info">
            <div className="member-name-conatiner ">
              <User className="icon" />
              <div className="member-name">
                {user.name === null ? "User" : user.name}
              </div>
            </div>
            <div className="member-email-container">
              <Envelope className="icon" />
              <div className="member-email">{user.email}</div>
            </div>
            {shouldShowAdminView && (
              <div
                className={
                  user.admin === true ? "members-remove " : "error-text"
                }
              >
                <button
                  className="members-remove-button"
                  disabled={user.admin === true ? true : false}
                  onClick={() => {
                    setActiveMember(user);
                    setDeleteOpen(true);
                  }}
                >
                  <Trash />
                  Remove
                </button>
              </div>
            )}
          </div>
        ))}
      </div>

      {inviteOpen && (
        <SuspenseWrapper>
          <InviteMembersDialog
            members={members}
            setMembers={setMembers}
            inviteOpen={inviteOpen}
            setInviteOpen={setInviteOpen}
            organisationData={organisationData}
          />
        </SuspenseWrapper>
      )}
      {deleteOpen && (
        <SuspenseWrapper>
          <DeleteMembersDialog
            members={members}
            setMembers={setMembers}
            deleteOpen={deleteOpen}
            setDeleteOpen={setDeleteOpen}
            activeMember={activeMember}
          />
        </SuspenseWrapper>
      )}
    </div>
  );
}
