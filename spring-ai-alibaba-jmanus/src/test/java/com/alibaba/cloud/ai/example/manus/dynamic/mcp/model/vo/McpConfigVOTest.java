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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * McpConfigVO测试类
 */
public class McpConfigVOTest {

	private McpConfigVO configVO;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		configVO = new McpConfigVO(objectMapper);
	}

	@Test
	void testDefaultConstructor() {
		assertNotNull(configVO);
		assertEquals("", configVO.getArgs());
		assertEquals("", configVO.getEnv());
		assertEquals("", configVO.getHeaders());
		assertEquals("ENABLE", configVO.getStatus());
		assertEquals(30, configVO.getTimeout());
		assertEquals(3, configVO.getRetryCount());
	}

	@Test
	void testSetAndGetBasicFields() {
		configVO.setId(1L);
		configVO.setMcpServerName("test-server");
		configVO.setConnectionType("STUDIO");
		configVO.setCommand("test-command");
		configVO.setUrl("http://test.com");
		configVO.setArgs("arg1\narg2");
		configVO.setEnv("KEY1:VALUE1\nKEY2:VALUE2");
		configVO.setHeaders("Header1:Value1\nHeader2:Value2");
		configVO.setTimeout(60);
		configVO.setRetryCount(5);
		configVO.setStatus("DISABLE");

		assertEquals(1L, configVO.getId());
		assertEquals("test-server", configVO.getMcpServerName());
		assertEquals("STUDIO", configVO.getConnectionType());
		assertEquals("test-command", configVO.getCommand());
		assertEquals("http://test.com", configVO.getUrl());
		assertEquals("arg1\narg2", configVO.getArgs());
		assertEquals("KEY1:VALUE1\nKEY2:VALUE2", configVO.getEnv());
		assertEquals("Header1:Value1\nHeader2:Value2", configVO.getHeaders());
		assertEquals(60, configVO.getTimeout());
		assertEquals(5, configVO.getRetryCount());
		assertEquals("DISABLE", configVO.getStatus());
	}

	@Test
	void testToJson() throws Exception {
		configVO.setId(1L);
		configVO.setMcpServerName("test-server");
		configVO.setConnectionType("STUDIO");
		configVO.setCommand("test-command");
		configVO.setArgs("arg1\narg2");
		configVO.setEnv("KEY1:VALUE1\nKEY2:VALUE2");
		configVO.setHeaders("Header1:Value1\nHeader2:Value2");
		configVO.setTimeout(60);
		configVO.setRetryCount(5);
		configVO.setStatus("ENABLE");

		String json = configVO.toJson();
		assertNotNull(json);
		assertTrue(json.contains("\"id\":1"));
		assertTrue(json.contains("\"mcpServerName\":\"test-server\""));
		assertTrue(json.contains("\"connectionType\":\"STUDIO\""));
		assertTrue(json.contains("\"command\":\"test-command\""));
		assertTrue(json.contains("\"args\":[\"arg1\",\"arg2\"]"));
		assertTrue(json.contains("\"env\":{\"KEY1\":\"VALUE1\",\"KEY2\":\"VALUE2\"}"));
		assertTrue(json.contains("\"headers\":{\"Header1\":\"Value1\",\"Header2\":\"Value2\"}"));
		assertTrue(json.contains("\"timeout\":60"));
		assertTrue(json.contains("\"retryCount\":5"));
		assertTrue(json.contains("\"status\":\"ENABLE\""));
	}

	@Test
	void testFromJson() throws Exception {
		String json = """
				{
				    "command": "test-command",
				    "args": ["arg1", "arg2"],
				    "env": {"KEY1": "VALUE1", "KEY2": "VALUE2"},
				    "headers": {"Header1": "Value1", "Header2": "Value2"},
				    "timeout": 60,
				    "retryCount": 5
				}
				""";

		configVO.fromJson(json);

		assertEquals("test-command", configVO.getCommand());
		assertEquals("arg1\narg2", configVO.getArgs());
		assertEquals("KEY1:VALUE1\nKEY2:VALUE2", configVO.getEnv());
		assertEquals("Header1:Value1\nHeader2:Value2", configVO.getHeaders());
		assertEquals(60, configVO.getTimeout());
		assertEquals(5, configVO.getRetryCount());
	}

	@Test
	void testGetConnectionType() {
		// 测试STUDIO类型
		configVO.setCommand("test-command");
		configVO.setUrl(null);
		assertEquals("STUDIO", configVO.getConnectionType());

		// 测试SSE类型
		configVO.setCommand(null);
		configVO.setUrl("http://test.com/sse/endpoint");
		assertEquals("SSE", configVO.getConnectionType());

		// 测试STREAMING类型
		configVO.setCommand(null);
		configVO.setUrl("http://test.com/streaming/endpoint");
		assertEquals("STREAMING", configVO.getConnectionType());
	}

	@Test
	void testFromEntities() {
		// 创建模拟的Entity对象
		MockEntity entity1 = new MockEntity();
		entity1.setId(1L);
		entity1.setMcpServerName("server1");
		entity1.setConnectionType("STUDIO");
		entity1.setStatus("ENABLE");
		entity1.setConnectionConfig("{\"command\":\"cmd1\",\"timeout\":30}");

		MockEntity entity2 = new MockEntity();
		entity2.setId(2L);
		entity2.setMcpServerName("server2");
		entity2.setConnectionType("SSE");
		entity2.setStatus("DISABLE");
		entity2.setConnectionConfig("{\"url\":\"http://test.com/sse\",\"timeout\":60}");

		List<MockEntity> entities = Arrays.asList(entity1, entity2);
		List<McpConfigVO> vos = McpConfigVO.fromEntities(entities, objectMapper);

		assertEquals(2, vos.size());

		// 验证第一个VO
		McpConfigVO vo1 = vos.get(0);
		assertEquals(1L, vo1.getId());
		assertEquals("server1", vo1.getMcpServerName());
		assertEquals("STUDIO", vo1.getConnectionType());
		assertEquals("ENABLE", vo1.getStatus());
		assertEquals("cmd1", vo1.getCommand());
		assertEquals(30, vo1.getTimeout());

		// 验证第二个VO
		McpConfigVO vo2 = vos.get(1);
		assertEquals(2L, vo2.getId());
		assertEquals("server2", vo2.getMcpServerName());
		assertEquals("SSE", vo2.getConnectionType());
		assertEquals("DISABLE", vo2.getStatus());
		assertEquals("http://test.com/sse", vo2.getUrl());
		assertEquals(60, vo2.getTimeout());
	}

	@Test
	void testEmptyJson() throws Exception {
		// 测试空JSON
		configVO.fromJson("");
		assertEquals("", configVO.getArgs());
		assertEquals("", configVO.getEnv());
		assertEquals("", configVO.getHeaders());

		// 测试null JSON
		configVO.fromJson(null);
		assertEquals("", configVO.getArgs());
		assertEquals("", configVO.getEnv());
		assertEquals("", configVO.getHeaders());
	}

	@Test
	void testInvalidJson() {
		// 测试无效JSON
		assertDoesNotThrow(() -> {
			configVO.fromJson("invalid json");
		});
	}

	/**
	 * 模拟Entity类用于测试
	 */
	private static class MockEntity {

		private Long id;

		private String mcpServerName;

		private String connectionType;

		private String status;

		private String connectionConfig;

		// Getters and Setters
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

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getConnectionConfig() {
			return connectionConfig;
		}

		public void setConnectionConfig(String connectionConfig) {
			this.connectionConfig = connectionConfig;
		}

	}

}
