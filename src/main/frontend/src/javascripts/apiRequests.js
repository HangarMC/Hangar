//=====> HELPER FUNCTIONS

function apiV2Request(url, method = "GET", data = {}) {
    return getApiSession().then(function (session) {
        return new Promise(function (resolve, reject) {
            const isFormData = data instanceof FormData;
            const isBodyRequest = (method === "POST" || method === "PUT" || method === "PATCH");

            $.ajax({
                url: '/api/v2/' + url,
                method: method,
                dataType: 'json',
                contentType: isFormData ? false : 'application/json',
                data: isBodyRequest && !isFormData ? JSON.stringify(data) : data,
                processData: !(isFormData || isBodyRequest),
                headers: {'Authorization': 'HangarApi session=' + session}
            }).done(function (data) {
                resolve(data);
            }).fail(function (xhr) {
                if (xhr.responseJSON && (xhr.responseJSON.error === 'Api session expired' || xhr.responseJSON.error === 'Invalid session')) {
                    // This should never happen but just in case we catch it and invalidate the session to definitely get a new one
                    invalidateApiSession();
                    apiV2Request(url, method, data).then(function (data) {
                        resolve(data);
                    }).catch(function (error) {
                        reject(error);
                    });
                } else {
                    reject(xhr.statusText)
                }
            })
        })
    });
}

function getApiSession() {
    return new Promise(function (resolve, reject) {
        let session;
        const date = new Date();
        date.setTime(date.getTime() + 60000);

        if (window.isLoggedIn) {
            session = parseOrNull(localStorage.getItem('api_session'));
            if (session === null || !isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date) {
                return $.ajax({
                    url: '/api/v2/authenticate/user',
                    method: 'POST',
                    dataType: 'json'
                }).done(function (data) {
                    if (data.type !== 'user') {
                        reject('Expected user session from user authentication');
                    } else {
                        localStorage.setItem('api_session', JSON.stringify(data));
                        resolve(data.session);
                    }
                }).fail(function (xhr) {
                    reject(xhr.statusText)
                })
            } else {
                resolve(session.session);
            }
        } else {
            session = parseOrNull(localStorage.getItem('public_api_session'));
            if (session === null || !isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date) {
                $.ajax({
                    url: '/api/v2/authenticate',
                    method: 'POST',
                    dataType: 'json'
                }).done(function (data) {
                    if (data.type !== 'public') {
                        reject('Expected public session from public authentication')
                    } else {
                        localStorage.setItem('public_api_session', JSON.stringify(data));
                        resolve(data.session);
                    }
                }).fail(function (xhr) {
                    reject(xhr.statusText)
                })
            } else {
                resolve(session.session);
            }
        }
    });
}

function invalidateApiSession() {
    if (window.isLoggedIn) {
        localStorage.removeItem('api_session')
    }
    else {
        localStorage.removeItem('public_api_session')
    }
}

function parseOrNull(jsonString) {
    try {
        return JSON.parse(jsonString);
    } catch (e) {
        return null;
    }
}
