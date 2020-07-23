//=====> HELPER FUNCTIONS

function initUserSearch(callback) {
    var search = $('.user-search');
    var input = search.find('input');

    // Disable button with no input
    input.on('input', function() {
        $(this).next().find('.btn').prop('disabled', $(this).val().length === 0);
    });

    // Catch enter key
    input.on('keypress', function(event) {
        if (event.keyCode === KEY_ENTER) {
            event.preventDefault();
            $(this).next().find('.btn').click();
        }
    });

    // Search for user
    search.find('.btn-search').click(function() {
        const input = $(this).closest('.user-search').find('input');
        const username = input.val().trim();
        if(username !== "") {
            var icon = toggleSpinner($(this).find('[data-fa-i2svg]').toggleClass('fa-search'));
            $.ajax({
                url: '/api/v1/users/' + username,
                dataType: 'json',

                complete: function() {
                    input.val('');
                    toggleSpinner($(".user-search .btn-search [data-fa-i2svg]").toggleClass('fa-search'));
                    $(".user-search .btn-search").prop('disabled', true);
                },

                error: function() {
                    callback({
                        isSuccess: false,
                        username: username,
                        user: null
                    })
                },

                success: function(user) {
                    callback({
                        isSuccess: true,
                        username: username,
                        user: user
                    });
                }
            });
        }
    });
}
