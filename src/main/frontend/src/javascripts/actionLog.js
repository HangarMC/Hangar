//=====> DOCUMENT READY

$('body').on('click', '.data-diff', function() {
    var idToDiff = $(this).attr('data-diff');

    var diff = new diff_match_patch();
    var textDiff = diff.diff_main($('textarea[data-oldstate="' + idToDiff + '"]').val(), $('textarea[data-newstate="' + idToDiff + '"]').val());
    diff.diff_cleanupSemantic(textDiff);

    $('#modal-view-body').html(diff.diff_prettyHtml(textDiff).replace(/&para;/g, ''));
    $('#modal-view').modal('show');

}).on('click', '.data-view-old', function() {
    var idToShow = $(this).attr('data-view');

    $('#modal-view-body').html('<textarea disabled style="width: 100%; height: 35vh">' + $('textarea[data-oldstate="' + idToShow + '"]').val() + '</textarea>');
    $('#modal-view').modal('show');
}).on('click', '.data-view-new', function() {
    var idToShow = $(this).attr('data-view');

    $('#modal-view-body').html('<textarea disabled style="width: 100%; height: 35vh">' + $('textarea[data-newstate="' + idToShow + '"]').val() + '</textarea>');
    $('#modal-view').modal('show');
});
