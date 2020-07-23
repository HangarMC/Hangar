//=====> CONSTANTS

var ICON = 'fa-eye';


//=====> DOCUMENT READY

$(function() {
    $('.btn-visibility-change').click(function () {
        var project = $(this).data('project');
        var visibilityLevel = $(this).data('level');
        var needsModal = $(this).data('modal');
        var spinner = $('button[data-project="'  + project + '"]').find('[data-fa-i2svg]');
        toggleSpinner(spinner.toggleClass(ICON));
        if (needsModal) {
            $('.modal-title').html($(this).text().trim() + ": comment");
            $('#modal-visibility-comment').modal('show');
            $('.btn-visibility-comment-submit').data('project', project);
            $('.btn-visibility-comment-submit').data('level', visibilityLevel);
            toggleSpinner(spinner.toggleClass(ICON));
        } else {
            sendVisibilityRequest(project, visibilityLevel, '', spinner);
        }
    });

    $('.btn-visibility-comment-submit').click(function () {
        var project = $(this).data('project');
        var visibilityLevel = $(this).data('level');
        var spinner = $(this).find('i');
        toggleSpinner(spinner.toggleClass(ICON));
        sendVisibilityRequest(project, visibilityLevel, $('.textarea-visibility-comment').val(), spinner);

    });

    function sendVisibilityRequest(project, level, comment, spinner) {
        var _url = '/' + project + (level == -99 ? '/manage/hardDelete' : '/visible/' + level);
        $.ajax({
            type: 'post',
            url: _url,
            data: { comment: comment },
            fail: function () {
                toggleSpinner($('button[data-project="'  + project + '"]').find('[data-fa-i2svg]').toggleClass(ICON));
            },
            success: function () {
                toggleSpinner($('button[data-project="'  + project + '"]').find('[data-fa-i2svg]').toggleClass(ICON));
                location.reload();
            }
        });
    }
});
