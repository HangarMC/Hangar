declare module 'hangar-internal' {
    import { Model } from 'hangar-api';
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
}
