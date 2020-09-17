//=====> HELPER FUNCTIONS

function updateIndices() {
    var memberList = $('.list-members');
    memberList.find('.user-new').each(function() {
        $(this).find('input').attr('name', 'users');
        $(this).find('select').attr('name', 'roles');
    });

    memberList.find('.user-changed').each(function(i) {
        $(this).find('input').attr('name', 'userUps');
        $(this).find('select').attr('name', 'roleUps');
    });
}

function getItemContainer(element) {
    return element.closest('.list-group-item');
}

function initMember(memberRow) {
    // Replace title with select on click
    memberRow.find('.fa-edit').parent().click(function(event) {
        event.preventDefault();
        var currentRole = getItemContainer($(this)).find("span.minor.pull-right").text().replace(/(\r\n|\n|\r|\s)/gm, "");

        var saveBtn = $('.btn-members-save');
        if (!saveBtn.is(':visible'))
            saveBtn.fadeIn('fast');

        // Mark user as changed
        var container = getItemContainer($(this)).addClass('user-changed');
        var input = $('#select-role').clone().removeAttr('id').attr('form', 'save');

        input.find('option').each(function(i, el) {
            if($(el).val().endsWith(currentRole)) {
                $(el).attr('selected', '')
            }
        })

        // Add input
        container.find('span').replaceWith(input.show());
        var username = container.find('.username').text().trim();
        container.append('<input type="hidden" form="save" value="' + username + '" />');

        // Remove edit button and update input names
        $(this).find('.fa-edit').parent().remove();
        updateIndices();
    });

    // Set form input on modal when delete is clicked
    memberRow.find('.fa-trash').parent().click(function(event) {
        event.preventDefault();
        $('#modal-user-delete').find('input[name=username]').val(getItemContainer($(this)).find('.username').text().trim());
    });
}


//=====> DOCUMENT READY

$(function() {
    initMember($('.list-members').find('.list-group-item'));

    initUserSearch(function(result) {
        var alert = $('.member-error');
        var message = alert.find('span');
        if (!result.isSuccess) {
            message.text('Could not find user with name "' + result.username + '".');
            alert.fadeIn();
            return;
        }
        alert.fadeOut();

        var saveBtn = $('.btn-members-save');
        if (!saveBtn.is(':visible'))
            saveBtn.fadeIn('fast');

        // hangar: user.name was username everywhere
        var user = result.user;
        // Check if user is already defined
        if ($('.list-members').find('a[href="/' + user.username + '"]').length) {
            return;
        }

        // Build result row
        var resultRow = $('#row-user').clone().removeAttr('id').addClass('user-new');
        resultRow.find('.username').attr('href', '/' + user.username).text(user.username);
        resultRow.find('input').attr('form', 'save').val(user.id);
        resultRow.find('select').attr('form', 'save');
        resultRow.find('svg').click(function() {
            $(this).parent().remove();
            updateIndices();
        });


        var avatarImg = resultRow.find('.user-avatar');
        if (user.hasOwnProperty('avatarUrl')) {
            var avatarUrl = user['avatarUrl'];
            avatarImg.css('background-image', 'url(' + avatarUrl + ')');
        } else
            avatarImg.remove();

        // Add result to list
        $('.user-search').parent().before(resultRow);
        updateIndices();
    });

    updateIndices();
});
