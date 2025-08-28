/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.McpConfigField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP配置VO类 - 完全基于注解的配置系统
 *
 * 特性： 1. 新增字段只需定义字段名和注解，无需修改任何代码 2. toJson、fromJson、构造函数等所有功能自动支持新字段 3. 支持动态字段类型转换（String
 * ↔ List/Map） 4. 支持条件显示和验证规则
 *
 * 使用方法： 1. 定义字段：private String myField; 2. 添加注解：@McpConfigField(...) 3.
 * 添加getter/setter（可选，推荐添加） 4. 所有功能自动可用！
 */
public class McpConfigVO {

	// ==================== 基础字段定义 ====================
	// 新增字段时，只需在这里定义字段和注解即可！

	@McpConfigField(dbField = "id", jsonName = "id", displayName = "配置ID",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.NUMBER,
			displayRequired = false, businessRequired = false, displayOrder = 1, displayCondition = "NEVER")
	@JsonProperty("id")
	protected Long id;

	@McpConfigField(dbField = "mcp_server_name", jsonName = "mcpServerName", displayName = "MCP服务器名称",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.TEXT,
			displayRequired = true, businessRequired = true, displayValidation = "minLength:5,maxLength:20",
			displayOrder = 2)
	@JsonProperty("mcpServerName")
	protected String mcpServerName;

	@McpConfigField(dbField = "connection_type", jsonName = "connectionType", displayName = "连接类型",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.SELECT,
			displayRequired = true, businessRequired = true, displayOptions = { "STUDIO", "SSE", "STREAMING" },
			displayOrder = 3)
	@JsonProperty("connectionType")
	protected String connectionType;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "command", jsonName = "command", displayName = "执行命令",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.TEXT,
			displayRequired = false, businessRequired = false, displayDescription = "STUDIO类型必须提供执行命令",
			displayOrder = 4, displayCondition = "connectionType=STUDIO",
			displayRequiredCondition = "connectionType=STUDIO")
	@JsonProperty("command")
	protected String command;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "url", jsonName = "url", displayName = "服务器URL",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.URL,
			displayRequired = false, businessRequired = false, displayDescription = "SSE/STREAMING类型必须提供URL",
			displayOrder = 5, displayCondition = "connectionType=SSE,STREAMING",
			displayRequiredCondition = "connectionType=SSE,STREAMING")
	@JsonProperty("url")
	protected String url;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "args", jsonName = "args", displayName = "命令行参数",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.TEXTAREA,
			displayRequired = false, businessRequired = false, displayDescription = "可选的命令行参数列表，每行一个参数",
			displayOrder = 6, displayCondition = "connectionType=STUDIO")
	@JsonProperty("args")
	protected String args;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "env", jsonName = "env", displayName = "环境变量",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.TEXTAREA,
			displayRequired = false, businessRequired = false, displayDescription = "环境变量配置，格式：key:value，每行一个",
			displayOrder = 7, displayCondition = "connectionType=STUDIO")
	@JsonProperty("env")
	protected String env;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "timeout", jsonName = "timeout", displayName = "超时时间(秒)",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.NUMBER,
			displayRequired = false, businessRequired = false, jsonDefaultValue = "30",
			displayValidation = "min:1,max:3600", displayOrder = 8)
	@JsonProperty("timeout")
	protected Integer timeout;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "retry_count", jsonName = "retryCount",
			displayName = "重试次数",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.NUMBER,
			displayRequired = false, businessRequired = false, jsonDefaultValue = "3",
			displayValidation = "min:0,max:10", displayOrder = 9)
	@JsonProperty("retryCount")
	protected Integer retryCount;

	@McpConfigField(dbField = "connection_config", dbJsonKey = "headers", jsonName = "headers", displayName = "HTTP请求头",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.TEXTAREA,
			displayRequired = false, businessRequired = false, displayDescription = "HTTP请求头配置，格式：key:value，每行一个",
			displayOrder = 10)
	@JsonProperty("headers")
	protected String headers;

	@McpConfigField(dbField = "status", jsonName = "status", displayName = "状态",
			displayType = com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType.SELECT,
			displayRequired = false, businessRequired = true, displayCondition = "NEVER", jsonDefaultValue = "ENABLE",
			displayOrder = 11)
	@JsonProperty("status")
	protected String status = "ENABLE";

	// ==================== 核心功能实现 ====================
	// 以下代码无需修改，自动支持所有新字段！

	protected final ObjectMapper objectMapper;

	protected final Map<String, Object> dynamicFields = new HashMap<>();

	/**
	 * 默认构造函数 - 自动初始化所有字段的默认值
	 */
	public McpConfigVO() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());

		// 自动设置所有字段的默认值
		initializeDefaultValues();
	}

	/**
	 * 带ObjectMapper的构造函数
	 */
	public McpConfigVO(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		initializeDefaultValues();
	}

	/**
	 * 从Entity构造VO - 自动映射所有字段
	 */
	public McpConfigVO(Object entity, ObjectMapper objectMapper) {
		this(objectMapper);

		if (entity != null) {
			try {
				// 使用反射自动映射所有字段
				mapEntityToVO(entity);
			}
			catch (Exception e) {
				System.err.println("Failed to create VO from entity: " + e.getMessage());
			}
		}
	}

	/**
	 * 自动初始化所有字段的默认值
	 */
	private void initializeDefaultValues() {
		// 获取所有带注解的字段
		List<Field> annotatedFields = getAnnotatedFields();

		for (Field field : annotatedFields) {
			McpConfigField annotation = field.getAnnotation(McpConfigField.class);
			if (annotation != null && !annotation.jsonDefaultValue().isEmpty()) {
				try {
					field.setAccessible(true);

					// 根据字段类型设置默认值
					Object defaultValue = parseDefaultValue(field.getType(), annotation.jsonDefaultValue());
					if (defaultValue != null) {
						field.set(this, defaultValue);
					}
				}
				catch (Exception e) {
					System.err
						.println("Failed to set default value for field " + field.getName() + ": " + e.getMessage());
				}
			}
		}

		// 设置特殊字段的默认值
		if (this.args == null)
			this.args = "";
		if (this.env == null)
			this.env = "";
		if (this.headers == null)
			this.headers = "";
	}

	/**
	 * 自动映射Entity到VO
	 */
	private void mapEntityToVO(Object entity) throws Exception {
		Class<?> entityClass = entity.getClass();

		// 获取所有带注解的字段
		List<Field> annotatedFields = getAnnotatedFields();

		for (Field field : annotatedFields) {
			McpConfigField annotation = field.getAnnotation(McpConfigField.class);
			if (annotation != null) {
				try {
					field.setAccessible(true);

					if (annotation
						.storageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.DIRECT) {
						// 直接字段映射
						Object value = getFieldValue(entity, entityClass, field.getName());
						if (value != null) {
							field.set(this, value);
						}
					}
					else if (annotation
						.storageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.JSON_KEY) {
						// JSON字段映射
						Object connectionConfig = getFieldValue(entity, entityClass, "connectionConfig");
						if (connectionConfig != null && !connectionConfig.toString().trim().isEmpty()) {
							// 解析JSON配置
							Map<String, Object> configMap = parseJsonToMap(connectionConfig.toString());
							String key = annotation.dbJsonKey().isEmpty() ? annotation.jsonName()
									: annotation.dbJsonKey();

							if (configMap.containsKey(key)) {
								Object value = configMap.get(key);
								Object convertedValue = convertValueForField(field, value);
								if (convertedValue != null) {
									field.set(this, convertedValue);
								}
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println("Failed to map field " + field.getName() + ": " + e.getMessage());
				}
			}
		}
	}

	/**
	 * 基于注解动态生成JSON字符串 - 自动包含所有字段
	 */
	public String toJson() throws JsonProcessingException {
		Map<String, Object> jsonMap = new HashMap<>();

		// 获取所有带注解的字段
		List<Field> annotatedFields = getAnnotatedFields();

		for (Field field : annotatedFields) {
			McpConfigField annotation = field.getAnnotation(McpConfigField.class);
			if (annotation != null) {
				try {
					field.setAccessible(true);
					Object value = field.get(this);

					if (value != null) {
						String jsonKey = annotation.jsonName();

						if (annotation
							.storageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.DIRECT) {
							// 直接字段
							jsonMap.put(jsonKey, value);
						}
						else if (annotation
							.storageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.JSON_KEY) {
							// JSON字段
							String finalKey = annotation.dbJsonKey().isEmpty() ? jsonKey : annotation.dbJsonKey();
							Object processedValue = processFieldValueForJson(field, value);
							if (processedValue != null) {
								jsonMap.put(finalKey, processedValue);
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println("Failed to serialize field " + field.getName() + ": " + e.getMessage());
				}
			}
		}

		return objectMapper.writeValueAsString(jsonMap);
	}

	/**
	 * 基于注解动态解析JSON字符串 - 自动解析所有字段
	 */
	public void fromJson(String json) throws JsonProcessingException {
		if (json == null || json.trim().isEmpty()) {
			return;
		}

		JsonNode configNode = objectMapper.readTree(json);

		// 获取所有带注解的字段
		List<Field> annotatedFields = getAnnotatedFields();

		for (Field field : annotatedFields) {
			McpConfigField annotation = field.getAnnotation(McpConfigField.class);
			if (annotation != null) {
				try {
					field.setAccessible(true);

					if (annotation
						.storageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.JSON_KEY) {
						String key = annotation.dbJsonKey().isEmpty() ? annotation.jsonName() : annotation.dbJsonKey();

						if (configNode.has(key)) {
							JsonNode valueNode = configNode.get(key);
							Object value = parseJsonValueForField(field, valueNode);
							if (value != null) {
								field.set(this, value);
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println("Failed to parse field " + field.getName() + ": " + e.getMessage());
				}
			}
		}
	}

	// ==================== 辅助方法 ====================
	// 以下方法无需修改，自动支持所有新字段！

	/**
	 * 获取所有带McpConfigField注解的字段
	 */
	private List<Field> getAnnotatedFields() {
		List<Field> annotatedFields = new ArrayList<>();
		Class<?> currentClass = this.getClass();

		while (currentClass != null && currentClass != Object.class) {
			Field[] fields = currentClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(McpConfigField.class)) {
					annotatedFields.add(field);
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return annotatedFields;
	}

	/**
	 * 处理字段值用于JSON序列化
	 */
	private Object processFieldValueForJson(Field field, Object value) {
		if (value == null) {
			return null;
		}

		Class<?> fieldType = field.getType();

		// 处理字符串类型的特殊字段
		if (fieldType == String.class) {
			String stringValue = (String) value;

			// 根据字段名自动判断转换方式
			String fieldName = field.getName().toLowerCase();

			if (fieldName.contains("args") && !stringValue.isEmpty()) {
				return Arrays.asList(stringValue.split("\n"));
			}

			if ((fieldName.contains("env") || fieldName.contains("headers")) && !stringValue.isEmpty()) {
				Map<String, String> map = new HashMap<>();
				String[] lines = stringValue.split("\n");
				for (String line : lines) {
					if (line.contains(":")) {
						String[] parts = line.split(":", 2);
						if (parts.length == 2) {
							map.put(parts[0].trim(), parts[1].trim());
						}
					}
				}
				return map;
			}
		}

		return value;
	}

	/**
	 * 解析JSON值到字段类型
	 */
	private Object parseJsonValueForField(Field field, JsonNode valueNode) {
		Class<?> fieldType = field.getType();

		try {
			if (fieldType == String.class) {
				if (valueNode.isArray()) {
					List<String> list = objectMapper.readValue(valueNode.toString(),
							objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
					return String.join("\n", list);
				}
				else if (valueNode.isObject()) {
					Map<String, String> map = objectMapper.readValue(valueNode.toString(),
							objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
					return map.entrySet()
						.stream()
						.map(entry -> entry.getKey() + ":" + entry.getValue())
						.collect(Collectors.joining("\n"));
				}
				else {
					return valueNode.asText();
				}
			}
			else if (fieldType == Integer.class || fieldType == int.class) {
				return valueNode.asInt();
			}
			else if (fieldType == Long.class || fieldType == long.class) {
				return valueNode.asLong();
			}
			else if (fieldType == Boolean.class || fieldType == boolean.class) {
				return valueNode.asBoolean();
			}
			else if (fieldType == Double.class || fieldType == double.class) {
				return valueNode.asDouble();
			}
			else {
				return objectMapper.readValue(valueNode.toString(), fieldType);
			}
		}
		catch (Exception e) {
			System.err.println("Failed to parse field value for " + field.getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * 解析默认值
	 */
	private Object parseDefaultValue(Class<?> fieldType, String defaultValue) {
		try {
			if (fieldType == String.class) {
				return defaultValue;
			}
			else if (fieldType == Integer.class || fieldType == int.class) {
				return Integer.parseInt(defaultValue);
			}
			else if (fieldType == Long.class || fieldType == long.class) {
				return Long.parseLong(defaultValue);
			}
			else if (fieldType == Boolean.class || fieldType == boolean.class) {
				return Boolean.parseBoolean(defaultValue);
			}
			else if (fieldType == Double.class || fieldType == double.class) {
				return Double.parseDouble(defaultValue);
			}
		}
		catch (Exception e) {
			System.err.println("Failed to parse default value: " + defaultValue + " for type: " + fieldType);
		}
		return null;
	}

	/**
	 * 转换值到字段类型
	 */
	private Object convertValueForField(Field field, Object value) {
		if (value == null) {
			return null;
		}

		Class<?> fieldType = field.getType();

		try {
			if (value.getClass() == fieldType) {
				return value;
			}
			else if (fieldType == String.class) {
				return value.toString();
			}
			else if (fieldType == Integer.class || fieldType == int.class) {
				return Integer.valueOf(value.toString());
			}
			else if (fieldType == Long.class || fieldType == long.class) {
				return Long.valueOf(value.toString());
			}
			else if (fieldType == Boolean.class || fieldType == boolean.class) {
				return Boolean.valueOf(value.toString());
			}
			else if (fieldType == Double.class || fieldType == double.class) {
				return Double.valueOf(value.toString());
			}
		}
		catch (Exception e) {
			System.err.println("Failed to convert value for field " + field.getName() + ": " + e.getMessage());
		}
		return null;
	}

	/**
	 * 获取字段值
	 */
	private Object getFieldValue(Object entity, Class<?> entityClass, String fieldName) {
		try {
			Field field = entityClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(entity);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * 解析JSON到Map
	 */
	private Map<String, Object> parseJsonToMap(String json) {
		try {
			return objectMapper.readValue(json, Map.class);
		}
		catch (Exception e) {
			return new HashMap<>();
		}
	}

	// ==================== 业务逻辑方法 ====================
	// 以下方法无需修改，自动支持所有新字段！

	/**
	 * 获取连接类型 - 自动判断
	 */
	public String getConnectionType() {
		if (command != null && !command.isEmpty()) {
			return "STUDIO";
		}
		if (url != null && !url.isEmpty() && isSSEUrl(url)) {
			return "SSE";
		}
		return "STREAMING";
	}

	/**
	 * 判断是否为SSE URL
	 */
	private boolean isSSEUrl(String url) {
		if (url == null || url.isEmpty()) {
			return false;
		}
		try {
			java.net.URL parsedUrl = new java.net.URL(url);
			String path = parsedUrl.getPath();
			return path != null && path.toLowerCase().contains("sse");
		}
		catch (java.net.MalformedURLException e) {
			return false;
		}
	}

	/**
	 * 验证URL格式
	 */
	protected boolean isValidUrlFormat(String url) {
		if (url == null || url.trim().isEmpty()) {
			return false;
		}
		try {
			new java.net.URL(url.trim());
			return true;
		}
		catch (java.net.MalformedURLException e) {
			return false;
		}
	}

	/**
	 * 静态方法：从Entity列表创建VO列表
	 */
	public static List<McpConfigVO> fromEntities(List<?> entities, ObjectMapper objectMapper) {
		List<McpConfigVO> vos = new ArrayList<>();
		if (entities != null) {
			for (Object entity : entities) {
				vos.add(new McpConfigVO(entity, objectMapper));
			}
		}
		return vos;
	}

	// ==================== 兼容方法 ====================
	// 为了向后兼容，提供List和Map类型的方法

	/**
	 * 兼容方法：获取args作为List
	 */
	public List<String> getArgsAsList() {
		if (args == null || args.trim().isEmpty()) {
			return new ArrayList<>();
		}
		return Arrays.asList(args.split("\n"));
	}

	/**
	 * 兼容方法：设置args为List
	 */
	public void setArgsAsList(List<String> argsList) {
		if (argsList == null || argsList.isEmpty()) {
			this.args = "";
		}
		else {
			this.args = String.join("\n", argsList);
		}
	}

	/**
	 * 兼容方法：获取env作为Map
	 */
	public Map<String, String> getEnvAsMap() {
		Map<String, String> envMap = new HashMap<>();
		if (env == null || env.trim().isEmpty()) {
			return envMap;
		}
		String[] lines = env.split("\n");
		for (String line : lines) {
			if (line.contains(":")) {
				String[] parts = line.split(":", 2);
				if (parts.length == 2) {
					envMap.put(parts[0].trim(), parts[1].trim());
				}
			}
		}
		return envMap;
	}

	/**
	 * 兼容方法：设置env为Map
	 */
	public void setEnvAsMap(Map<String, String> envMap) {
		if (envMap == null || envMap.isEmpty()) {
			this.env = "";
		}
		else {
			this.env = envMap.entrySet()
				.stream()
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.joining("\n"));
		}
	}

	// ==================== Getters and Setters ====================
	// 所有字段的getter和setter方法

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMcpServerName() {
		return mcpServerName;
	}

	public void setMcpServerName(String mcpServerName) {
		this.mcpServerName = mcpServerName;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
