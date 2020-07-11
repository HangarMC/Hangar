//=====> EXTERNAL CONSTANTS

var USERNAME = window.USERNAME;
var NO_ACTION_MESSAGE = {};
var CATEGORY_TITLE = {};
var CATEGORY_ICON = {};

var CONTENT_PER_PAGE = 5;

var pages = {
    starred: 1,
    watching: 1
};


//=====> HELPER FUNCTIONS

function getStarsPanel(action) {
    return $('.panel-user-info[data-action=' + action + ']');
}

function getStarsFooter(action) {
    return getStarsPanel(action).find('.panel-footer');
}

function loadActions(increment, action) {
    pages[action] += increment;
    var offset = (pages[action] - 1) * CONTENT_PER_PAGE;

    apiV2Request('users/' + USERNAME + '/' + action + '?offset=' + offset + '&limit=' + CONTENT_PER_PAGE).then(function (result) {
        //TODO: Use pagination info
        var tbody = getStarsPanel(action).find('.panel-body').find('tbody');

        var content = [];

        if(result.pagination.count === 0) {
            content.push($("<tr>").append($("<td>").append($("<i class='minor'>").text(NO_ACTION_MESSAGE[action]))));
        }
        else {
            for (var project of result.result) {

                var link = $("<a>").attr("href", '/' + project.namespace.owner + '/' + project.namespace.slug).text(project.namespace.owner + '/').append($("<strong>").text(project.namespace.slug));
                var versionDiv = $("<div class='pull-right'>");
                if (project.recommended_version) {
                    versionDiv.append($("<span class='minor'>").text(project.recommended_version.version));
                }

                var versionIcon = $("<i>");
                versionIcon.attr("title", CATEGORY_TITLE[project.category]);
                versionIcon.addClass('fas fa-fw').addClass(CATEGORY_ICON[project.category]);
                versionDiv.append(versionIcon);

                content.push($("<tr>").append($("<td>").append(link, versionDiv)));
            }
        }

        // Done loading, set the table to the result
        tbody.empty();
        tbody.append(content);
        var footer = getStarsFooter(action);
        var prev = footer.find('.prev');

        // Check if there is a last page
        if (pages[action] > 1) {
            prev.show();
        } else {
            prev.hide();
        }

        // Check if there is a next page
        var next = footer.find('.next');
        if (result.pagination.count > pages[action] * CONTENT_PER_PAGE) {
            next.show();
        } else {
            next.hide();
        }
    });
}

function formAsync(form, route, onSuccess) {
    form.submit(function (e) {
        e.preventDefault();
        var formData = new FormData(this);
        var spinner = $(this).find('.fa-spinner').show();
        $.ajax({
            url: route,
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            type: 'post',
            dataType: 'json',
            complete: function () {
                spinner.hide();
            },
            success: onSuccess
        });
    });
}

function setupAvatarForm() {

    $('.btn-got-it').click(function () {
        var prompt = $(this).closest('.prompt');
        $.ajax({
            type: 'post',
            url: 'prompts/read/' + prompt.data('prompt-id')
        });
        prompt.fadeOut('fast');
    });

    $('.organization-avatar').hover(function () {
        $('.edit-avatar').fadeIn('fast');
    }, function (e) {
        if (!$(e.relatedTarget).closest("div").hasClass("edit-avatar")) {
            $('.edit-avatar').fadeOut('fast');
        }
    });

    var avatarModal = $('#modal-avatar');
    avatarModal.find('.alert').hide();

    var avatarForm = avatarModal.find('#form-avatar');
    avatarForm.find('input[name="avatar-method"]').change(function () {
        avatarForm.find('input[name="avatar-file"]').prop('disabled', $(this).val() !== 'by-file');
    });

    formAsync(avatarForm, 'organizations/' + USERNAME + '/settings/avatar', function (json) {
        if (json.hasOwnProperty('errors')) {
            var alert = avatarForm.find('.alert-danger');
            alert.find('.error').text(json['errors'][0]);
            alert.fadeIn('slow');
        } else {
            avatarModal.modal('hide');
            var success = $('.alert-success');
            success.find('.success').text('Avatar successfully updated!');
            success.fadeIn('slow');
            $('.user-avatar[title="' + USERNAME + '"]')
                .prop('src', json['avatarTemplate'].replace('{size}', '200'));
        }
    });
}


//=====> DOCUMENT READY

$(function () {
    for (let action of ['starred', 'watching']) {
        var footer = getStarsFooter(action);
        loadActions(0, action);
        footer.find('.next').click(function () {
            loadActions(1, action);
        });
        footer.find('.prev').click(function () {
            loadActions(-1, action);
        });
    }

    setupAvatarForm();
});
