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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.metadata;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.DbStorageType;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation.FieldType;

/**
 * 字段元数据，用于存储字段的各种配置信息
 */
public class FieldMetadata {

	/**
	 * 字段名
	 */
	private String fieldName;

	/**
	 * 对应的数据库字段名
	 */
	private String dbField;

	/**
	 * 如果是JSON字段中的key
	 */
	private String dbJsonKey;

	/**
	 * 存储类型
	 */
	private DbStorageType storageType;

	/**
	 * JSON中的字段名
	 */
	private String jsonName;

	/**
	 * JSON默认值
	 */
	private String jsonDefaultValue;

	/**
	 * JSON中是否必填
	 */
	private boolean jsonRequired;

	/**
	 * 前端显示名称
	 */
	private String displayName;

	/**
	 * 字段描述
	 */
	private String displayDescription;

	/**
	 * 前端是否必填
	 */
	private boolean displayRequired;

	/**
	 * 前端控件类型
	 */
	private FieldType displayType;

	/**
	 * 选择项（用于下拉框等）
	 */
	private String[] displayOptions;

	/**
	 * 前端验证规则
	 */
	private String displayValidation;

	/**
	 * 前端显示顺序
	 */
	private int displayOrder;

	/**
	 * 业务逻辑是否必填
	 */
	private boolean businessRequired;

	/**
	 * 业务验证规则
	 */
	private String businessValidation;

	/**
	 * 是否支持动态配置
	 */
	private boolean dynamic;

	/**
	 * 字段显示条件，格式：fieldName=value1,value2
	 */
	private String displayCondition;

	/**
	 * 字段必填条件，格式：fieldName=value1,value2
	 */
	private String displayRequiredCondition;

	/**
	 * 字段类型
	 */
	private Class<?> fieldType;

	// Getters and Setters
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDbField() {
		return dbField;
	}

	public void setDbField(String dbField) {
		this.dbField = dbField;
	}

	public String getDbJsonKey() {
		return dbJsonKey;
	}

	public void setDbJsonKey(String dbJsonKey) {
		this.dbJsonKey = dbJsonKey;
	}

	public DbStorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(DbStorageType storageType) {
		this.storageType = storageType;
	}

	public String getJsonName() {
		return jsonName;
	}

	public void setJsonName(String jsonName) {
		this.jsonName = jsonName;
	}

	public String getJsonDefaultValue() {
		return jsonDefaultValue;
	}

	public void setJsonDefaultValue(String jsonDefaultValue) {
		this.jsonDefaultValue = jsonDefaultValue;
	}

	public boolean isJsonRequired() {
		return jsonRequired;
	}

	public void setJsonRequired(boolean jsonRequired) {
		this.jsonRequired = jsonRequired;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayDescription() {
		return displayDescription;
	}

	public void setDisplayDescription(String displayDescription) {
		this.displayDescription = displayDescription;
	}

	public boolean isDisplayRequired() {
		return displayRequired;
	}

	public void setDisplayRequired(boolean displayRequired) {
		this.displayRequired = displayRequired;
	}

	public FieldType getDisplayType() {
		return displayType;
	}

	public void setDisplayType(FieldType displayType) {
		this.displayType = displayType;
	}

	public String[] getDisplayOptions() {
		return displayOptions;
	}

	public void setDisplayOptions(String[] displayOptions) {
		this.displayOptions = displayOptions;
	}

	public String getDisplayValidation() {
		return displayValidation;
	}

	public void setDisplayValidation(String displayValidation) {
		this.displayValidation = displayValidation;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public boolean isBusinessRequired() {
		return businessRequired;
	}

	public void setBusinessRequired(boolean businessRequired) {
		this.businessRequired = businessRequired;
	}

	public String getBusinessValidation() {
		return businessValidation;
	}

	public void setBusinessValidation(String businessValidation) {
		this.businessValidation = businessValidation;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public String getDisplayCondition() {
		return displayCondition;
	}

	public void setDisplayCondition(String displayCondition) {
		this.displayCondition = displayCondition;
	}

	public String getDisplayRequiredCondition() {
		return displayRequiredCondition;
	}

	public void setDisplayRequiredCondition(String displayRequiredCondition) {
		this.displayRequiredCondition = displayRequiredCondition;
	}

	public Class<?> getFieldType() {
		return fieldType;
	}

	public void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		return "FieldMetadata{" + "fieldName='" + fieldName + '\'' + ", dbField='" + dbField + '\'' + ", dbJsonKey='"
				+ dbJsonKey + '\'' + ", storageType=" + storageType + ", jsonName='" + jsonName + '\''
				+ ", displayName='" + displayName + '\'' + ", displayType=" + displayType + ", displayOrder="
				+ displayOrder + '}';
	}

}
