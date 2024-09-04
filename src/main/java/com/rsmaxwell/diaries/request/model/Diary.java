package com.rsmaxwell.diaries.request.model;

import java.util.Map;

import lombok.Data;

@Data
public class Diary {

	private Long id;
	private String name;

	public Diary(Map<?, ?> map) throws Exception {

		// Extract the 'id' from the map
		Object id_object = map.get("id");
		if (id_object == null) {
			throw new Exception("'id' not found");
		}
		if (!(id_object instanceof Number)) {
			throw new Exception(String.format("Unexpected type: %s", id_object.getClass().getSimpleName()));
		}
		Number id_number = (Number) id_object;
		this.id = id_number.longValue();

		// Extract the 'name' from the map
		Object name_object = map.get("name");
		if (name_object == null) {
			throw new Exception("'name' not found");
		}
		if (!(name_object instanceof String)) {
			throw new Exception(String.format("Unexpected type: %s", name_object.getClass().getSimpleName()));
		}
		this.name = (String) name_object;
	}
}
