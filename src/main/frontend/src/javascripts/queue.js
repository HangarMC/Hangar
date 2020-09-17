//=====> DOCUMENT READY

$(function() {
    $('.btn-approve').click(function() {
        var listItem = $(this).closest('.list-group-item');
        var versionPath = listItem.data('version');
        var icon = toggleSpinner($(this).find('[data-fa-i2svg]').removeClass('fa-thumbs-up'));
        $.ajax({
            type: 'post',
            url: '/' + versionPath + '/approve',
            complete: function() { toggleSpinner($('.btn-approve').find('[data-fa-i2svg]').addClass('fa-thumbs-up')); },
            success: function() {
                $.when(listItem.fadeOut('slow')).done(function() { 
                    listItem.remove();
                    if (!$('.list-versions').find('li').length) {
                        $('.no-versions').fadeIn();
                        clearUnread($('a[href="/admin/queue"]'));
                    }
                });
            }
        });
    });
});
