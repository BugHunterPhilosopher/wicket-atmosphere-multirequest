jQuery(document).ready(function() {
	initialize();
});

var subscribePath = "atmosphere/subscribe/";
var connectedEndpointJob1 = null;
var connectedEndpointJob2 = null;
var transport = "polling";
var nbMessages = 0;

function initialize() {
		connectedEndpointJob1 = subscribeUrl("job1", callbackJob1, transport);
		connectedEndpointJob2 = subscribeUrl("job2", callbackJob2, transport);
}

function sendMessage(connectedEndpoint, jobName) {
	var phrase = jQuery('#cometStart').val();
	connectedEndpoint.push({data: "message=" + phrase});
}
function subscribeUrl(jobName, call, transport) {
	var location = subscribePath + jobName;
	return subscribeAtmosphere(location, call, transport);
}

function subscribeAtmosphere(location, call, transport) {
	var rq = jQuery.atmosphere.subscribe(location, globalCallback, jQuery.atmosphere.request = {
		logLevel : 'info',
		transport : transport,
		callback : call
	});
	return rq;
}

function globalCallback(response) {
	if (response.state != "messageReceived") {
		return;
	}
	nbMessages++;
	jQuery('#nbMessages').html(nbMessages);
}

function callbackJob1(response) {
	console.log("Call to callbackJob1");
	if (response.state != "messageReceived") {
		return;
	}
	var data = getDataFromResponse(response);
	if (data != null) {
		jQuery('#clock').html(data);
	}
}

function callbackJob2(response) {
	console.log("Call to callbackJob2");
	if (response.state != "messageReceived") {
		return;
	}
	var data = getDataFromResponse(response);
	if (data != null) {
		jQuery('#lst-job2').append('<li>' + data + '</li>');
	}
}

function getDataFromResponse(response) {
	var detectedTransport = response.transport;
	console.log("[DEBUG] Real transport is <" + detectedTransport + ">");
	if (response.transport != 'polling' && response.state != 'connected' && response.state != 'closed') {
		if (response.status == 200) {
			return response.responseBody;
		}
	}
	return null;
}

