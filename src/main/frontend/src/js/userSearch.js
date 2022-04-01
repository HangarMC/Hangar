import $ from 'jquery';
import { KEY_ENTER, toggleSpinner } from '@/utils';
import { API } from '@/api';

//=====> HELPER FUNCTIONS

export function initUserSearch(callback) {
    var search = $('.user-search');
    var input = search.find('input');

    // Disable button with no input
    input.on('input', function () {
        $(this)
            .next()
            .find('.btn')
            .prop('disabled', $(this).val().length === 0);
    });

    // Catch enter key
    input.on('keypress', function (event) {
        if (event.keyCode === KEY_ENTER) {
            event.preventDefault();
            $(this).next().find('.btn').click();
        }
    });

    // Search for user
    search.find('.btn-search').click(function () {
        const input = $(this).closest('.user-search').find('input');
        const username = input.val().trim();
        if (username !== '') {
            toggleSpinner($(this).find('[data-fa-i2svg]').toggleClass('fa-search'));
            API.request(`users/${username}`)
                .then((user) => {
                    callback({
                        isSuccess: true,
                        username: username,
                        user: user,
                    });
                })
                .catch(() => {
                    callback({
                        isSuccess: false,
                        username: username,
                        user: null,
                    });
                })
                .finally(() => {
                    input.val('');
                    toggleSpinner($('.user-search .btn-search [data-fa-i2svg]').toggleClass('fa-search'));
                    $('.user-search .btn-search').prop('disabled', true);
                });
        }
    });
}
