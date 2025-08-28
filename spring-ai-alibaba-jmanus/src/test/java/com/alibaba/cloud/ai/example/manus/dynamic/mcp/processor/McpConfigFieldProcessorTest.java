package com.alibaba.cloud.ai.example.manus.dynamic.mcp.processor;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.metadata.FieldMetadata;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class McpConfigFieldProcessorTest {

	@Test
	public void testGenerateFieldMetadata() {
		McpConfigFieldProcessor processor = new McpConfigFieldProcessor();

		// 测试从McpConfigRequestVO收集字段
		List<FieldMetadata> fields = processor.generateFieldMetadata(McpConfigRequestVO.class);

		// 打印所有字段信息
		System.out.println("=== 收集到的字段 ===");
		for (FieldMetadata field : fields) {
			System.out.printf("字段名: %s, 显示名: %s, 显示类型: %s, 显示顺序: %d, 显示条件: %s%n", field.getFieldName(),
					field.getDisplayName(), field.getDisplayType(), field.getDisplayOrder(),
					field.getDisplayCondition());
		}

		// 验证基本字段是否存在
		assertNotNull(fields);
		assertTrue(fields.size() > 0, "应该至少有一个字段");

		// 验证关键字段
		boolean hasMcpServerName = fields.stream().anyMatch(f -> "mcpServerName".equals(f.getFieldName()));
		assertTrue(hasMcpServerName, "应该包含mcpServerName字段");

		boolean hasConnectionType = fields.stream().anyMatch(f -> "connectionType".equals(f.getFieldName()));
		assertTrue(hasConnectionType, "应该包含connectionType字段");

		boolean hasCommand = fields.stream().anyMatch(f -> "command".equals(f.getFieldName()));
		assertTrue(hasCommand, "应该包含command字段");

		boolean hasUrl = fields.stream().anyMatch(f -> "url".equals(f.getFieldName()));
		assertTrue(hasUrl, "应该包含url字段");

		boolean hasArgs = fields.stream().anyMatch(f -> "args".equals(f.getFieldName()));
		assertTrue(hasArgs, "应该包含args字段");

		boolean hasEnv = fields.stream().anyMatch(f -> "env".equals(f.getFieldName()));
		assertTrue(hasEnv, "应该包含env字段");

		System.out.println("=== 测试通过 ===");
	}

}
