//=====> DOCUMENT READY

$(function() {
    $('.btn-note-addmessage-submit').click(function() {
        var panel = $(this).parent().parent().parent();
        var textarea = panel.find('textarea');
        textarea.attr('disabled', 'disabled');
        toggleSpinner($(this).find('[data-fa-i2svg]').toggleClass('fa-save'));
        $.ajax({
            type: 'post',
            url: '/' + resourcePath + '/notes/addmessage',
            data: { content: textarea.val() },
            success: function() {
                location.reload();
            }
        });
    });
});
