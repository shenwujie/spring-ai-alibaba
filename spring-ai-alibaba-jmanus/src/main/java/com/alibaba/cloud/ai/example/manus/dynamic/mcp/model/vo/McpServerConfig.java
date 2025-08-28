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

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Internal server configuration class 继承McpConfigVO，只包含特有的字段
 *
 * 注意：基类McpConfigVO已经包含了所有基础字段（包括url）， 子类无需重新定义这些字段，直接使用基类的实现即可
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpServerConfig extends McpConfigVO {

	// 注意：不再需要重新定义url字段，因为基类McpConfigVO已经有了完整的配置
	// 基类的url字段配置：
	// - dbField = "connection_config"
	// - dbJsonKey = "url"
	// - jsonName = "url"
	// - displayName = "服务器URL"
	// - displayType = FieldType.URL
	// - displayRequired = false
	// - businessRequired = false
	// - displayDescription = "SSE/STREAMING类型必须提供URL"
	// - displayOrder = 5
	// - displayCondition = "connectionType=SSE,STREAMING"
	// - displayRequiredCondition = "connectionType=SSE,STREAMING"

	/**
	 * Default constructor for Jackson deserialization
	 */
	public McpServerConfig() {
		super();
	}

	public McpServerConfig(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
		super(objectMapper);
	}

	/**
	 * Get connection type. Logic: 1. If has command field → STUDIO 2. If URL suffix is
	 * sse → SSE 3. Other cases → STREAMING
	 * @return Connection type
	 */
	@Override
	public String getConnectionType() {
		// 直接使用基类的实现，保持返回类型一致
		return super.getConnectionType();
	}

	/**
	 * Get connection type as McpConfigType enum
	 * @return Connection type as enum
	 */
	public McpConfigType getConnectionTypeAsEnum() {
		String connectionTypeStr = getConnectionType();
		return McpConfigType.valueOf(connectionTypeStr);
	}

	/**
	 * Convert ServerConfig to JSON string 使用基类的toJson()方法，自动支持所有字段
	 * @return Converted JSON string
	 */
	public String toJson() {
		try {
			// 直接使用基类的toJson()方法，自动支持所有字段
			return super.toJson();
		}
		catch (Exception e) {
			// 如果基类方法失败，使用手动构建的备用方案
			System.err.println("Base class toJson() failed, using fallback: " + e.getMessage());

			StringBuilder sb = new StringBuilder();
			sb.append("{");

			// Add URL (if it exists)
			if (getUrl() != null && !getUrl().isEmpty()) {
				sb.append("\"url\":\"").append(getUrl()).append("\"");
			}

			// Add command (if it exists)
			if (getCommand() != null && !getCommand().isEmpty()) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"command\":\"").append(getCommand()).append("\"");
			}

			// Add parameters (if they exist)
			if (getArgs() != null && !getArgs().isEmpty()) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"args\":[");
				boolean first = true;
				String[] args = getArgs().split("\n");
				for (String arg : args) {
					if (!first)
						sb.append(",");
					sb.append("\"").append(arg).append("\"");
					first = false;
				}
				sb.append("]");
			}

			// Add environment variables (if they exist)
			if (getEnv() != null && !getEnv().isEmpty()) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"env\":{");
				boolean first = true;
				String[] envLines = getEnv().split("\n");
				for (String line : envLines) {
					if (line.contains(":")) {
						String[] parts = line.split(":", 2);
						if (parts.length == 2) {
							if (!first)
								sb.append(",");
							sb.append("\"")
								.append(parts[0].trim())
								.append("\":\"")
								.append(parts[1].trim())
								.append("\"");
							first = false;
						}
					}
				}
				sb.append("}");
			}

			// Add timeout (if it exists)
			if (getTimeout() != null) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"timeout\":").append(getTimeout());
			}

			// Add retry count (if it exists)
			if (getRetryCount() != null) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"retryCount\":").append(getRetryCount());
			}

			// Add headers (if they exist)
			if (getHeaders() != null && !getHeaders().isEmpty()) {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"headers\":{");
				boolean first = true;
				String[] headerLines = getHeaders().split("\n");
				for (String line : headerLines) {
					if (line.contains(":")) {
						String[] parts = line.split(":", 2);
						if (parts.length == 2) {
							if (!first)
								sb.append(",");
							sb.append("\"")
								.append(parts[0].trim())
								.append("\":\"")
								.append(parts[1].trim())
								.append("\"");
							first = false;
						}
					}
				}
				sb.append("}");
			}

			// Add status (always include)
			if (sb.length() > 1)
				sb.append(",");
			sb.append("\"status\":\"").append(getStatus()).append("\"");

			sb.append("}");
			return sb.toString();
		}
	}

	// 注意：不再需要重写getUrl()和setUrl()方法，直接使用基类的实现
	// 基类McpConfigVO已经提供了完整的url字段getter和setter方法

	/**
	 * 兼容方法：获取args作为List
	 */
	public List<String> getArgsAsList() {
		return super.getArgsAsList();
	}

	/**
	 * 兼容方法：设置args为List
	 */
	public void setArgsAsList(List<String> argsList) {
		super.setArgsAsList(argsList);
	}

	/**
	 * 兼容方法：获取env作为Map
	 */
	public Map<String, String> getEnvAsMap() {
		return super.getEnvAsMap();
	}

	/**
	 * 兼容方法：设置env为Map
	 */
	public void setEnvAsMap(Map<String, String> envMap) {
		super.setEnvAsMap(envMap);
	}

}
