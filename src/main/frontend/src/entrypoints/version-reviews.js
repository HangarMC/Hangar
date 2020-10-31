import { createApp } from 'vue';
import { setupI18n } from '@/plugins/i18n';
import ReviewBtnGroup from '@/components/entrypoints/versions/reviews/ReviewBtnGroup';
import ReviewNewMessage from '@/components/entrypoints/versions/reviews/ReviewNewMessage';

const i18n = setupI18n();
createApp(ReviewBtnGroup).use(i18n).mount('#review-btn-group');
if (document.getElementById('review-new-message')) {
    createApp(ReviewNewMessage).use(i18n).mount('#review-new-message');
}
