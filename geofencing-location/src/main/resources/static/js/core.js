// VARIABLES =============================================================
var TOKEN_KEY = "jwtToken";
var GEOFENCES_NAME = "arrayGeofences";

// FUNCTIONS =============================================================
function getJwtToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function setJwtToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function removeJwtToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function doLogin(loginData) {
    $.ajax({
        url: "http://localhost:8080/api/users/auth",
        type: "POST",
        data: JSON.stringify(loginData),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data, textStatus, jqXHR) {
            setJwtToken(data.token);
            window.location.replace("/home");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 401) {
                console.log("Unauthorized request");
            } else {
                throw new Error("an unexpected error occured: " + errorThrown);
            }
        }
    });
}

function doLogout() {
    removeJwtToken();
}

function getData() {
    getGeofences();
    getNotifications();
}

function getGeofences() {
    $.ajax({
        url: "http://localhost:8080/api/geofences",
        type: "GET",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            var geofences = [];
            for (var i = 0; i < data.length; i++) {
               var geo = [];
               var coordinates = data[i].geometry.coordinates[0];
               for (var j = 0; j < coordinates.length; j++) {
                    geo.push({lat: coordinates[j][0], lng: coordinates[j][1]});
               }
               geofences.push(geo);
            }
            localStorage.setItem(GEOFENCES_NAME, JSON.stringify(geofences));
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });
}

function getNotifications() {
    $.ajax({
        url: "http://localhost:8080/api/notifications",
        type: "GET",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            var len = data.length;
            if (len > 10) {
                len = 10;
            }
            $('#table tr td').remove();
            for (var i = 0; i < len; i++) {
               var date = data[i].date;
               var message = data[i].rule.message;
               $('#table').append('<tr><td><div style="text-align:right"><b>'+date
                    +'</b></div><div class="message">'+message+'</div></td></tr>');
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });
}

function createAuthorizationTokenHeader() {
    var token = getJwtToken();
    if (token) {
        return {"Authorization": token};
    } else {
        return {};
    }
}

// REGISTER EVENT LISTENERS =============================================================
$("#loginForm").submit(function (event) {
    event.preventDefault();

    var $form = $(this);
    var formData = {
        username: $form.find('input[name="username"]').val(),
        password: $form.find('input[name="password"]').val()
    };

    doLogin(formData);
});

$("#logout").click(doLogout);

var timerData = setTimeout(gettingData, 5000);

function gettingData() {
    getData();
    timerData = setTimeout(gettingData, 30000);
}

function abortTimerData() {
    clearTimeout(timerData);
}