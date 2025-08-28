package com.alibaba.cloud.ai.example.manus.dynamic.mcp.processor;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.metadata.FieldMetadata;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import java.util.List;

public class SimpleFieldProcessorTest {

	public static void main(String[] args) {
		System.out.println("=== 开始测试字段收集 ===");

		McpConfigFieldProcessor processor = new McpConfigFieldProcessor();

		try {
			// 测试从McpConfigRequestVO收集字段
			List<FieldMetadata> fields = processor.generateFieldMetadata(McpConfigRequestVO.class);

			// 打印所有字段信息
			System.out.println("=== 收集到的字段 ===");
			System.out.println("总字段数: " + fields.size());

			for (FieldMetadata field : fields) {
				System.out.printf("字段名: %s, 显示名: %s, 显示类型: %s, 显示顺序: %d, 显示条件: %s%n", field.getFieldName(),
						field.getDisplayName(), field.getDisplayType(), field.getDisplayOrder(),
						field.getDisplayCondition());
			}

			// 验证关键字段
			System.out.println("\n=== 字段验证 ===");

			boolean hasMcpServerName = fields.stream().anyMatch(f -> "mcpServerName".equals(f.getFieldName()));
			System.out.println("包含mcpServerName字段: " + hasMcpServerName);

			boolean hasConnectionType = fields.stream().anyMatch(f -> "connectionType".equals(f.getFieldName()));
			System.out.println("包含connectionType字段: " + hasConnectionType);

			boolean hasCommand = fields.stream().anyMatch(f -> "command".equals(f.getFieldName()));
			System.out.println("包含command字段: " + hasCommand);

			boolean hasUrl = fields.stream().anyMatch(f -> "url".equals(f.getFieldName()));
			System.out.println("包含url字段: " + hasUrl);

			boolean hasArgs = fields.stream().anyMatch(f -> "args".equals(f.getFieldName()));
			System.out.println("包含args字段: " + hasArgs);

			boolean hasEnv = fields.stream().anyMatch(f -> "env".equals(f.getFieldName()));
			System.out.println("包含env字段: " + hasEnv);

			System.out.println("\n=== 测试完成 ===");

		}
		catch (Exception e) {
			System.err.println("测试失败: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
