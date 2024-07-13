package com.rsmaxwell.diaries.request;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmaxwell.mqtt.rpc.common.Request;
import com.rsmaxwell.mqtt.rpc.common.Response;
import com.rsmaxwell.mqtt.rpc.request.RemoteProcedureCall;
import com.rsmaxwell.mqtt.rpc.request.Token;

public class QuitRequest {

	private static final Logger logger = LogManager.getLogger(QuitRequest.class);

	static int qos = 0;
	static volatile boolean keepRunning = true;

	static private ObjectMapper mapper = new ObjectMapper();

	static Option createOption(String shortName, String longName, String argName, String description, boolean required) {
		return Option.builder(shortName).longOpt(longName).argName(argName).desc(description).hasArg().required(required).build();
	}

	public static void main(String[] args) throws Exception {

		Option serverOption = createOption("s", "server", "mqtt server", "URL of MQTT server", false);
		Option usernameOption = createOption("u", "username", "Username", "Username for the MQTT server", true);
		Option passwordOption = createOption("p", "password", "Password", "Password for the MQTT server", true);

		// @formatter:off
		Options options = new Options();
		options.addOption(serverOption)
			   .addOption(usernameOption)
			   .addOption(passwordOption);
		// @formatter:on

		CommandLineParser commandLineParser = new DefaultParser();
		CommandLine commandLine = commandLineParser.parse(options, args);
		String server = commandLine.hasOption("h") ? commandLine.getOptionValue(serverOption) : "tcp://127.0.0.1:1883";
		String username = commandLine.getOptionValue(usernameOption);
		String password = commandLine.getOptionValue(passwordOption);

		String clientID = "requester";
		String requestTopic = "request";

		MqttClientPersistence persistence = new MqttDefaultFilePersistence();
		MqttAsyncClient client = new MqttAsyncClient(server, clientID, persistence);
		MqttConnectionOptions connOpts = new MqttConnectionOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password.getBytes());

		// Make an RPC instance
		RemoteProcedureCall rpc = new RemoteProcedureCall(client, String.format("response/%s", clientID));

		// Connect
		logger.debug(String.format("Connecting to broker: %s as '%s'", server, clientID));
		client.connect(connOpts).waitForCompletion();
		logger.debug(String.format("Client %s connected", clientID));

		// Subscribe to the responseTopic
		rpc.subscribeToResponseTopic();

		// Make a request
		Request request = new Request("quit");
		request.put("quit", true);

		// Send the request as a json string
		byte[] bytes = mapper.writeValueAsBytes(request);
		Token token = rpc.request(requestTopic, bytes);

		// Wait for the response to arrive
		Response response = token.waitForResponse();

		// Handle the response
		if (response.ok()) {
			logger.info("Responder is Quitting");
		} else {
			logger.info(String.format("error response: code: %d, message: %s", response.getCode(), response.getMessage()));
		}

		// Disconnect
		client.disconnect().waitForCompletion();
		logger.debug(String.format("Client %s disconnected", clientID));
		logger.debug("exiting");
	}
}
