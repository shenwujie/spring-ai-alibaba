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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.controller;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.processor.McpConfigFieldProcessor;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpConfigVO;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP配置元数据控制器，提供前端配置生成API
 */
@RestController
@RequestMapping("/api/mcp/config/metadata")
public class McpConfigMetadataController {

	@Autowired
	private McpConfigFieldProcessor fieldProcessor;

	/**
	 * 获取McpConfigVO的字段元数据
	 */
	@GetMapping("/vo")
	public ResponseEntity<Map<String, Object>> getVoMetadata() {
		Map<String, Object> metadata = fieldProcessor.generateFrontendConfig(McpConfigVO.class);
		return ResponseEntity.ok(metadata);
	}

	/**
	 * 获取McpConfigRequestVO的字段元数据
	 */
	@GetMapping("/request")
	public ResponseEntity<Map<String, Object>> getRequestMetadata() {
		Map<String, Object> metadata = fieldProcessor.generateFrontendConfig(McpConfigRequestVO.class);
		return ResponseEntity.ok(metadata);
	}

	/**
	 * 获取所有配置类的元数据
	 */
	@GetMapping("/all")
	public ResponseEntity<Map<String, Object>> getAllMetadata() {
		Map<String, Object> allMetadata = new HashMap<>();

		allMetadata.put("vo", fieldProcessor.generateFrontendConfig(McpConfigVO.class));
		allMetadata.put("request", fieldProcessor.generateFrontendConfig(McpConfigRequestVO.class));

		return ResponseEntity.ok(allMetadata);
	}

	/**
	 * 获取字段验证规则
	 */
	@GetMapping("/validation")
	public ResponseEntity<Map<String, Object>> getValidationRules() {
		Map<String, Object> voValidation = fieldProcessor.generateFrontendConfig(McpConfigVO.class);
		Map<String, Object> requestValidation = fieldProcessor.generateFrontendConfig(McpConfigRequestVO.class);

		Map<String, Object> allValidation = new HashMap<>();
		allValidation.put("vo", voValidation.get("validationRules"));
		allValidation.put("request", requestValidation.get("validationRules"));

		return ResponseEntity.ok(allValidation);
	}

	/**
	 * 获取表单模式
	 */
	@GetMapping("/schema")
	public ResponseEntity<Map<String, Object>> getFormSchema() {
		Map<String, Object> voSchema = fieldProcessor.generateFrontendConfig(McpConfigVO.class);
		Map<String, Object> requestSchema = fieldProcessor.generateFrontendConfig(McpConfigRequestVO.class);

		Map<String, Object> allSchema = new HashMap<>();
		allSchema.put("vo", voSchema.get("formSchema"));
		allSchema.put("request", requestSchema.get("formSchema"));

		return ResponseEntity.ok(allSchema);
	}

}
