//=====> HELPER FUNCTIONS

function validateMeta(pluginId, name, owner, slug) {
    checkId(pluginId, name, owner, slug)
}

function tooltip(selector, title) {
    $(selector).tooltip({
        placement: 'right',
        title: title
    });
}

function success(selector, then) {
    // Simulate loading :P
    setTimeout(function() {
        toggleSpinner($(selector));
        $(selector).addClass('fa-check-circle');
        then();
    }, 500);
}

function failed(selector, message) {
    toggleSpinner($(selector));
    $(selector).addClass('fa-times-circle');
    tooltip(selector, message);
}

function checkId(pluginId, name, owner, slug) {
    $.ajax({
        url: '/api/v1/projects/' + pluginId,
        statusCode: {
            404: function() {
                success('.id-status', function() {
                    checkName(name, true, owner, slug);
                });
            },
            200: function() {
                failed('.id-status', 'That plugin ID is not available!');
                checkName(name, false, owner, slug);
            }
        }
    })
}

function checkName(name, idSuccess, owner, slug) {
    if (name.length > 25) {
        failed('.name-status', 'This name is too long. Please rename your project to something under 25 characters.');
        updateContinueButton(idSuccess, false);
        return;
    }

    $.ajax({
        url: '/' + owner + '/' + slug,
        statusCode: {
            404: function() {
                success('.name-status', function() {
                    updateContinueButton(idSuccess, true);
                });
            },
            200: function() {
                failed('.name-status', 'You already have a project of this name!');
                updateContinueButton(idSuccess, false);
            }
        }
    });
}

function updateContinueButton(idSuccess, nameSuccess) {
    var btn = $('.continue-btn').hide().removeClass('btn-default');
    var icon = toggleSpinner(btn.find('[data-fa-i2svg]'));
    if (idSuccess && nameSuccess) {
        btn.addClass('btn-primary').prop('disabled', false);
        icon.addClass('fa-arrow-right');
    } else {
        btn.addClass('btn-danger');
        icon.addClass('fa-times')
    }
    btn.fadeIn();
}
