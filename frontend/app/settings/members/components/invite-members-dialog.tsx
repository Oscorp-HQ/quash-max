import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { z } from "zod";
import { useRouter } from "next/navigation";
import { PostEmails } from "@/app/apis/membersapi";
import { useToast } from "@/components/ui/use-toast";
import { PaperPlaneRight, X } from "../../../lib/icons";
import { Button } from "@/components/ui/button";
import SpinLoader from "@/components/ui/spinner";
import { Member } from "@/app/types/member-types";
import { ApiError, Organisation } from "@/app/types/organisation-types";

// ZOD email validation
const emailSchema = z.string().email({ message: "Invalid email address" });

/**
 * Function component for displaying a dialog to invite members to collaborate.
 *
 * @param {Object} props - The props object containing the following properties:
 *   - members: Array of current members.
 *   - setMembers: Function to update the members array.
 *   - inviteOpen: Boolean to control the visibility of the invite dialog.
 *   - setInviteOpen: Function to toggle the invite dialog visibility.
 *   - organisationData: Data related to the organization.
 *
 * @returns {JSX.Element} A dialog component for inviting members with email input and invite button.
 */

interface InviteMembersDialogProps {
  members: Member[];
  setMembers: React.Dispatch<React.SetStateAction<Member[]>>;
  inviteOpen: boolean;
  setInviteOpen: React.Dispatch<React.SetStateAction<boolean>>;
  organisationData: Organisation | null;
}

const InviteMembersDialog = ({
  members,
  setMembers,
  inviteOpen,
  setInviteOpen,
  organisationData,
}: InviteMembersDialogProps) => {
  const [enteredEmails, setEnteredEmails] = useState<string[]>([]);
  const [email, setEmail] = useState("");
  const [error, setError] = useState({ error: false, errMsg: "" });
  const [showQuashProBanner, setShowQuashProBanner] = useState(false);
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const router = useRouter();

  const handleDeleteEmail = (inputEmail: string) => {
    let temp = enteredEmails.filter((email: string) => {
      return email !== inputEmail;
    });
    setEnteredEmails(temp);
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
  };

  function enterKeyPressed() {
    try {
      emailSchema.parse(email);
      if (!enteredEmails.includes(email.toLowerCase())) {
        setEnteredEmails([...enteredEmails, email]);
        setEmail("");
      } else {
        setError({
          error: true,
          errMsg: "Email already entered",
        });
      }
    } catch (error) {
      setError({
        error: true,
        errMsg: "Invalid Email Address",
      });
    }
  }

  const inviteMembersHandler = () => {
    inviteMembers(enteredEmails);
  };
  const inviteOpenChangeHandler = () => {
    setInviteOpen(!inviteOpen);
    setEnteredEmails([]);
    setError({
      error: false,
      errMsg: "",
    });
  };

  //Sending members invite via email
  const inviteMembers = async (emails: string[]) => {
    setLoading(true);
    const body = {
      emailList: emails,
    };
    try {
      const res = await PostEmails(body);
      if (res.success) {
        const listEmails = [...members, ...res.data];
        setMembers(listEmails);
        setInviteOpen(false);
        setEnteredEmails([]);
        setError({
          error: false,
          errMsg: "",
        });
        toast({
          description: res?.message,
        });
      } else {
        setError({
          error: true,
          errMsg: res?.message,
        });
      }
      setLoading(false);
    } catch (error) {
      const apiError = error as ApiError;
      setError({
        error: true,
        errMsg:
          apiError?.response?.data?.message ??
          "Something went wrong. Please try again.",
      });
      console.log(error);
      setLoading(false);
    }
  };

  return (
    <Dialog open={inviteOpen} onOpenChange={inviteOpenChangeHandler}>
      <div>
        <DialogContent className="members-dialog-content">
          <DialogHeader>
            <DialogTitle>Invite people to Quash</DialogTitle>
            <DialogDescription>
              Invite your teammates through their work emails to collaborate
              with you
            </DialogDescription>
          </DialogHeader>

          <div className="invite-emails-container">
            {enteredEmails.length > 0 &&
              enteredEmails.map((email, index) => (
                <div key={index} className="invite-email">
                  <div className="email-text">
                    {email}
                    <X
                      className="icon"
                      onClick={() => handleDeleteEmail(email)}
                    />
                  </div>
                </div>
              ))}
            <input
              placeholder="Enter email address and press enter"
              value={email}
              onChange={(e) => handleEmailChange(e)}
              onKeyUp={(e) => {
                if (e.key === "Enter") {
                  setError({
                    error: false,
                    errMsg: "",
                  });
                  enterKeyPressed();
                }
              }}
            ></input>
          </div>

          {error && error?.error ? (
            <p className="invite-members-error-text">{error?.errMsg}</p>
          ) : null}

          <DialogFooter>
            <div className="members-dialog-footer">
              <p className="invite-info">
                We’ll send them an invite link to join your team
              </p>
              <Button
                disabled={!(enteredEmails.length > 0) || loading}
                type="submit"
                className="invite-button"
                onClick={inviteMembersHandler}
              >
                {loading ? (
                  <SpinLoader />
                ) : (
                  <>
                    <PaperPlaneRight />
                    Invite
                  </>
                )}
              </Button>
            </div>
          </DialogFooter>
        </DialogContent>
      </div>
    </Dialog>
  );
};

export default InviteMembersDialog;
