//=====> DOCUMENT READY

$(function() {
    $('.btn-resolve').click(function() {
        var listItem = $(this).closest('li');
        var flagId = listItem.data('flag-id');
        var spinner = toggleSpinner($(this).find('[data-fa-i2svg]').removeClass('fa-check'));

        $.ajax({
            url: '/admin/flags/' + flagId + '/resolve/true',
            complete: function() { toggleSpinner($('.btn-resolve').find('[data-fa-i2svg]').addClass('fa-check')); },
            success: function() {
                $.when(listItem.fadeOut('slow')).done(function() {
                    listItem.remove();
                    if (!$('.list-flags-admin').find('li').length) {
                        resolveAll.fadeOut();
                        $('.no-flags').fadeIn();
                        clearUnread($('a[href="/admin/flags"]'));
                    }
                });
            }
        });
    });
});
