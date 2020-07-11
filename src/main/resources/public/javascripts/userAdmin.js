//=====> HELPER FUNCTIONS

function update(thing, action, data) {
    return $.ajax({
        url: window.location.pathname + '/update',
        method: 'post',
        data: {
            thing: thing,
            action: action,
            data: JSON.stringify(data)
        }
    });
}

function loadingButton(button) {
    var children = button.children();
    children.remove();
    button.append('<i class="fa fa-spinner fa-spin"></i>');
    return function () {
        button.children().remove();
        button.append(children);
    };
}


//=====> DOCUMENT READY

$(function() {
    var globalRoleSelect = $('#add-global-role-select')
    var globalRoleList = $('#global-roles-list');

    $('#add-global-role').click(function () {
        var finishLoading = loadingButton($('#add-global-role'));

        var selected = $('option:selected', globalRoleSelect);

        update('globalRole', 'add', { role: parseInt(selected.val()) })
            .done(function () {
                // Add role to list
                var newItem = $('<div class="list-group-item"></div>');
                newItem.data({role: selected.val()});
                newItem.text(selected.text());
                newItem.append($('<span class="pull-right"><a href="#" class="global-role-delete"><i class="fa fa-trash"></i></a></span>'));
                newItem.insertBefore(globalRoleList.children().last());
                // Remove from select
                selected.remove();
            })
            .always(finishLoading);

    });

    globalRoleList.on('click', '.global-role-delete', function(event) {
        event.preventDefault();
        var item = $(event.target).closest('.list-group-item');

        var finishLoading = loadingButton($(event.target).parent());

        var role = parseInt(item.data('role'));
        var title = item.text();

        update('globalRole', 'remove', { role: role })
            .done(function () {
                var option = $('<option></option>');
                option.text(title);
                option.val(role);
                globalRoleSelect.append(option);
                item.remove();
            })
            .fail(finishLoading);
    });
    
    function rowChange(action, data, element) {
        element.disabled = true;
        var row = $(element).closest('tr');
        var id = parseInt(row.data('role-id'));
        var type = row.data('role-type');
        return update(type, action, $.extend({ id: id }, data))
            .always(function () {
                element.disabled = false;
            });
    }

    var roleTables = $('.role-table');

    roleTables.on('change', '.select-role', function(event) {
        rowChange('setRole', { role: parseInt(event.target.value) }, event.target)
            .fail(function () {
                event.target.selectedIndex = event.target.defaultValue;
            })
            .done(function () {
                event.target.defaultValue = event.target.selectedIndex;
                if ($('option:selected', event.target).data('refresh')) {
                    history.go(0)
                }
            });
    });

    roleTables.find('.select-role').each(function (i, element) {
        element.defaultValue = element.selectedIndex;
    });

    roleTables.on('change', '.role-accepted', function(event) {
        rowChange('setAccepted', { accepted: event.target.checked }, event.target)
            .fail(function () {
                event.target.checked = !event.target.checked;
            });
    });
    roleTables.on('click', '.delete-role', function(event) {
        event.preventDefault();
        var row = $(event.target).closest('tr');
        rowChange('deleteRole', {}, event.target)
            .done(row.remove);
    });
});
