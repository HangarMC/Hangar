declare module 'hangar-internal' {
    import { Model, ProjectNamespace } from 'hangar-api';
    import { ReviewAction } from '~/types/enums';

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

    interface ReviewQueueEntry {
        namespace: ProjectNamespace;
        versionString: string;
        versionCreatedAt: string;
        versionAuthor: string;
        channelName: string;
        channelColor: string;
        reviewerName: string | null;
        reviewStarted: string | null;
        reviewEnded: string | null;
    }
}
