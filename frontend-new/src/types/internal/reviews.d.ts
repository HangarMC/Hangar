declare module "hangar-internal" {
  import type { Model, ProjectNamespace } from "hangar-api";
  import type { Platform, ReviewAction } from "~/types/enums";

  interface HangarReviewMessage extends Model {
    message: string;
    args: Record<string, string>;
    action: ReviewAction;
  }

  interface HangarReview extends Model {
    endedAt: string | null;
    userName: string;
    userId: number;
    messages: HangarReviewMessage[];
  }

  interface Review {
    reviewerName: string;
    reviewStarted: string;
    reviewEnded: string | null;
    lastAction: ReviewAction;
  }

  interface ReviewQueueEntry {
    namespace: ProjectNamespace;
    versionId: number;
    versionString: string;
    platforms: Platform[];
    versionCreatedAt: string;
    versionAuthor: string;
    channelName: string;
    channelColor: string;
    reviews: Review[];
  }
}
