$(function() {
    var rowTemplate = "<tr id='version-%val%' class='version-add'></tr>";
    var versionTemplate = "<td>%val%<input type='hidden' value='%val%' name='versions'></td>";
    var removeTemplate = "<button class='btn btn-danger remove-version' data-version='%val%'><span class='glyphicon glyphicon-remove'></span></button>";

    $(".add-version").on('click', function(event) {
        event.preventDefault();
        var input = $("#" + $(event.currentTarget).data("input"));
        if (input.val() === '') return;
        var table = $("#" + $(event.currentTarget).data("table"));
        var row = $(rowTemplate.replace(/%val%/g, input.val()));
        row.append($(versionTemplate.replace(/%val%/g, input.val())));
        var removeBtn = $(removeTemplate.replace(/%val%/g, input.val()));
        $("<td></td>").appendTo(row).append(removeBtn);
        table.append(row);
        removeBtn.on('click', onRemoveClick);
        input.val("");
        $(this).prop('disabled', true);
    });

    $(".version-input").on('input', function(event) {
        if (typeof event.target.value === 'undefined' || event.target.value.trim() === '') {
            $("#" + $(this).data("button")).prop('disabled', true);
        } else {
            $("#" + $(this).data("button")).prop('disabled', false);
        }
    });

    $('.remove-version').on('click', onRemoveClick);

    function onRemoveClick(event) {
        event.preventDefault();
        $("<input type='hidden' name='removedVersions' value='" + $(this).data('version') + "'>").appendTo(this);
        if ($(this).parent().parent().hasClass('version-add')) {
            $(this).parent().parent().remove();
        } else {
            $(this).parent().parent().css('display', 'none');
        }
    }
});