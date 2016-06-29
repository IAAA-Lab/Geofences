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

var mapOptions = {
    zoom : 2,
    center : new google.maps.LatLng(40.46366700000001,
        -3.7492200000000366),
    mapTypeControlOptions : {
        style : google.maps.MapTypeControlStyle.DROPDOWN_MENU
    },
    mapTypeId : google.maps.MapTypeId.ROADMAP,
    zoomControlOptions : {
        style : google.maps.ZoomControlStyle.SMALL
    }
};

var map = new google.maps.Map(document.getElementById('map-canvas'),
    mapOptions);

function findUserCurrentLocation(callback) {
    "use strict";
    navigator.geolocation.getCurrentPosition(function (position) {
        var latitude = position.coords.latitude;
        var longitude = position.coords.longitude;
        console.log('latitude .. ' + latitude);
        console.log('longitude .. ' + longitude);
        var location = [latitude, longitude];
        callback(location);
    }, function (e) {
        switch (e.code) {
        case e.PERMISSION_DENIED:
            alert('You have denied access to your position. You will ' +
                  'not get the most out of the application now.');
            break;
        case e.POSITION_UNAVAILABLE:
            alert('There was a problem getting your position.');
            break;
        case e.TIMEOUT:
            alert('The application has timed out attempting to get ' +
                  'your location.');
            break;
        default:
            alert('There was a horrible Geolocation error that has ' +
                  'not been defined.');
        }
    }, { timeout: 45000 });
}

var geofencesArray = [];
var drawingShape;
var marker;

function renderMessageOnMap(data) {
    "use strict";
    if (typeof marker !== 'undefined') {
        marker.setMap(null);
    }
    var latLng = new google.maps.LatLng(data[0], data[1]);
    map.setCenter(latLng);
    marker = new google.maps.Marker({
        position : latLng,
        animation : google.maps.Animation.DROP,
        html : "<p>You are here</p>"
    });

    marker.setMap(map);
    map.setCenter(latLng);
    map.setZoom(14);
}

function drawing() {
    "use strict";
    var drawingManager = new google.maps.drawing.DrawingManager({
        drawingControl: true,
        drawingControlOptions: {
            position: google.maps.ControlPosition.TOP_CENTER,
            drawingModes: [
                google.maps.drawing.OverlayType.CIRCLE,
                google.maps.drawing.OverlayType.POLYGON,
                google.maps.drawing.OverlayType.RECTANGLE
            ]
        },
        circleOptions: {
            strokeColor: '#20B2AA',
            strokeOpacity: 0.8,
            fillColor: '#20B2AA',
            fillOpacity: 0.35,
            strokeWeight: 2,
            clickable: true,
            editable: true,
            zIndex: 1
        },
        polygonOptions: {
            strokeColor: '#20B2AA',
            strokeOpacity: 0.8,
            fillColor: '#20B2AA',
            fillOpacity: 0.35,
            strokeWeight: 2,
            clickable: true,
            editable: true,
            zIndex: 1
        },
        rectangleOptions: {
            strokeColor: '#20B2AA',
            strokeOpacity: 0.8,
            fillColor: '#20B2AA',
            fillOpacity: 0.35,
            strokeWeight: 2,
            clickable: true,
            editable: true,
            zIndex: 1
        }
    });
    drawingManager.setMap(map);

    google.maps.event.addListener(drawingManager, "overlaycomplete", function(event) {
        var lat, lng, coordinates, radius, radiusLatDeg, radiusLongDeg, len, init, i, latNorth, latSouth, lngNorth, lngSouth;
        if (typeof drawingShape !== 'undefined') {
            drawingShape.setMap(null);
        }
        drawingShape = event.overlay;
        if (event.type === google.maps.drawing.OverlayType.CIRCLE) {
            overlayClickListenerCircle(event.overlay);
            lat = event.overlay.getCenter().lat();
            lng = event.overlay.getCenter().lng();
            radius = event.overlay.getRadius() / 1000;
            radiusLatDeg = (1 / 110.574) * radius;
            radiusLongDeg = (1 / (121 * Math.cos(lat))) * radius;
            coordinates = circle(lat - radiusLatDeg, lng - radiusLongDeg, lat + radiusLatDeg, lng + radiusLongDeg, 40);
            console.log(coordinates);
            $('#vertices').val(JSON.stringify([coordinates]));
        } else if (event.type === google.maps.drawing.OverlayType.POLYGON) {
            overlayClickListenerPolygon(event.overlay);
            len = event.overlay.getPath().getLength();
            coordinates = [];
            init = null;
            for (i = 0; i < len; i++) {
                lat = event.overlay.getPath().getAt(i).lat();
                lng = event.overlay.getPath().getAt(i).lng();
                if (i === 0) {
                    init = [lat, lng];
                }
                coordinates.push([lat, lng]);
            }
            coordinates.push(init);
            $('#vertices').val(JSON.stringify([coordinates]));
        } else if (event.type == google.maps.drawing.OverlayType.RECTANGLE) {
            overlayClickListenerRectangle(event.overlay);
            coordinates = [];
            latNorth = event.overlay.getBounds().getNorthEast().lat();
            latSouth = event.overlay.getBounds().getSouthWest().lat();
            lngNorth = event.overlay.getBounds().getNorthEast().lng();
            lngSouth = event.overlay.getBounds().getSouthWest().lng();
            coordinates.push([latNorth, lngNorth]);
            coordinates.push([latNorth, lngSouth]);
            coordinates.push([latSouth, lngSouth]);
            coordinates.push([latSouth, lngNorth]);
            coordinates.push([latNorth, lngNorth]);
            $('#vertices').val(JSON.stringify([coordinates]));
        }
    });
}

function overlayClickListenerCircle(overlay) {
    "use strict";
    var lat, lng, coordinates, radius, radiusLatDeg, radiusLongDeg;
    google.maps.event.addListener(overlay, "click", function(event){
        lat = overlay.getCenter().lat();
        lng = overlay.getCenter().lng();
        radius = overlay.getRadius() / 1000;
        radiusLatDeg = (1 / 110.574) * radius;
        radiusLongDeg = (1 / (121 * Math.cos(lat))) * radius;
        coordinates = circle(lat - radiusLatDeg, lng - radiusLongDeg, lat + radiusLatDeg, lng + radiusLongDeg, 40);
        $('#vertices').val(JSON.stringify([coordinates]));
    });
}

function overlayClickListenerPolygon(overlay) {
    "use strict";
    var lat, lng, coordinates, len, init, i;
    google.maps.event.addListener(overlay, "click", function (event) {
        len = overlay.getPath().getLength();
        coordinates = [];
        init = null;
        for (i = 0; i < len; i++) {
            lat = overlay.getPath().getAt(i).lat();
            lng = overlay.getPath().getAt(i).lng();
            if(i === 0) {
                init = [lat, lng];
            }
            coordinates.push([lat, lng]);
        }
        coordinates.push(init);
        $('#vertices').val(JSON.stringify([coordinates]));
    });
}

function overlayClickListenerRectangle(overlay) {
    "use strict";
    var latNorth, latSouth, lngNorth, lngSouth, coordinates;
    google.maps.event.addListener(overlay, "click", function (event) {
        coordinates = [];
        latNorth = overlay.getBounds().getNorthEast().lat();
        latSouth = overlay.getBounds().getSouthWest().lat();
        lngNorth = overlay.getBounds().getNorthEast().lng();
        lngSouth = overlay.getBounds().getSouthWest().lng();
        coordinates.push([latNorth, lngNorth]);
        coordinates.push([latNorth, lngSouth]);
        coordinates.push([latSouth, lngSouth]);
        coordinates.push([latSouth, lngNorth]);
        coordinates.push([latNorth, lngNorth]);
        $('#vertices').val(JSON.stringify([coordinates]));
    });
}

function circle(x1, y1, x2, y2, nsides) {
    "use strict";
    var lat, lng;
    var coordinates = [];
    var init = null;
    var rx = Math.abs(x2 - x1) / 2;
    var ry = Math.abs(y2 - y1) / 2;
    var cx = Math.min(x1, x2) + rx;
    var cy = Math.min(y1, y2) + ry;

    var angInc = 2 * Math.PI / nsides;
    // create ring in CW order
    for (var i = 0; i < nsides; i++) {
        var ang = -(i * angInc);
        lat = cx + rx * Math.cos(ang);
        lng = cy + ry * Math.sin(ang);
        if(i === 0) {
            init = [lat, lng];
        }
        coordinates.push([lat, lng]);
    }
    coordinates.push(init);
    return coordinates;
}

function clearGeofences() {
    "use strict";
    for (var i = 0; i < geofencesArray.length; i++ ) {
        geofencesArray[i].setMap(null);
    }
    geofencesArray.length = 0;
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
            geofencesArray = [];
            var i, j;
            for (i = 0; i < data.length; i++) {
                var geo = [];
                var coordinates = data[i].geometry.coordinates[0];
                var id = data[i].id;
                for (j = 0; j < coordinates.length; j++) {
                    geo.push({lat: coordinates[j][0], lng: coordinates[j][1]});
                }
                // Construct the polygon.
                var polygon = new google.maps.Polygon({
                    paths: geo,
                    strokeColor: '#20B2AA',
                    strokeOpacity: 0.8,
                    strokeWeight: 2,
                    fillColor: '#20B2AA',
                    fillOpacity: 0.35  });
                polygon.id = id;
                geofencesArray.push(polygon);
                polygon.setMap(map);
                addListenersOnPolygon(polygon);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });
}

var addListenersOnPolygon = function(polygon) {
    google.maps.event.addListener(polygon, 'click', function (event) {
        if (window.confirm('Do you want to delete it?')) {
            deleteGeofence(polygon.id);
        }
    });
}

function starting() {
    "use strict";
    drawing();
    findUserCurrentLocation(renderMessageOnMap);
    getGeofences();
}

$("#mapForm").submit(function (event) {
    "use strict";
    event.preventDefault();

    var $form = $(this);
    var name = $form.find('input[name="name"]').val();
    console.log(name);
    if (name !== "" && name !== "undefined") {
        var geofence = $form.find('input[name="vertices"]').val();
        var geofenceData = {
            id: null,
            type: "Feature",
            properties: {"name": name},
            geometry: {"type": "Polygon", "coordinates": JSON.parse(geofence)},
            user: null,
            rules: []
        };

        postGeofence(geofenceData);
    }
});

function postGeofence(geofenceData) {
    "use strict";
    $.ajax({
        url: "http://localhost:8080/api/geofences",
        type: "POST",
        data: JSON.stringify(geofenceData),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            drawingShape.setMap(null);
            clearGeofences();
            getGeofences();
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

function deleteGeofence(id) {
    "use strict";
    $.ajax({
        url: "http://localhost:8080/api/geofences/"+id,
        type: "DELETE",
        headers: createAuthorizationTokenHeader(),
        success: function (data, textStatus, jqXHR) {
            clearGeofences();
            getGeofences();
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
    "use strict";
    removeJwtToken();
}

$("#logout").click(doLogout);