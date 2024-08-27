package com.rsmaxwell.diaries.request.state;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class State {

	static private ObjectMapper mapper = new ObjectMapper();

	private String accessToken;
	private String refreshToken;

	private static Path getFilePath() {
		String home = System.getProperty("user.home");
		return Paths.get(home, ".diaries", "state.json");
	}

	public static State read() throws Exception {

		File file = getFilePath().toFile();

		if (!file.exists()) {
			throw new Exception(String.format("file not found: %s", file.getAbsolutePath()));
		}

		return mapper.readValue(file, State.class);
	}

	public String toJson() throws StreamReadException, DatabindException, IOException {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
	}

	public void write() throws StreamReadException, DatabindException, IOException {
		Files.write(getFilePath(), toJson().getBytes());
	}
}
