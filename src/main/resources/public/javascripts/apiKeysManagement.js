//=====> EXTERNAL CONSTANTS

var NO_PERMS_SET = null;
var NO_NAME_SET = null;
var TOO_LONG_NAME = null;
var NAMED_USED = null;
var DELETE_KEY = null;


//=====> HELPER FUNCTIONS

function deleteKey(name, row) {
    return function () {
        apiV2Request('keys?name=' + name, 'DELETE').then(function () {
            row.remove();
        });
    }
}

function showError(error) {
    var alert = $('#keyAlert');
    alert.text(error);
    alert.show();
}


//=====> DOCUMENT READY

$(function () {
    $('.api-key-row').each(function () {
        var row = $(this);
        var name = row.find('.api-key-name').text();
        row.find('.api-key-row-delete-button').click(deleteKey(name, row));
    });

    $('#button-create-new-key').click(function () {
        var checked = [];
        $('#api-create-key-form').find('input[type=checkbox]').filter("input[id^='perm.']").filter(':checked').each(function () {
            checked.push($(this).attr('id').substr('perm.'.length))
        });
        var name = $('#keyName').val();

        if (checked.length === 0) {
            showError(NO_PERMS_SET);
        } else {
            var hasName = name.length !== 0;
            if (!hasName) {
                showError(NO_NAME_SET);
                return;
            }

            if(name.length > 255) {
                showError(TOO_LONG_NAME);
                return;
            }

            var nameTaken = $('.api-key-name:contains(' + name + ')').size();
            if (nameTaken !== 0) {
                showError(NAMED_USED);
                return;
            }

            var data = {
                permissions: checked,
                name: name
            };

            apiV2Request('keys', 'POST', data).then(function (newKey) {
                $('#keyAlert').hide();
                var namedPerms = '';

                for (let perm of checked) {
                    namedPerms += perm + ", "
                }

                namedPerms.substr(0, namedPerms.length - 2);

                var row = $('<tr>');
                var token = newKey.key;

                row.append($('<th>').addClass('api-key-name').text(name));
                row.append($('<th>').text(token));
                row.append($('<th>'));
                row.append($('<th>').text(namedPerms));
                row.append($('<th>').append($('<button>').addClass('btn btn-danger api-key-row-delete-button').text(DELETE_KEY).click(deleteKey(name, row))));

                $('#api-key-rows:last-child').append(row);
            })
        }
    });
});