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
 * 零代码修改演示程序
 *
 * 这个演示展示了McpConfigVO系统的强大功能： 新增字段时，只需定义字段和注解，所有功能自动可用！
 */
public class ZeroCodeDemo {

	public static void main(String[] args) {
		try {
			System.out.println("=== 零代码修改演示 ===\n");

			// 演示1：基本功能
			demonstrateBasicFunctionality();

			// 演示2：新增字段自动支持
			demonstrateNewFieldSupport();

			// 演示3：类型转换自动处理
			demonstrateTypeConversion();

			// 演示4：默认值自动设置
			demonstrateDefaultValues();

			System.out.println("\n=== 所有演示完成！===");
			System.out.println("🎉 新增字段时，您真的不需要修改任何代码！");

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
		config.setMcpServerName("demo-server");
		config.setConnectionType("STUDIO");
		config.setCommand("demo-command");

		System.out.println("   ✓ 创建配置对象");
		System.out.println("   ✓ 设置基本字段");
		System.out.println("   ✓ 服务器名称: " + config.getMcpServerName());
		System.out.println("   ✓ 连接类型: " + config.getConnectionType());
		System.out.println("   ✓ 执行命令: " + config.getCommand());
	}

	/**
	 * 演示2：新增字段自动支持
	 *
	 * 注意：这里我们模拟了一个新字段，但实际上这个字段已经在McpConfigVO中定义了 在真实场景中，您只需要在McpConfigVO类中添加新字段和注解即可
	 */
	private static void demonstrateNewFieldSupport() throws Exception {
		System.out.println("\n2. 新增字段自动支持演示:");

		McpConfigVO config = new McpConfigVO();

		// 模拟设置新字段（这些字段已经在类中定义，但演示了自动支持）
		config.setTimeout(60);
		config.setRetryCount(3);
		config.setArgs("arg1\narg2\narg3");
		config.setEnv("KEY1:VALUE1\nKEY2:VALUE2");

		System.out.println("   ✓ 设置新字段值");
		System.out.println("   ✓ 超时时间: " + config.getTimeout());
		System.out.println("   ✓ 重试次数: " + config.getRetryCount());
		System.out.println("   ✓ 参数列表: " + config.getArgs());
		System.out.println("   ✓ 环境变量: " + config.getEnv());

		// 转换为JSON - 新字段自动包含！
		String json = config.toJson();
		System.out.println("\n   ✓ 转换为JSON（新字段自动包含）:");
		System.out.println("   " + json);

		// 从JSON解析 - 新字段自动解析！
		McpConfigVO newConfig = new McpConfigVO();
		newConfig.fromJson(json);

		System.out.println("\n   ✓ 从JSON解析（新字段自动解析）:");
		System.out.println("   ✓ 超时时间: " + newConfig.getTimeout());
		System.out.println("   ✓ 重试次数: " + newConfig.getRetryCount());
		System.out.println("   ✓ 参数列表: " + newConfig.getArgs());
		System.out.println("   ✓ 环境变量: " + newConfig.getEnv());
	}

	/**
	 * 演示3：类型转换自动处理
	 */
	private static void demonstrateTypeConversion() {
		System.out.println("\n3. 类型转换自动处理演示:");

		McpConfigVO config = new McpConfigVO();

		// 设置特殊格式的字符串
		config.setArgs("line1\nline2\nline3");
		config.setEnv("KEY1:VALUE1\nKEY2:VALUE2");
		config.setHeaders("Header1:Value1\nHeader2:Value2");

		System.out.println("   ✓ 设置特殊格式字段");
		System.out.println("   ✓ args (换行分隔): " + config.getArgs());
		System.out.println("   ✓ env (key:value格式): " + config.getEnv());
		System.out.println("   ✓ headers (key:value格式): " + config.getHeaders());

		try {
			// 转换为JSON - 自动类型转换！
			String json = config.toJson();
			System.out.println("\n   ✓ 转换为JSON（自动类型转换）:");
			System.out.println("   " + json);

			// 验证类型转换结果
			if (json.contains("\"args\":[\"line1\",\"line2\",\"line3\"]")) {
				System.out.println("   ✓ args字段自动转换为数组");
			}
			if (json.contains("\"env\":{\"KEY1\":\"VALUE1\",\"KEY2\":\"VALUE2\"}")) {
				System.out.println("   ✓ env字段自动转换为对象");
			}
			if (json.contains("\"headers\":{\"Header1\":\"Value1\",\"Header2\":\"Value2\"}")) {
				System.out.println("   ✓ headers字段自动转换为对象");
			}

		}
		catch (Exception e) {
			System.err.println("   ✗ JSON转换失败: " + e.getMessage());
		}
	}

	/**
	 * 演示4：默认值自动设置
	 */
	private static void demonstrateDefaultValues() {
		System.out.println("\n4. 默认值自动设置演示:");

		// 创建新实例 - 自动设置默认值！
		McpConfigVO config = new McpConfigVO();

		System.out.println("   ✓ 创建新实例");
		System.out.println("   ✓ 状态默认值: " + config.getStatus());
		System.out.println("   ✓ 超时默认值: " + config.getTimeout());
		System.out.println("   ✓ 重试次数默认值: " + config.getRetryCount());
		System.out.println("   ✓ args默认值: '" + config.getArgs() + "'");
		System.out.println("   ✓ env默认值: '" + config.getEnv() + "'");
		System.out.println("   ✓ headers默认值: '" + config.getHeaders() + "'");
	}

	/**
	 * 演示如何添加新字段（说明文档）
	 */
	public static void showHowToAddNewField() {
		System.out.println("\n=== 如何添加新字段 ===");
		System.out.println("1. 在McpConfigVO类中添加字段:");
		System.out.println("   private String newField;");
		System.out.println("");
		System.out.println("2. 添加注解:");
		System.out.println("   @McpConfigField(");
		System.out.println("       dbField = \"new_field\",");
		System.out.println("       jsonName = \"newField\",");
		System.out.println("       displayName = \"新字段\",");
		System.out.println("       displayType = FieldType.TEXT,");
		System.out.println("       displayOrder = 13");
		System.out.println("   )");
		System.out.println("   @JsonProperty(\"newField\")");
		System.out.println("");
		System.out.println("3. 添加getter/setter（推荐）:");
		System.out.println("   public String getNewField() { return newField; }");
		System.out.println("   public void setNewField(String newField) { this.newField = newField; }");
		System.out.println("");
		System.out.println("4. 完成！所有功能自动支持新字段:");
		System.out.println("   ✓ toJson() 自动包含新字段");
		System.out.println("   ✓ fromJson() 自动解析新字段");
		System.out.println("   ✓ 构造函数自动初始化新字段");
		System.out.println("   ✓ 类型转换自动处理新字段");
		System.out.println("   ✓ 验证规则自动应用新字段");
	}

}
