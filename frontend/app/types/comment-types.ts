export interface ThreadAttachment {
  mediaType: string;
  url: string;
}

export interface MemberMention {
  display: string;
  id: string;
  email: string;
}

export type ThreadAttachmentLocal = Blob;

export interface Thread {
  id: string;
  mentions: string[];
  messages: string;
  posterId: string;
  posterName: string;
  timestamp: string | undefined;
  uploads: ThreadAttachment[] | ThreadAttachmentLocal[];
}
