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
 * McpConfigVO演示程序 用于验证合并后的McpConfigVO类的功能
 */
public class McpConfigVODemo {

	public static void main(String[] args) {
		try {
			System.out.println("=== McpConfigVO功能演示 ===\n");

			// 演示1：基本功能
			demonstrateBasicFunctionality();

			// 演示2：JSON转换功能
			demonstrateJsonConversion();

			// 演示3：兼容方法
			demonstrateCompatibilityMethods();

			// 演示4：连接类型检测
			demonstrateConnectionTypeDetection();

			System.out.println("\n=== 所有演示完成！===");

		}
		catch (Exception e) {
			System.err.println("演示失败: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 演示1：基本功能
	 */
	private static void demonstrateBasicFunctionality() {
		System.out.println("1. 基本功能演示:");

		McpConfigVO config = new McpConfigVO();
		config.setId(1L);
		config.setMcpServerName("demo-server");
		config.setConnectionType("STUDIO");
		config.setCommand("java -jar app.jar");

		System.out.println("   ✓ 创建配置对象");
		System.out.println("   ✓ 设置基本字段");
		System.out.println("   ✓ ID: " + config.getId());
		System.out.println("   ✓ 服务器名称: " + config.getMcpServerName());
		System.out.println("   ✓ 连接类型: " + config.getConnectionType());
		System.out.println("   ✓ 执行命令: " + config.getCommand());
	}

	/**
	 * 演示2：JSON转换功能
	 */
	private static void demonstrateJsonConversion() throws Exception {
		System.out.println("\n2. JSON转换功能演示:");

		McpConfigVO config = new McpConfigVO();
		config.setTimeout(60);
		config.setRetryCount(3);
		config.setArgs("arg1\narg2");
		config.setEnv("KEY1:VALUE1\nKEY2:VALUE2");

		// 转换为JSON
		String json = config.toJson();
		System.out.println("   ✓ 转换为JSON:");
		System.out.println("   " + json);

		// 从JSON解析
		McpConfigVO newConfig = new McpConfigVO();
		newConfig.fromJson(json);

		System.out.println("\n   ✓ 从JSON解析:");
		System.out.println("   ✓ 超时: " + newConfig.getTimeout());
		System.out.println("   ✓ 重试次数: " + newConfig.getRetryCount());
		System.out.println("   ✓ 参数: " + newConfig.getArgs());
		System.out.println("   ✓ 环境变量: " + newConfig.getEnv());
	}

	/**
	 * 演示3：兼容方法
	 */
	private static void demonstrateCompatibilityMethods() {
		System.out.println("\n3. 兼容方法演示:");

		McpConfigVO config = new McpConfigVO();

		// 使用List类型设置args
		config.setArgsAsList(java.util.Arrays.asList("arg1", "arg2", "arg3"));
		System.out.println("   ✓ 使用List设置args:");
		System.out.println("   ✓ args字符串: " + config.getArgs());
		System.out.println("   ✓ args列表: " + config.getArgsAsList());

		// 使用Map类型设置env
		java.util.Map<String, String> envMap = new java.util.HashMap<>();
		envMap.put("KEY1", "VALUE1");
		envMap.put("KEY2", "VALUE2");
		config.setEnvAsMap(envMap);

		System.out.println("\n   ✓ 使用Map设置env:");
		System.out.println("   ✓ env字符串: " + config.getEnv());
		System.out.println("   ✓ env映射: " + config.getEnvAsMap());
	}

	/**
	 * 演示4：连接类型检测
	 */
	private static void demonstrateConnectionTypeDetection() {
		System.out.println("\n4. 连接类型检测演示:");

		McpConfigVO config = new McpConfigVO();

		// 测试STUDIO类型
		config.setCommand("java -jar app.jar");
		config.setUrl(null);
		System.out.println("   ✓ 设置command: " + config.getCommand());
		System.out.println("   ✓ 检测连接类型: " + config.getConnectionType());

		// 测试SSE类型
		config.setCommand(null);
		config.setUrl("http://localhost:8080/sse/endpoint");
		System.out.println("\n   ✓ 设置URL: " + config.getUrl());
		System.out.println("   ✓ 检测连接类型: " + config.getConnectionType());

		// 测试STREAMING类型
		config.setCommand(null);
		config.setUrl("http://localhost:8080/streaming/endpoint");
		System.out.println("\n   ✓ 设置URL: " + config.getUrl());
		System.out.println("   ✓ 检测连接类型: " + config.getConnectionType());
	}

}
