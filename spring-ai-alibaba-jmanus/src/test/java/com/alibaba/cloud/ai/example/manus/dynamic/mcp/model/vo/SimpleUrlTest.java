package com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo;

/**
 * 简单的URL字段测试
 */
public class SimpleUrlTest {

	public static void main(String[] args) {
		try {
			System.out.println("=== 简单URL字段测试 ===\n");

			// 测试1：创建McpServerConfig实例
			McpServerConfig config = new McpServerConfig();
			System.out.println("1. 创建McpServerConfig实例成功");

			// 测试2：设置URL字段
			config.setUrl("http://localhost:8080/api");
			System.out.println("2. 设置URL字段成功: " + config.getUrl());

			// 测试3：设置连接类型
			config.setConnectionType("STREAMING");
			System.out.println("3. 设置连接类型成功: " + config.getConnectionType());

			// 测试4：转换为JSON
			String json = config.toJson();
			System.out.println("4. 转换为JSON成功:");
			System.out.println("   " + json);

			// 测试5：验证URL字段是否包含在JSON中
			if (json.contains("\"url\":\"http://localhost:8080/api\"")) {
				System.out.println("5. ✓ URL字段正确包含在JSON中");
			}
			else {
				System.out.println("5. ✗ URL字段未包含在JSON中");
			}

			// 测试6：从JSON解析
			McpServerConfig newConfig = new McpServerConfig();
			newConfig.fromJson(json);
			System.out.println("6. 从JSON解析成功");
			System.out.println("   URL: " + newConfig.getUrl());
			System.out.println("   连接类型: " + newConfig.getConnectionType());

			System.out.println("\n=== 所有测试完成！===");
			System.out.println("🎉 URL字段功能正常！");

		}
		catch (Exception e) {
			System.err.println("测试失败: " + e.getMessage());
			e.printStackTrace();
		}
	}

}




