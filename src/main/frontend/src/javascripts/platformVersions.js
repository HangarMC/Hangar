$(function() {
    var rowTemplate = "<tr id='version-%val%' class='version-add success'></tr>";
    var versionTemplate = "<td>%val%<input type='hidden' value='%val%' name='versions'></td>";
    var removeTemplate = "<button class='btn btn-danger remove-version' data-version='%val%'><span class='glyphicon glyphicon-remove'></span></button>";

    $(".add-version").on('click', function(event) {
        event.preventDefault();
        var input = $("#" + CSS.escape($(event.currentTarget).data("input")));
        var value = input.val();
        if (value === '') {
            return;
        }
        var table = $("#" + CSS.escape($(event.currentTarget).data("table")));
        var existing = table.find('#version-' + CSS.escape(input.val()));
        if (existing.length !== 0) {
            existing.css('display', '');
            input.val('');
            return;
        }
        var row = $(rowTemplate.replace(/%val%/g, value));
        row.append($(versionTemplate.replace(/%val%/g, value)));
        var removeBtn = $(removeTemplate.replace(/%val%/g, value));
        $("<td class='table-shrink'></td>").appendTo(row).append(removeBtn);
        table.append(row);
        removeBtn.on('click', onRemoveClick);
        input.val('');
        $(this).prop('disabled', true);
        $(this).closest('form').find('.version-submit').prop('disabled', false);
    });

    $(".version-input").on('input', function(event) {
        var btn = $("#" + CSS.escape($(this).data("button")));
        if (typeof event.target.value === 'undefined' || event.target.value.trim() === '') {
            btn.prop('disabled', true);
        } else {
            btn.prop('disabled', false);
        }
    });

    $('.remove-version').on('click', onRemoveClick);

    function onRemoveClick(event) {
        event.preventDefault();
        $("<input type='hidden' name='removedVersions' value='" + $(this).data('version') + "'>").appendTo(this);
        var submitBtn = $(this).closest('form').find('.version-submit');
        if ($(this).closest('tr').hasClass('version-add')) {
            $(this).closest('tr').remove();
            if ($(this).closest('table').find('version-add').length === 0) {
                submitBtn.prop('disabled', true);
            }
        } else {
            submitBtn.prop('disabled', false);
            $(this).closest('tr').css('display', 'none');
        }
    }
});