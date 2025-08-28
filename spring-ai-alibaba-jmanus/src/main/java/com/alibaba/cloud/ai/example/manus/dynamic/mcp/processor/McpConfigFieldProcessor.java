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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.processor;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.McpConfigField;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.metadata.FieldMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP配置字段注解处理器
 */
@Component
public class McpConfigFieldProcessor {

	/**
	 * 生成字段元数据
	 */
	public List<FieldMetadata> generateFieldMetadata(Class<?> configClass) {
		List<FieldMetadata> metadata = new ArrayList<>();

		// 递归获取当前类和所有父类的字段
		collectFieldsRecursively(configClass, metadata);

		// 按显示顺序排序
		metadata.sort(Comparator.comparingInt(FieldMetadata::getDisplayOrder));
		return metadata;
	}

	/**
	 * 递归收集字段，包括当前类和所有父类
	 */
	private void collectFieldsRecursively(Class<?> clazz, List<FieldMetadata> metadata) {
		// 如果到达Object类，停止递归
		if (clazz == null || clazz == Object.class) {
			return;
		}

		// 先递归处理父类，确保父类字段在前面（保持继承顺序）
		collectFieldsRecursively(clazz.getSuperclass(), metadata);

		// 处理当前类的字段
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			McpConfigField annotation = field.getAnnotation(McpConfigField.class);
			if (annotation != null) {
				FieldMetadata fieldMeta = new FieldMetadata();

				// 设置字段基本信息
				fieldMeta.setFieldName(field.getName());
				fieldMeta.setFieldType(field.getType());

				// 设置数据库相关配置
				fieldMeta.setDbField(annotation.dbField());
				fieldMeta.setDbJsonKey(annotation.dbJsonKey());
				fieldMeta.setStorageType(annotation.storageType());

				// 设置JSON相关配置
				fieldMeta.setJsonName(annotation.jsonName());
				fieldMeta.setJsonDefaultValue(annotation.jsonDefaultValue());
				fieldMeta.setJsonRequired(annotation.jsonRequired());

				// 设置前端展示配置
				fieldMeta.setDisplayName(annotation.displayName());
				fieldMeta.setDisplayDescription(annotation.displayDescription());
				fieldMeta.setDisplayRequired(annotation.displayRequired());
				fieldMeta.setDisplayType(annotation.displayType());
				fieldMeta.setDisplayOptions(annotation.displayOptions());
				fieldMeta.setDisplayValidation(annotation.displayValidation());
				fieldMeta.setDisplayOrder(annotation.displayOrder());
				fieldMeta.setDisplayCondition(annotation.displayCondition());
				fieldMeta.setDisplayRequiredCondition(annotation.displayRequiredCondition());

				// 设置业务逻辑配置
				fieldMeta.setBusinessRequired(annotation.businessRequired());
				fieldMeta.setBusinessValidation(annotation.businessValidation());
				fieldMeta.setDynamic(annotation.dynamic());

				metadata.add(fieldMeta);
			}
		}
	}

	/**
	 * 生成前端表单配置
	 */
	public Map<String, Object> generateFrontendConfig(Class<?> configClass) {
		List<FieldMetadata> fields = generateFieldMetadata(configClass);

		Map<String, Object> config = new HashMap<>();
		config.put("fields", fields);
		config.put("validationRules", generateValidationRules(fields));
		config.put("formSchema", generateFormSchema(fields));

		return config;
	}

	/**
	 * 生成验证规则
	 */
	private Map<String, Object> generateValidationRules(List<FieldMetadata> fields) {
		Map<String, Object> rules = new HashMap<>();

		for (FieldMetadata field : fields) {
			if (field.isBusinessRequired() || field.isDisplayRequired()) {
				Map<String, Object> fieldRules = new HashMap<>();

				// 必填验证
				if (field.isBusinessRequired()) {
					fieldRules.put("required", true);
					fieldRules.put("message", field.getDisplayName() + "不能为空");
				}

				// 自定义验证规则
				if (field.getDisplayValidation() != null && !field.getDisplayValidation().isEmpty()) {
					parseValidationRule(field.getDisplayValidation(), fieldRules);
				}

				rules.put(field.getFieldName(), fieldRules);
			}
		}

		return rules;
	}

	/**
	 * 解析验证规则
	 */
	private void parseValidationRule(String validationRule, Map<String, Object> fieldRules) {
		if (validationRule.contains("minLength:")) {
			String minLength = validationRule.substring(validationRule.indexOf("minLength:") + 10);
			if (minLength.contains(",")) {
				minLength = minLength.substring(0, minLength.indexOf(","));
			}
			fieldRules.put("minLength", Integer.parseInt(minLength));
		}

		if (validationRule.contains("maxLength:")) {
			String maxLength = validationRule.substring(validationRule.indexOf("maxLength:") + 10);
			if (maxLength.contains(",")) {
				maxLength = maxLength.substring(0, maxLength.indexOf(","));
			}
			fieldRules.put("maxLength", Integer.parseInt(maxLength));
		}

		if (validationRule.contains("min:")) {
			String min = validationRule.substring(validationRule.indexOf("min:") + 4);
			if (min.contains(",")) {
				min = min.substring(0, min.indexOf(","));
			}
			fieldRules.put("min", Integer.parseInt(min));
		}

		if (validationRule.contains("max:")) {
			String max = validationRule.substring(validationRule.indexOf("max:") + 4);
			if (max.contains(",")) {
				max = max.substring(0, max.indexOf(","));
			}
			fieldRules.put("max", Integer.parseInt(max));
		}

		if (validationRule.contains("url")) {
			fieldRules.put("url", true);
		}

		if (validationRule.contains("email")) {
			fieldRules.put("email", true);
		}
	}

	/**
	 * 生成表单模式
	 */
	private Map<String, Object> generateFormSchema(List<FieldMetadata> fields) {
		Map<String, Object> schema = new HashMap<>();
		schema.put("type", "object");

		Map<String, Object> properties = new HashMap<>();
		List<String> required = new ArrayList<>();

		for (FieldMetadata field : fields) {
			Map<String, Object> property = new HashMap<>();

			// 设置字段类型
			property.put("type", getJsonSchemaType(field.getFieldType()));

			// 设置标题
			if (field.getDisplayName() != null && !field.getDisplayName().isEmpty()) {
				property.put("title", field.getDisplayName());
			}

			// 设置描述
			if (field.getDisplayDescription() != null && !field.getDisplayDescription().isEmpty()) {
				property.put("description", field.getDisplayDescription());
			}

			// 设置默认值
			if (field.getJsonDefaultValue() != null && !field.getJsonDefaultValue().isEmpty()) {
				property.put("default", field.getJsonDefaultValue());
			}

			// 设置选择项
			if (field.getDisplayOptions() != null && field.getDisplayOptions().length > 0) {
				property.put("enum", Arrays.asList(field.getDisplayOptions()));
			}

			// 设置验证规则
			if (field.getDisplayValidation() != null && !field.getDisplayValidation().isEmpty()) {
				Map<String, Object> validation = new HashMap<>();
				parseValidationRule(field.getDisplayValidation(), validation);
				property.putAll(validation);
			}

			properties.put(field.getFieldName(), property);

			// 设置必填字段
			if (field.isBusinessRequired()) {
				required.add(field.getFieldName());
			}
		}

		schema.put("properties", properties);
		if (!required.isEmpty()) {
			schema.put("required", required);
		}

		return schema;
	}

	/**
	 * 获取JSON Schema类型
	 */
	private String getJsonSchemaType(Class<?> fieldType) {
		if (fieldType == String.class) {
			return "string";
		}
		else if (fieldType == Integer.class || fieldType == int.class || fieldType == Long.class
				|| fieldType == long.class || fieldType == Double.class || fieldType == double.class
				|| fieldType == Float.class || fieldType == float.class) {
			return "number";
		}
		else if (fieldType == Boolean.class || fieldType == boolean.class) {
			return "boolean";
		}
		else if (List.class.isAssignableFrom(fieldType)) {
			return "array";
		}
		else if (Map.class.isAssignableFrom(fieldType)) {
			return "object";
		}
		else {
			return "string";
		}
	}

	/**
	 * 生成数据库插入SQL
	 */
	public String generateInsertSQL(Class<?> configClass) {
		List<FieldMetadata> fields = generateFieldMetadata(configClass);

		// 获取直接存储字段
		List<String> directFields = fields.stream()
			.filter(f -> f
				.getStorageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.DIRECT)
			.map(FieldMetadata::getDbField)
			.filter(field -> !field.isEmpty())
			.collect(Collectors.toList());

		// 检查是否有JSON字段
		boolean hasJsonField = fields.stream()
			.anyMatch(f -> f
				.getStorageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.JSON_KEY);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO mcp_config (");

		// 添加直接字段
		sql.append(String.join(", ", directFields));

		// 如果有JSON字段，添加connection_config字段
		if (hasJsonField) {
			if (!directFields.isEmpty()) {
				sql.append(", ");
			}
			sql.append("connection_config");
		}

		sql.append(") VALUES (");

		// 添加值占位符
		sql.append(String.join(", ", Collections.nCopies(directFields.size(), "?")));

		if (hasJsonField) {
			if (!directFields.isEmpty()) {
				sql.append(", ");
			}
			sql.append("?");
		}

		sql.append(")");

		return sql.toString();
	}

	/**
	 * 生成数据库更新SQL
	 */
	public String generateUpdateSQL(Class<?> configClass) {
		List<FieldMetadata> fields = generateFieldMetadata(configClass);

		// 获取直接存储字段（排除id字段）
		List<String> directFields = fields.stream()
			.filter(f -> f
				.getStorageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.DIRECT)
			.map(FieldMetadata::getDbField)
			.filter(field -> !field.isEmpty() && !"id".equals(field))
			.collect(Collectors.toList());

		// 检查是否有JSON字段
		boolean hasJsonField = fields.stream()
			.anyMatch(f -> f
				.getStorageType() == com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType.JSON_KEY);

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE mcp_config SET ");

		// 添加直接字段
		List<String> setClauses = directFields.stream().map(field -> field + " = ?").collect(Collectors.toList());

		// 如果有JSON字段，添加connection_config字段
		if (hasJsonField) {
			setClauses.add("connection_config = ?");
		}

		sql.append(String.join(", ", setClauses));
		sql.append(" WHERE id = ?");

		return sql.toString();
	}

}
