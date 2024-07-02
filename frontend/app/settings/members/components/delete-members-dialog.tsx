import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { User, Envelope } from "../../../lib/icons";
import { Button } from "@/components/ui/button";
import SpinLoader from "@/components/ui/spinner";
import { useToast } from "@/components/ui/use-toast";
import { deleteMember } from "@/app/apis/membersapi";
import { Member } from "@/app/types/member-types";

/**
 * Function component for a dialog to delete a member.
 *
 * @param {Object} props - The props object containing the following properties:
 * @param {boolean} deleteOpen - Flag to control the visibility of the delete dialog.
 * @param {function} setDeleteOpen - Function to toggle the delete dialog visibility.
 * @param {Object} activeMember - The active member object to be deleted.
 * @param {Array} members - The array of members.
 * @param {function} setMembers - Function to update the members array after deletion.
 *
 * @returns {JSX.Element} A dialog component for deleting a member with options to confirm or cancel.
 */

interface DeleteMembersDialogProps {
  members: Member[];
  setMembers: React.Dispatch<React.SetStateAction<Member[]>>;
  deleteOpen: boolean;
  setDeleteOpen: React.Dispatch<React.SetStateAction<boolean>>;
  activeMember: Member | null;
}

const DeleteMembersDialog = ({
  deleteOpen,
  setDeleteOpen,
  activeMember,
  members,
  setMembers,
}: DeleteMembersDialogProps) => {
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const handleDelete = async (teamMemberId: string) => {
    setLoading(true);
    let temp = members.filter((member: Member) => {
      return member.teamMemberId !== teamMemberId;
    });
    try {
      await deleteMember(teamMemberId);
      setMembers(temp);
      toast({
        description: "Member removed from the team",
      });
    } catch (error) {
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
      setDeleteOpen(false);
    }
  };
  return (
    <Dialog
      open={deleteOpen}
      onOpenChange={() => {
        setDeleteOpen(!deleteOpen);
      }}
    >
      <div className="members-remove-dialog">
        <DialogContent className="members-remove-dialog-content">
          <DialogHeader>
            <DialogTitle className="title text-xl">
              Remove selection ?
            </DialogTitle>
            <DialogDescription role="none" className="description">
              Are you sure you want to remove this user?
              <div className="remove-user-info">
                <User />
                {activeMember?.name === null ? "User" : activeMember?.name}
              </div>
              <div className="remove-user-info">
                <Envelope />
                {activeMember?.email}
              </div>
            </DialogDescription>
          </DialogHeader>

          <DialogFooter className="members-remove-dialog-footer">
            <Button
              type="button"
              className="members-cancel"
              onClick={() => {
                setDeleteOpen(false);
              }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={loading}
              onClick={() => {
                if (activeMember) handleDelete(activeMember.teamMemberId);
              }}
              className="remove"
            >
              {loading ? <SpinLoader /> : "Remove"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </div>
    </Dialog>
  );
};

export default DeleteMembersDialog;
