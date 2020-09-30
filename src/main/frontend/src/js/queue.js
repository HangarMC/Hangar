import $ from 'jquery';
import moment from 'moment';
import { clearUnread, toggleSpinner } from '@/utils';

//=====> EXTERNAL CONSTANTS
const MAX_REVIEW_TIME = window.MAX_REVIEW_TIME;

//=====> DOCUMENT READY

$(function () {
    var momentNow = moment();
    var maxDifference = MAX_REVIEW_TIME;
    $('span[data-ago]').each(function () {
        var momentAgo = moment($(this).data('ago'));
        $(this).text($(this).data('title') + momentAgo.fromNow());
        if (momentNow.diff(momentAgo) >= maxDifference) {
            $(this)
                .text('pastdue ' + momentAgo.fromNow())
                .css('color', 'darkred');
            $(this)
                .parent()
                .parent()
                .find('.status')
                .removeClass()
                .addClass('status far fa-fw fa-clock fa-2x')
                .css('color', 'darkred');
        }
    });

    $('.btn-approve').click(function () {
        var listItem = $(this).closest('.list-group-item');
        var versionPath = listItem.data('version');
        toggleSpinner($(this).find('[data-fa-i2svg]').removeClass('fa-thumbs-up'));
        $.ajax({
            type: 'post',
            url: '/' + versionPath + '/approve',
            complete: function () {
                toggleSpinner($('.btn-approve').find('[data-fa-i2svg]').addClass('fa-thumbs-up'));
            },
            success: function () {
                $.when(listItem.fadeOut('slow')).done(function () {
                    listItem.remove();
                    if (!$('.list-versions').find('li').length) {
                        $('.no-versions').fadeIn();
                        clearUnread($('a[href="/admin/queue"]'));
                    }
                });
            },
        });
    });
});
