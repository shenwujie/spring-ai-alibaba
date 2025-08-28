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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.frontend;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.metadata.FieldMetadata;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.processor.McpConfigFieldProcessor;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpConfigRequestVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 动态表单生成器，展示如何使用注解生成的元数据 这个类模拟前端如何使用后端提供的元数据来动态生成表单
 */
@Component
public class DynamicFormGenerator {

	private final McpConfigFieldProcessor fieldProcessor;

	public DynamicFormGenerator(McpConfigFieldProcessor fieldProcessor) {
		this.fieldProcessor = fieldProcessor;
	}

	/**
	 * 生成HTML表单
	 */
	public String generateHtmlForm() {
		List<FieldMetadata> fields = fieldProcessor.generateFieldMetadata(McpConfigRequestVO.class);

		StringBuilder html = new StringBuilder();
		html.append("<form id=\"mcpConfigForm\" class=\"dynamic-form\">\n");

		for (FieldMetadata field : fields) {
			html.append(generateFieldHtml(field));
		}

		html.append("  <div class=\"form-actions\">\n");
		html.append("    <button type=\"submit\" class=\"btn btn-primary\">提交</button>\n");
		html.append("    <button type=\"reset\" class=\"btn btn-secondary\">重置</button>\n");
		html.append("  </div>\n");
		html.append("</form>\n");

		return html.toString();
	}

	/**
	 * 生成单个字段的HTML
	 */
	private String generateFieldHtml(FieldMetadata field) {
		StringBuilder html = new StringBuilder();

		html.append("  <div class=\"form-group\">\n");

		// 标签
		html.append("    <label for=\"").append(field.getFieldName()).append("\" class=\"form-label\">");
		if (field.isDisplayRequired()) {
			html.append("<span class=\"required\">*</span>");
		}
		html.append(field.getDisplayName()).append("</label>\n");

		// 字段描述
		if (field.getDisplayDescription() != null && !field.getDisplayDescription().isEmpty()) {
			html.append("    <div class=\"field-description\">")
				.append(field.getDisplayDescription())
				.append("</div>\n");
		}

		// 输入控件
		html.append("    ").append(generateInputControl(field)).append("\n");

		// 验证提示
		if (field.getDisplayValidation() != null && !field.getDisplayValidation().isEmpty()) {
			html.append("    <div class=\"validation-hint\">").append(field.getDisplayValidation()).append("</div>\n");
		}

		html.append("  </div>\n");

		return html.toString();
	}

	/**
	 * 根据字段类型生成输入控件
	 */
	private String generateInputControl(FieldMetadata field) {
		FieldType fieldType = field.getDisplayType();

		switch (fieldType) {
			case TEXT:
				return generateTextInput(field);
			case SELECT:
				return generateSelectInput(field);
			case NUMBER:
				return generateNumberInput(field);
			case CHECKBOX:
				return generateCheckboxInput(field);
			case JSON_EDITOR:
				return generateJsonEditor(field);
			default:
				return generateTextInput(field);
		}
	}

	private String generateTextInput(FieldMetadata field) {
		return String.format("<input type=\"text\" id=\"%s\" name=\"%s\" class=\"form-control\" %s/>",
				field.getFieldName(), field.getFieldName(), field.isDisplayRequired() ? "required" : "");
	}

	private String generateNumberInput(FieldMetadata field) {
		return String.format("<input type=\"number\" id=\"%s\" name=\"%s\" class=\"form-control\" %s/>",
				field.getFieldName(), field.getFieldName(), field.isDisplayRequired() ? "required" : "");
	}

	private String generateSelectInput(FieldMetadata field) {
		StringBuilder html = new StringBuilder();
		html.append("<select id=\"")
			.append(field.getFieldName())
			.append("\" name=\"")
			.append(field.getFieldName())
			.append("\" class=\"form-control\"");

		if (field.isDisplayRequired()) {
			html.append(" required");
		}

		html.append(">\n");
		html.append("      <option value=\"\">请选择</option>\n");

		if (field.getDisplayOptions() != null) {
			for (String option : field.getDisplayOptions()) {
				html.append("      <option value=\"").append(option).append("\">").append(option).append("</option>\n");
			}
		}

		html.append("    </select>");
		return html.toString();
	}

	private String generateCheckboxInput(FieldMetadata field) {
		return String.format("<input type=\"checkbox\" id=\"%s\" name=\"%s\" class=\"form-check-input\" %s/>",
				field.getFieldName(), field.getFieldName(), field.isDisplayRequired() ? "required" : "");
	}

	private String generateJsonEditor(FieldMetadata field) {
		return String.format(
				"<textarea id=\"%s\" name=\"%s\" class=\"form-control json-editor\" rows=\"5\" placeholder=\"请输入JSON格式数据\" %s></textarea>",
				field.getFieldName(), field.getFieldName(), field.isDisplayRequired() ? "required" : "");
	}

}
