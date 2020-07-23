//=====> DOCUMENT READY

$(function() {
    var modal = $('#new-page');
    modal.on('shown.bs.modal', function() { $(this).find('input').focus(); });

    modal.find('input').keydown(function(event) {
        if (event.keyCode === KEY_ENTER) {
            event.preventDefault();
            $('#continue-page').click();
        }
    });

    $('#continue-page').click(function() {
        var pageName = $('#page-name').val().trim();
        var url = '/' + projectOwner + '/' + projectSlug + '/pages/' + slugify(pageName) + '/edit';
        var parent = $('.select-parent').find(':selected');
        var parentId = null;

        if (parent.length) {
            parentId = parent.val() === "-1" ? null : parent.val();

            if (parentId !== null)
                url = '/' + projectOwner + '/' + projectSlug + '/pages/' + parent.data('slug') + '/' + slugify(pageName) + '/edit';
        }
        $.ajax({
            method: 'post',
            url: url,
            data: {'parent-id': parentId, 'content': '# ' + pageName + '\n', 'name': pageName},
            success: function() {
                go(url);
            },
            error: function() {
                console.log("error");
                $("#new-page-label-error").show().delay(2000).fadeOut();
            }
        });
    })
});
