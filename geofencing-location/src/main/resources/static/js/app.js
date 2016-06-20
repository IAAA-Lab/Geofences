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

function getGeometries() {
    return localStorage.getItem(GEOFENCES_NAME);
}

function removeGeometries() {
    localStorage.removeItem(GEOFENCES_NAME);
}

var stompClient = null;

function connect() {
    var socket = new SockJS('http://localhost:8080/api/locations');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/positions',
            function(positions) {
                renderMessageOnMap(JSON.parse(positions.body));
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendLocation(location) {
    stompClient.send("/api/locations",
        {'content-type':'application/json'},
        JSON.stringify({ "authorization" : getJwtToken(), "position": { "coordinates": { "type" : "Point", "coordinates" : location }}}));
}

function starting() {
    removeGeometries();
    disconnect();
    connect();
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

	navigator.geolocation.getCurrentPosition(function(position){
        var latitude = position.coords.latitude;
        var longitude = position.coords.longitude;
        console.log('latitude .. '+latitude);
        console.log('longitude .. '+longitude);

        var location = new Array(latitude, longitude);
        callback(location);
    }, function(e){
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
    },
    { timeout: 45000 }

    );

}

var marker;

function renderMessageOnMap(data) {
    if (typeof marker !== 'undefined') {
        marker.setMap(null);
    }
	var latLng = new google.maps.LatLng(data.coordinates.coordinates[0], data.coordinates.coordinates[1]);
	map.setCenter(latLng);
	marker = new google.maps.Marker({
		position : latLng,
		animation : google.maps.Animation.DROP,
		html : "<p>You are here</p>"
	});

	marker.setMap(map);
	map.setCenter(latLng);
	map.setZoom(15);
}

function drawGeofences() {
    var geofences = JSON.parse(getGeometries());
    for (var i = 0; i < geofences.length; i++) {
        // Construct the polygon.
        var polygon = new google.maps.Polygon({
            paths: geofences[i],
            strokeColor: '#20B2AA',
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: '#20B2AA',
            fillOpacity: 0.35  });
        polygon.setMap(map);
    }
}

var timer = setTimeout(startingTimer, 5000);

function startingTimer() {
    findUserCurrentLocation(sendLocation);
    timer = setTimeout(startingTimer, 30000);
}

function abortTimer() {
    clearTimeout(timer);
}


setTimeout(checkVariable, 1000);

function checkVariable() {
    var geofences = JSON.parse(getGeometries());
    if (typeof geofences !== 'undefined' && geofences != null) {
        stopCheckingVariable();
        drawGeofences();
    } else {
        setTimeout(checkVariable, 1000);
    }
}

function stopCheckingVariable() {
    clearTimeout(checkVariable);
}