// VARIABLES =============================================================
var TOKEN_KEY = "jwtToken";

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

function createAuthorizationTokenHeader() {
    "use strict";
    var token = getJwtToken();
    if (token) {
        return {"Authorization": token};
    }
    return {};
}

function isNumberKey(evt) {
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57))
        return false;
    return true;
}

function getGeofences() {
    "use strict";
    $.ajax({
        url: "http://localhost:8080/api/geofences",
        type: "GET",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            var options = $("#geofence");
            $.each(data, function() {
                options.append($("<option />").val(this.id).text(this.properties.name));
            });
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });
}

function getGeofence(id) {
    "use strict";
    var geofence;
    $.ajax({
        url: "http://localhost:8080/api/geofences/"+id,
        type: "GET",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        async: false,
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            geofence = data;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });
    return geofence;
}

function postRule(ruleData) {
    "use strict";
    $.ajax({
        url: "http://localhost:8080/api/rules",
        type: "POST",
        data: JSON.stringify(ruleData),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
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

$("#ruleForm").submit(function (event) {
    event.preventDefault();

    var $form = $(this);
    var enabled = $('#enabled option:selected').text();
    if (enabled === "Enabled") {
        enabled = true;
    } else {
        enabled = false;
    }
    var message = $form.find('input[name="message"]').val();
    if (message !== "" && message !== 'undefined') {
        var geofence_id = $('#geofence option:selected').val();
        var geofence = getGeofence(geofence_id);
        var ruleData = {
            enabled: enabled,
            type: $('#type option:selected').text(),
            time: $form.find('input[name="time"]').val(),
            message: message,
            days: [],
            notifications: [],
            geofence: geofence
        }

    postRule(ruleData);
    }
});