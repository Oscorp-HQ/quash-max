import { Spinner, Trash } from "@/app/lib/icons";
import React, { RefObject, useEffect, useMemo, useRef, useState } from "react";
import { DeleteComment, GetComments } from "@/app/apis/dashboardapis";
import { useToast } from "@/components/ui/use-toast";
import { useCustomToast } from "./custom-use-toast";
import ImageComponent from "./image-component";
import VideoComponent from "./video-component";
import PdfComponent from "./pdf-component";
import AudioComponent from "./audio-component";
import CrashComponent from "./crash-component";
import CommnetsInput from "./commnets-input";
import DeleteThreadDialog from "./delete-thread-dialog";
import SuspenseWrapper from "./suspense-wrapper";
import { ApiError, Organisation } from "@/app/types/organisation-types";
import { ToastObject } from "@/app/types/dashboard-types";
import {
  Thread,
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";
import { Member } from "@/app/types/member-types";

/**
 * Component for displaying comments related to a specific report.
 *
 * @param {string} reportId - The ID of the report for which the comments are being displayed.
 * @param {object} organisationData - Data related to the organization.
 * @param {function} setToastObject - Function to set toast object for displaying messages.
 * @param {function} toastDismiss - Function to dismiss the toast message.
 *
 * @returns {JSX.Element} A React component that renders the comments section with thread discussions, attachments, and input for posting comments.
 */

const Comments = ({
  reportId,
  organisationData,
  setToastObject,
  toastDismiss,
}: {
  reportId: string;
  organisationData: Organisation;
  setToastObject: React.Dispatch<React.SetStateAction<ToastObject>>;
  toastDismiss: () => void;
}): JSX.Element => {
  const [threadList, setThreadList] = useState<Thread[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteThreadAlert, setDeleteThreadAlert] = useState(false);
  const { toastCustom, dismiss } = useCustomToast();
  const [threadSelectedToDelete, setThreadSelectedToDelete] = useState("");
  const { toast } = useToast();

  useEffect(() => {
    setThreadList([]);
    setLoading(true);
    async function fetchThread() {
      try {
        const { data, success, message } = await GetComments(reportId);
        if (success) {
          let temp: Thread[] = [];
          data.forEach((thread: Thread) => {
            const user: Member | undefined = organisationData?.orgMembers.find(
              (member: Member) => member?.id === thread?.posterId,
            );

            if (user) temp.push({ ...thread, posterName: user?.name });
          });
          setThreadList(temp.slice(0));
        } else {
        }
      } catch (error) {
        const apiError = error as ApiError;
        if (!apiError?.data?.message) {
          toast({
            description: apiError?.data?.message
              ? apiError?.data?.message
              : "Something went wrong. Please try again.",
            typeof: "error",
          });
        }

        console.log(error);
      } finally {
        setLoading(false);
      }
    }
    if (reportId) {
      fetchThread();
    }
  }, [reportId]);

  const threadListRef: RefObject<HTMLDivElement> = useRef(null);

  useEffect(() => {
    if (threadListRef.current) {
      threadListRef.current.scrollTop = threadListRef.current.scrollHeight;
    }
  }, [threadList]);

  function formatComments(inputValue: string) {
    const mentionRegex = /@\[([^\]]+)\]\(([^)]+)\)/g;

    const elements = [];
    let lastIndex = 0;

    let match;
    while ((match = mentionRegex.exec(inputValue)) !== null) {
      const [fullMatch, display, userId] = match;

      // Add regular text before mention
      if (match.index > lastIndex) {
        elements.push(inputValue.substring(lastIndex, match.index));
      }

      // Add mention as a React element
      elements.push(
        <span
          key={Math.random()}
          className="mention text-[#3762bb] dark:text-[#62a1f0]"
          data-user-id={userId}
        >
          @{display}{" "}
        </span>,
      );

      lastIndex = mentionRegex.lastIndex;
    }

    // Add remaining regular text after the last mention
    if (lastIndex < inputValue?.length) {
      elements.push(inputValue.substring(lastIndex));
    }

    return elements;
  }

  const threadDeleteHandler = async (id: string) => {
    toastCustom({
      description: "",
    });
    setToastObject({
      message: "Deleting thread",
      type: "load",
    });
    try {
      const { success, message } = await DeleteComment(id);
      if (success) {
        let temp = threadList.filter((thread: Thread) => thread.id !== id);
        setThreadList([...temp]);
        setDeleteThreadAlert(false);

        setToastObject({
          message: message ? message : "Deleted",
          type: "success",
        });
        toastDismiss();
      } else {
        setToastObject({
          message: "Could not delete the comment",
          type: "error",
        });
        toastDismiss();
        setDeleteThreadAlert(false);
      }
    } catch (error) {
      console.log(error);

      setToastObject({
        message: "Could not delete the comment",
        type: "error",
      });
      toastDismiss();
    } finally {
      setThreadSelectedToDelete("");
    }
  };

  function formatTimestampToTime(timestamp: string) {
    const date = timestamp ? new Date(timestamp) : new Date();
    const currentDate = new Date();

    const timeDifference = currentDate.getTime() - date.getTime();

    if (timeDifference > 24 * 60 * 60 * 1000) {
      const formattedDate = new Intl.DateTimeFormat("en-IN", {
        year: "numeric",
        month: "numeric",
        day: "numeric",
      }).format(date);

      return formattedDate;
    }

    const formattedTime = new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      minute: "numeric",
      hour12: true,
    }).format(date);

    return formattedTime;
  }

  const RenderAttachmentComponent = React.memo(
    ({
      attachment,
    }: {
      attachment: ThreadAttachment | ThreadAttachmentLocal;
    }) => {
      const src = useMemo(() => {
        if ("url" in attachment) {
          return attachment.url;
        } else {
          return URL.createObjectURL(attachment);
        }
      }, [attachment]);

      if ("mediaType" in attachment) {
        switch (attachment.mediaType) {
          case "IMAGE":
            return <ImageComponent src={src} />;
          case "VIDEO":
            return <VideoComponent src={src} />;
          case "PDF":
            return <PdfComponent />;
          case "AUDIO":
            return <AudioComponent src={src} />;
          case "CRASH":
            return <CrashComponent />;
          default:
            return null;
        }
      } else if (attachment?.type) {
        switch (true) {
          case attachment.type.startsWith("image"):
            return <ImageComponent src={src} />;
          case attachment.type.startsWith("video"):
            return <VideoComponent src={src} />;
          case attachment.type.startsWith("audio"):
            return <AudioComponent src={src} />;
          case attachment.type.includes("pdf"):
            return <PdfComponent />;
          case attachment.type.startsWith("text"):
            return <CrashComponent />;
          default:
            return null;
        }
      }
      return null;
    },
  );

  return (
    <div className="comments-container">
      <div className="comments-header">
        <span className="comments-header-text">Comments</span>
      </div>
      <div className="comments-thread-conatiner">
        {threadList.length === 0 ? (
          !loading ? (
            <div className="no-comments-placeholder">No comments yet</div>
          ) : (
            <div className="flex flex-col items-center justify-center gap-2">
              <Spinner
                size={32}
                className={`no-comments-spinner flex animate-spin spinner`}
              />
              <span className="loading-comments">Loading comments</span>
            </div>
          )
        ) : (
          <div
            className="thread-list flex flex-col h-full w-full overflow-y-scroll snap-y snap-mandatory"
            ref={threadListRef}
          >
            {threadList.map((thread: Thread) => (
              <div className="thread scroll-snap-start" key={thread.id}>
                <div className="thread-header flex justify-center w-full items-center">
                  <div className="thread-meta flex-1">
                    <div className="thread-created-by">{thread.posterName}</div>
                    <div className="thread-created-at">
                      {thread.timestamp &&
                        formatTimestampToTime(thread.timestamp)}
                    </div>
                  </div>
                  {thread.id ? (
                    <Trash
                      className="text-[#747474] hover:text-black dark:hover:text-white cursor-pointer"
                      size={12}
                      onClick={() => {
                        setThreadSelectedToDelete(thread.id);
                        setDeleteThreadAlert(true);
                      }}
                    />
                  ) : (
                    <Spinner
                      size={12}
                      className={`comment-upload-spinner flex animate-spin spinner`}
                    />
                  )}
                </div>

                <div className="thread-messag">
                  {formatComments(thread.messages)}
                </div>
                {thread.uploads?.length > 0 && (
                  <div className="thread-attachments flex flex-col w-full items-start gap-4">
                    {thread.uploads?.map(
                      (
                        attachment: ThreadAttachment | ThreadAttachmentLocal,
                        index: number,
                      ) => (
                        <div
                          className="attachment flex rounded-[4px] overflow-hidden relative items-center justify-start"
                          key={index}
                          onClick={() => {
                            if ("url" in attachment) {
                              window.open(attachment.url, "_blank");
                            } else {
                              window.open(
                                URL.createObjectURL(attachment),
                                "_blank",
                              );
                            }
                          }}
                        >
                          <RenderAttachmentComponent attachment={attachment} />
                        </div>
                      ),
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <CommnetsInput
        threadList={threadList}
        setThreadList={setThreadList}
        reportId={reportId}
        organisationData={organisationData}
      />
      <SuspenseWrapper>
        <DeleteThreadDialog
          deleteThreadAlert={deleteThreadAlert}
          setDeleteThreadAlert={setDeleteThreadAlert}
          threadSelectedToDelete={threadSelectedToDelete}
          setThreadSelectedToDelete={setThreadSelectedToDelete}
          loading={loading}
          threadDeleteHandler={threadDeleteHandler}
        />
      </SuspenseWrapper>
    </div>
  );
};

export default Comments;
