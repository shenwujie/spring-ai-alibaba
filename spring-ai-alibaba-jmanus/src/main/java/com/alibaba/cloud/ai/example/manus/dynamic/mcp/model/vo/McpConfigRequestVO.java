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
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * 统一的MCP配置请求类，支持单个和批量请求
 */
public class McpConfigRequestVO extends McpConfigVO {

	/**
	 * 请求类型枚举
	 */
	public enum RequestType {

		SINGLE, // 单个服务器配置
		BATCH // 批量服务器配置

	}

	/**
	 * 请求类型
	 */
	private RequestType requestType = RequestType.SINGLE;

	@McpConfigField(storageType = DbStorageType.VIRTUAL, jsonName = "configJson", displayName = "配置JSON",
			displayType = FieldType.JSON_EDITOR, displayRequired = true, businessRequired = true,
			displayDescription = "批量配置的JSON字符串", displayOrder = 12)
	private String configJson;

	@McpConfigField(storageType = DbStorageType.VIRTUAL, jsonName = "overwrite", displayName = "是否覆盖",
			displayType = FieldType.CHECKBOX, displayRequired = false, businessRequired = false,
			displayDescription = "是否覆盖已存在的配置", displayOrder = 13)
	private boolean overwrite = false;

	// 通用字段 - objectMapper从父类McpConfigBase继承

	/**
	 * 默认构造函数
	 */
	public McpConfigRequestVO() {
		super();
	}

	/**
	 * 静态工厂方法 - 创建单个请求
	 */
	public static McpConfigRequestVO single() {
		McpConfigRequestVO request = new McpConfigRequestVO();
		request.setRequestType(RequestType.SINGLE);
		return request;
	}

	/**
	 * 静态工厂方法 - 创建批量请求
	 */
	public static McpConfigRequestVO batch() {
		McpConfigRequestVO request = new McpConfigRequestVO();
		request.setRequestType(RequestType.BATCH);
		return request;
	}

	/**
	 * 验证方法 - 根据请求类型进行不同验证
	 */
	public List<String> validate() {
		if (requestType == RequestType.SINGLE) {
			return validateSingle();
		}
		else {
			return validateBatch();
		}
	}

	/**
	 * 单个请求验证
	 */
	private List<String> validateSingle() {
		List<String> errors = new ArrayList<>();

		if (mcpServerName == null || mcpServerName.trim().isEmpty()) {
			errors.add("MCP名称不能为空");
		}

		if (connectionType == null || connectionType.trim().isEmpty()) {
			errors.add("连接类型不能为空");
		}

		// 根据连接类型验证必填字段
		if (connectionType != null) {
			String connectionTypeUpper = connectionType.toUpperCase();
			switch (connectionTypeUpper) {
				case "STUDIO":
					if (command == null || command.trim().isEmpty()) {
						errors.add("STUDIO类型必须提供command");
					}
					break;
				case "SSE":
				case "STREAMING":
					if (url == null || url.trim().isEmpty()) {
						errors.add(connectionTypeUpper + "类型必须提供URL");
					}
					else if (!isValidUrlFormat(url)) {
						errors.add(connectionTypeUpper + "类型URL格式无效: " + url);
					}
					break;
				default:
					errors.add("不支持的连接类型: " + connectionTypeUpper);
					break;
			}
		}

		return errors;
	}

	/**
	 * 批量请求验证
	 */
	private List<String> validateBatch() {
		List<String> errors = new ArrayList<>();

		if (configJson == null || configJson.trim().isEmpty()) {
			errors.add("配置JSON不能为空");
			return errors;
		}

		try {
			JsonNode jsonNode = objectMapper.readTree(configJson);

			if (!jsonNode.has("mcpServers")) {
				errors.add("JSON必须包含mcpServers字段");
				return errors;
			}

			JsonNode mcpServersNode = jsonNode.get("mcpServers");
			if (!mcpServersNode.isObject() || mcpServersNode.size() == 0) {
				errors.add("mcpServers必须是包含至少一个服务器配置的对象");
			}

		}
		catch (Exception e) {
			errors.add("JSON格式无效: " + e.getMessage());
		}

		return errors;
	}

	/**
	 * 判断是否为更新操作
	 */
	public boolean isUpdate() {
		return requestType == RequestType.SINGLE && id != null;
	}

	/**
	 * 获取服务器数量
	 */
	public int getServerCount() {
		if (requestType != RequestType.BATCH) {
			return 1; // 单个请求返回1
		}

		try {
			JsonNode jsonNode = objectMapper.readTree(configJson);
			JsonNode mcpServersNode = jsonNode.get("mcpServers");
			return mcpServersNode.size();
		}
		catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 获取服务器名称列表
	 */
	public String[] getServerNames() {
		if (requestType != RequestType.BATCH) {
			return new String[] { mcpServerName };
		}

		try {
			JsonNode jsonNode = objectMapper.readTree(configJson);
			JsonNode mcpServersNode = jsonNode.get("mcpServers");

			String[] names = new String[mcpServersNode.size()];
			int index = 0;
			Iterator<String> fieldNames = mcpServersNode.fieldNames();
			while (fieldNames.hasNext()) {
				names[index++] = fieldNames.next();
			}
			return names;
		}
		catch (Exception e) {
			return new String[0];
		}
	}

	/**
	 * 构建单个服务器配置JSON
	 */
	public String buildSingleConfigJson() {
		if (requestType != RequestType.SINGLE) {
			throw new IllegalStateException("当前请求类型不是单个请求");
		}

		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{");

		if (command != null && !command.trim().isEmpty()) {
			jsonBuilder.append("\"command\":\"").append(command).append("\"");
		}

		if (url != null && !url.trim().isEmpty()) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"url\":\"").append(url).append("\"");
		}

		if (args != null && !args.trim().isEmpty()) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"args\":[");
			String[] argsArray = args.split("\n");
			for (int i = 0; i < argsArray.length; i++) {
				if (i > 0)
					jsonBuilder.append(",");
				jsonBuilder.append("\"").append(argsArray[i].trim()).append("\"");
			}
			jsonBuilder.append("]");
		}

		if (env != null && !env.trim().isEmpty()) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"env\":{");
			String[] envLines = env.split("\n");
			boolean first = true;
			for (String line : envLines) {
				if (line.contains(":")) {
					String[] parts = line.split(":", 2);
					if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
						if (!first)
							jsonBuilder.append(",");
						jsonBuilder.append("\"")
							.append(parts[0].trim())
							.append("\":\"")
							.append(parts[1].trim())
							.append("\"");
						first = false;
					}
				}
			}
			jsonBuilder.append("}");
		}

		if (timeout != null) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"timeout\":").append(timeout);
		}

		if (retryCount != null) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"retryCount\":").append(retryCount);
		}

		if (headers != null && !headers.trim().isEmpty()) {
			if (jsonBuilder.length() > 1)
				jsonBuilder.append(",");
			jsonBuilder.append("\"headers\":{");
			String[] headerLines = headers.split("\n");
			boolean first = true;
			for (String line : headerLines) {
				if (line.contains(":")) {
					String[] parts = line.split(":", 2);
					if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
						if (!first)
							jsonBuilder.append(",");
						jsonBuilder.append("\"")
							.append(parts[0].trim())
							.append("\":\"")
							.append(parts[1].trim())
							.append("\"");
						first = false;
					}
				}
			}
			jsonBuilder.append("}");
		}

		jsonBuilder.append("}");
		return jsonBuilder.toString();
	}

	/**
	 * 获取标准化的批量配置JSON
	 */
	public String getNormalizedBatchConfigJson() {
		if (requestType != RequestType.BATCH) {
			throw new IllegalStateException("当前请求类型不是批量请求");
		}

		try {
			JsonNode jsonNode = objectMapper.readTree(configJson);

			if (jsonNode.has("mcpServers")) {
				return configJson; // 已经是标准格式
			}

			// 转换为标准格式
			StringBuilder fullJsonBuilder = new StringBuilder();
			fullJsonBuilder.append("{\n  \"mcpServers\": ");
			fullJsonBuilder.append(configJson);
			fullJsonBuilder.append("\n}");
			return fullJsonBuilder.toString();

		}
		catch (Exception e) {
			return configJson;
		}
	}

	// isValidUrlFormat方法从父类McpConfigBase继承

	// Getters and Setters
	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

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

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	// 以下字段的getter和setter方法从父类McpConfigBase继承：
	// - command, url, args, env, timeout, retryCount, headers, status

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

}
