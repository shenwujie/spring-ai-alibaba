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

/**
 * URL字段测试程序 用于验证URL字段的显示和配置是否正确
 */
public class UrlFieldTest {

	public static void main(String[] args) {
		try {
			System.out.println("=== URL字段测试 ===\n");

			// 测试1：STREAMING连接类型
			testStreamingConnection();

			// 测试2：SSE连接类型
			testSSEConnection();

			// 测试3：STUDIO连接类型
			testStudioConnection();

			// 测试4：从JSON解析
			testFromJson();

			System.out.println("\n=== 所有测试完成！===");

		}
		catch (Exception e) {
			System.err.println("测试失败: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 测试STREAMING连接类型
	 */
	private static void testStreamingConnection() throws Exception {
		System.out.println("1. 测试STREAMING连接类型:");

		McpServerConfig config = new McpServerConfig();
		config.setConnectionType("STREAMING");
		config.setUrl("http://localhost:8080/streaming");
		config.setTimeout(20);
		config.setRetryCount(3);

		System.out.println("   ✓ 设置连接类型: " + config.getConnectionType());
		System.out.println("   ✓ 设置URL: " + config.getUrl());
		System.out.println("   ✓ 设置超时: " + config.getTimeout());
		System.out.println("   ✓ 设置重试次数: " + config.getRetryCount());

		// 转换为JSON
		String json = config.toJson();
		System.out.println("\n   ✓ 转换为JSON:");
		System.out.println("   " + json);

		// 验证URL字段是否包含
		if (json.contains("\"url\":\"http://localhost:8080/streaming\"")) {
			System.out.println("   ✓ URL字段正确包含在JSON中");
		}
		else {
			System.out.println("   ✗ URL字段未包含在JSON中");
		}
	}

	/**
	 * 测试SSE连接类型
	 */
	private static void testSSEConnection() throws Exception {
		System.out.println("\n2. 测试SSE连接类型:");

		McpServerConfig config = new McpServerConfig();
		config.setConnectionType("SSE");
		config.setUrl("http://localhost:8080/sse/endpoint");
		config.setTimeout(30);

		System.out.println("   ✓ 设置连接类型: " + config.getConnectionType());
		System.out.println("   ✓ 设置URL: " + config.getUrl());
		System.out.println("   ✓ 设置超时: " + config.getTimeout());

		// 转换为JSON
		String json = config.toJson();
		System.out.println("\n   ✓ 转换为JSON:");
		System.out.println("   " + json);

		// 验证URL字段是否包含
		if (json.contains("\"url\":\"http://localhost:8080/sse/endpoint\"")) {
			System.out.println("   ✓ URL字段正确包含在JSON中");
		}
		else {
			System.out.println("   ✗ URL字段未包含在JSON中");
		}
	}

	/**
	 * 测试STUDIO连接类型
	 */
	private static void testStudioConnection() throws Exception {
		System.out.println("\n3. 测试STUDIO连接类型:");

		McpServerConfig config = new McpServerConfig();
		config.setConnectionType("STUDIO");
		config.setCommand("java -jar app.jar");
		config.setArgs("arg1\narg2");
		config.setEnv("KEY1:VALUE1\nKEY2:VALUE2");

		System.out.println("   ✓ 设置连接类型: " + config.getConnectionType());
		System.out.println("   ✓ 设置命令: " + config.getCommand());
		System.out.println("   ✓ 设置参数: " + config.getArgs());
		System.out.println("   ✓ 设置环境变量: " + config.getEnv());

		// 转换为JSON
		String json = config.toJson();
		System.out.println("\n   ✓ 转换为JSON:");
		System.out.println("   " + json);

		// 验证command字段是否包含
		if (json.contains("\"command\":\"java -jar app.jar\"")) {
			System.out.println("   ✓ command字段正确包含在JSON中");
		}
		else {
			System.out.println("   ✗ command字段未包含在JSON中");
		}
	}

	/**
	 * 测试从JSON解析
	 */
	private static void testFromJson() throws Exception {
		System.out.println("\n4. 测试从JSON解析:");

		String json = """
				{
				    "url": "http://test.com/api",
				    "timeout": 60,
				    "retryCount": 5,
				    "headers": "Content-Type:application/json\\nAuthorization:Bearer token"
				}
				""";

		McpServerConfig config = new McpServerConfig();
		config.fromJson(json);

		System.out.println("   ✓ 从JSON解析:");
		System.out.println("   ✓ URL: " + config.getUrl());
		System.out.println("   ✓ 超时: " + config.getTimeout());
		System.out.println("   ✓ 重试次数: " + config.getRetryCount());
		System.out.println("   ✓ 请求头: " + config.getHeaders());

		// 验证解析结果
		if ("http://test.com/api".equals(config.getUrl())) {
			System.out.println("   ✓ URL字段正确解析");
		}
		else {
			System.out.println("   ✗ URL字段解析失败");
		}
	}

	/**
	 * 显示字段配置信息
	 */
	public static void showFieldConfiguration() {
		System.out.println("\n=== URL字段配置信息 ===");
		System.out.println("基类McpConfigVO中的url字段配置:");
		System.out.println("- dbField: connection_config");
		System.out.println("- dbJsonKey: url");
		System.out.println("- jsonName: url");
		System.out.println("- displayName: 服务器URL");
		System.out.println("- displayType: FieldType.URL");
		System.out.println("- displayRequired: false");
		System.out.println("- businessRequired: false");
		System.out.println("- displayDescription: SSE/STREAMING类型必须提供URL");
		System.out.println("- displayOrder: 5");
		System.out.println("- displayCondition: connectionType=SSE,STREAMING");
		System.out.println("- displayRequiredCondition: connectionType=SSE,STREAMING");

		System.out.println("\n显示条件说明:");
		System.out.println("- 当connectionType为SSE或STREAMING时，URL字段会显示");
		System.out.println("- 当connectionType为STUDIO时，URL字段不会显示");
		System.out.println("- 当connectionType为SSE或STREAMING时，URL字段为必填");
	}

}
