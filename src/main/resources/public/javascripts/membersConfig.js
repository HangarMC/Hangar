//=====> HELPER FUNCTIONS

function updateIndices() {
    // Set the input fields to their proper indices so the server can read
    // them as a list.
    var rows = $('.table-members').find('tr');
    rows.each(function(i) {
        if (i == 0 || i == rows.length - 1) return; // Skip owner and search rows
        var index = i - 1;
        $(this).find('input').attr('name', 'users[' + index + ']');
        $(this).find('select').attr('name', 'roles[' + index + ']');
    });
}


//=====> DOCUMENT READY

$(function() {
    initUserSearch(function(result) {
        var alert = $('.alert-danger');
        if (!result.isSuccess) {
            $('.error-username').text(result.username);
            alert.fadeIn();
            return;
        }
        alert.fadeOut();

        // Check if user is already defined
        var user = result.user;
        if ($('input[value="' + user.id + '"]').length
            || $('.table-members').first('tr').find('strong').text() === user.username) {
            return;
        }

        // Build the result row from the template
        var newRow = $('#result-row').clone().removeAttr('id');
        newRow.find('input').attr('form', 'form-continue').val(user.id);
        newRow.find('select').attr('form', 'form-continue');
        newRow.find('.username').attr('href', '/' + user.username).text(user.username);

        var avatarImg = newRow.find('.user-avatar');
        if (user.hasOwnProperty('avatarUrl')) {
            avatarImg.css('background-image', 'url(' + user['avatarUrl'] + ')');
        } else {
            avatarImg.remove();
        }
        
        // Bind cancel button
        newRow.find('.user-cancel').click(function() {
            $(this).closest('tr').remove();
            updateIndices();
        });

        // Insert the new row before the search row
        $('.user-search').closest('tr').before(newRow);
        updateIndices();
    });

    updateIndices();
});
