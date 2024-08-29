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
import com.rsmaxwell.diaries.common.config.Config;
import com.rsmaxwell.diaries.common.config.MqttConfig;
import com.rsmaxwell.diaries.common.config.User;
import com.rsmaxwell.diaries.request.state.State;
import com.rsmaxwell.mqtt.rpc.common.Request;
import com.rsmaxwell.mqtt.rpc.common.Response;
import com.rsmaxwell.mqtt.rpc.request.RemoteProcedureCall;

public class SignInRequest {

	private static final Logger log = LogManager.getLogger(SignInRequest.class);

	static final int qos = 0;
	static final String clientID = "requester";
	static final String requestTopic = "request";

	static private ObjectMapper mapper = new ObjectMapper();

	static Option createOption(String shortName, String longName, String argName, String description, boolean required) {
		return Option.builder(shortName).longOpt(longName).argName(argName).desc(description).hasArg().required(required).build();
	}

	public static void main(String[] args) throws Exception {

		Option configOption = createOption("c", "config", "Configuration", "Configuration", true);
		Option usernameOption = createOption("u", "username", "Username", "Username", true);
		Option passwordOption = createOption("p", "password", "Password", "Password", true);

		// @formatter:off
		Options options = new Options();
		options.addOption(configOption)
	           .addOption(usernameOption)
			   .addOption(passwordOption);
		// @formatter:on

		CommandLineParser commandLineParser = new DefaultParser();
		CommandLine commandLine = commandLineParser.parse(options, args);

		String filename = commandLine.getOptionValue("config");
		Config config = Config.read(filename);
		MqttConfig mqtt = config.getMqtt();
		String server = mqtt.getServer();
		User user = mqtt.getUser();

		// Connect
		MqttConnectionOptions connOpts = new MqttConnectionOptions();
		connOpts.setUserName(user.getUsername());
		connOpts.setPassword(user.getPassword().getBytes());

		MqttClientPersistence persistence = new MqttDefaultFilePersistence();
		MqttAsyncClient client = new MqttAsyncClient(server, clientID, persistence);
		RemoteProcedureCall rpc = new RemoteProcedureCall(client, String.format("response/%s", clientID));

		log.debug(String.format("Connecting to broker: %s as '%s'", server, clientID));
		client.connect(connOpts).waitForCompletion();
		rpc.subscribeToResponseTopic();

		// Make a request
		Request request = new Request("signin");
		request.put("username", commandLine.getOptionValue("username"));
		request.put("password", commandLine.getOptionValue("password"));

		// Send the request as a JSON string
		byte[] bytes = mapper.writeValueAsBytes(request);
		Response response = rpc.request(requestTopic, bytes).waitForResponse();

		// Handle the response
		if (response.isok()) {
			log.info(String.format("'%s' is signed-in", user.getUsername()));

			String accessToken = response.getString("accessToken");
			String refreshToken = response.getString("refreshToken");

			log.info(String.format("accessToken:  %s", accessToken));
			log.info(String.format("refreshToken: %s", refreshToken));

			State state = new State(accessToken, refreshToken);
			state.write();

		} else {
			log.info(String.format("error response: code: %d, message: %s", response.getCode(), response.getMessage()));
		}

		// Disconnect
		client.disconnect().waitForCompletion();
		log.debug(String.format("Client %s disconnected", clientID));
		log.info("Success");
	}
}
