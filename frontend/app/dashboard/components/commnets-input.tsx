import { PostComment } from "@/app/apis/dashboardapis";
import Image from "next/image";
import React, {
  RefObject,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";
import { MentionsInput, Mention } from "react-mentions";
import { Paperclip, Smiley } from "@/app/lib/icons";
import { useDropzone } from "react-dropzone";
import { useToast } from "@/components/ui/use-toast";
import MediaArrayRenderer from "./media-array-rerenderer";
import { useSession } from "next-auth/react";
import EmojiPicker, { EmojiClickData } from "emoji-picker-react";
import {
  MemberMention,
  Thread,
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";
import { ApiError, Organisation } from "@/app/types/organisation-types";
import { Member } from "@/app/types/member-types";

/**
 * Component for handling user input to post comments with mentions, media uploads, and emojis.
 * Manages state for chat input, mentions, media array, current member, and emoji picker visibility.
 * Utilizes various hooks like useState, useEffect, useRef, useCallback, useDropzone, and custom useToast.
 * Integrates with external APIs for posting comments and fetching organization members.
 */

const CommnetsInput = ({
  threadList,
  setThreadList,
  reportId,
  organisationData,
}: {
  threadList: Thread[];
  setThreadList: React.Dispatch<React.SetStateAction<any[]>>;
  reportId: string;
  organisationData: Organisation;
}) => {
  const [commentInputFocused, setCommentInputFocused] = useState(false);
  const [membersSuggestion, setMembersSuggestion] = useState<MemberMention[]>(
    [],
  );
  const [chatInput, setChatInput] = useState("");
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [mentions, setMentions] = useState<string[]>([]);
  const [mediaArray, setMediaArray] = useState<ThreadAttachmentLocal[]>([]);
  const [currentMember, setCurrentMember] = useState<Member | null>(null);
  const { data: session } = useSession();
  const emojiRef: React.RefObject<HTMLDivElement> =
    useRef<HTMLDivElement>(null);

  useEffect(() => {
    let temp = [];
    const getMembers = () => {
      try {
        const membersList = organisationData?.orgMembers;
        const currentUserEmail = session?.user?.email;
        const currentMember = membersList.find((member: Member) => {
          return member?.email === currentUserEmail;
        });
        if (currentMember) setCurrentMember(currentMember);
        temp = membersList
          .filter((user: Member) => user.name)
          .map(({ id, name, email }) => ({ display: name, id, email }));
        setMembersSuggestion(temp.slice(0));
      } catch (error) {
        console.log(error);
      }
    };
    getMembers();
  }, []);

  const { toast } = useToast();

  const { getRootProps, getInputProps } = useDropzone({
    accept: {
      "image/jpeg": [],
      "image/png": [],
      "image/gif": [],
      "video/mp4": [],
      "video/avi": [],
      "application/pdf": [],
      "audio/mpeg": [],
      "text/plain": [],
    },
    onDrop: (acceptedFiles) => {
      const mediaFile = acceptedFiles[0];
      handleImageUpload(mediaFile);
    },
  });

  const handleImageUpload = useCallback(
    (imageFile: ThreadAttachmentLocal) => {
      setMediaArray((prevMediaArray: ThreadAttachmentLocal[]) => [
        ...prevMediaArray,
        imageFile,
      ]);
    },
    [setMediaArray],
  );

  const handleChange = (input: string) => {
    setChatInput(input);

    // Allowing for characters like + and . in the email address
    const regex = /@\[[^)]+\]\((\w+[\w\d\.\+\-]+)\)/g;

    let matches;
    const userIDs = [];

    while ((matches = regex.exec(input)) !== null) {
      userIDs.push(matches[1]);
    }

    setMentions(userIDs.slice(0));
  };

  const onEmojiClick = (emojiObject: EmojiClickData) => {
    setChatInput((previousInput) => previousInput + emojiObject.emoji);
    setShowEmojiPicker(false);
  };

  onClickOutside(emojiRef, () => {
    setShowEmojiPicker(!showEmojiPicker);
  });

  function onClickOutside(
    ref: RefObject<HTMLDivElement>,
    handler: (event: MouseEvent | TouchEvent) => void,
  ) {
    useEffect(() => {
      const listener = (event: MouseEvent | TouchEvent) => {
        if (!ref.current || ref.current.contains(event.target as Node)) {
          return;
        }

        handler(event);
      };

      document.addEventListener("mousedown", listener);
      document.addEventListener("touchstart", listener);

      return () => {
        document.removeEventListener("mousedown", listener);
        document.removeEventListener("touchstart", listener);
      };
    }, [ref, handler]);
  }

  const postThread = async () => {
    const previousList = threadList.slice(0);
    const tempMediaArray = mediaArray.slice(0);
    const tempInput = chatInput;

    // Format the chatInput to add a space after each mention
    const formattedMessages = chatInput.replace(/@\[[^\]]+\]\([^)]+\)/g, "$& ");

    setThreadList((previousList: Thread[]) => [
      ...previousList,
      {
        id: "",
        posterId: currentMember?.id,
        messages: formattedMessages, // Use the formatted messages
        mentions: mentions,
        posterName: currentMember?.name,
        timeStamp: new Date(),
        uploads: mediaArray.length > 0 ? mediaArray : [],
      },
    ]);

    // Clear chat input and media array
    setChatInput("");
    setMediaArray([]);

    const formData = new FormData();
    formData.append("reportId", reportId);
    if (currentMember) {
      formData.append("posterId", currentMember?.id);
    }
    formData.append("messages", formattedMessages); // Use the formatted messages
    if (mentions?.length > 0) {
      mentions.forEach((mention: string) => {
        formData.append("mentions", mention);
      });
    }

    if (mediaArray?.length > 0) {
      mediaArray.forEach((media: ThreadAttachmentLocal) => {
        formData.append("attachments", media);
      });
    }

    try {
      const { data, success, message } = await PostComment(formData);
      if (success) {
        setThreadList(() => [
          ...previousList,
          {
            id: data?.id,
            posterId: data?.posterId,
            messages: data?.messages,
            mentions: data?.mentions ? data?.mentions?.slice(0) : mentions,
            posterName: currentMember?.name,
            timeStamp: data?.timeStamp,
            uploads: data?.uploads
              ? data?.uploads?.slice(0)
              : mediaArray.length > 0
              ? mediaArray
              : [],
          },
        ]);

        toast({
          description: message,
          typeof: "success",
        });
      } else {
        setThreadList([...previousList]);
        setChatInput(tempInput);
        setMediaArray(tempMediaArray);

        toast({
          description: message,
          typeof: "error",
        });
      }
    } catch (error) {
      const apiError = error as ApiError;

      setThreadList([...previousList]);
      setChatInput(tempInput);
      setMediaArray(tempMediaArray);
      toast({
        description: apiError?.data?.message
          ? apiError?.data.message
          : "Something went wrong. Please try again.",
        typeof: "error",
      });
      console.log(error);
    }
  };

  const handleMediaDelete = (img: ThreadAttachmentLocal, index: number) => {
    let temp = [];

    temp = mediaArray.filter(
      (media: ThreadAttachmentLocal, inx: number) => inx !== index,
    );

    setMediaArray(temp.slice(0));
  };

  return (
    <>
      <div className={`comments-chat-container `}>
        <div
          className={`comments-chat-input-conatiner ${
            commentInputFocused ? "active" : "non-active"
          }`}
        >
          <div className="comments-chat-input-wrapper relative">
            <MentionsInput
              onFocus={() => {
                setCommentInputFocused(true);
              }}
              onBlur={() => {
                setCommentInputFocused(false);
              }}
              singleLine
              value={chatInput}
              onChange={(e) => handleChange(e.target.value)}
              placeholder="Post a comment..."
              className="mentions placeholder:text-[#747474] dark:placeholder-[#dfdfdf]"
              style={{
                border: "0px !important",
                width: "100%",
                height: "100%",
                outline: 0,
                color: "black",
              }}
              // Add 'any' type to bypass TypeScript error
              // @ts-ignore
              markup="@{{__type__||__id__||__display__}}"
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  postThread();
                }
              }}
              allowSuggestionsAboveCursor={true}
              forceSuggestionsAboveCursor={true}
            >
              <Mention
                displayTransform={(id, display) => `@${display} `}
                trigger="@"
                data={membersSuggestion}
                className="mentions__mention"
                // @ts-ignore
                type="user"
              />
            </MentionsInput>

            <MediaArrayRenderer
              mediaArray={mediaArray}
              handleMediaDelete={handleMediaDelete}
            />
            <div className="comments-action-conatiner">
              <div className="comments-action-upload">
                <button
                  className="comments-action-upload-button"
                  {...getRootProps()}
                >
                  <Paperclip size={16} className="action-icon" />
                  <input {...getInputProps()} />
                </button>
                <Smiley
                  size={16}
                  className="action-icon"
                  onClick={() => {
                    setShowEmojiPicker(!showEmojiPicker);
                  }}
                />
              </div>
              <button
                className="comments-action-send disabled:opacity-50"
                disabled={!chatInput}
                onClick={postThread}
              >
                <Image
                  src={"/icons/paper-plane-right-active.svg"}
                  alt="post-comment"
                  height={16}
                  width={16}
                  className="action-icon dark:hidden"
                />
                <Image
                  src={"/icons/paper-plane-right-active-dark.svg"}
                  alt="post-comment"
                  height={16}
                  width={16}
                  className="action-icon dark:flex hidden"
                />
              </button>
            </div>
          </div>
        </div>
      </div>
      {showEmojiPicker && (
        <div ref={emojiRef} className="absolute top-16 ">
          <EmojiPicker
            skinTonesDisabled={true}
            className="emoji-picker"
            onEmojiClick={onEmojiClick}
          />
        </div>
      )}
    </>
  );
};

export default CommnetsInput;
