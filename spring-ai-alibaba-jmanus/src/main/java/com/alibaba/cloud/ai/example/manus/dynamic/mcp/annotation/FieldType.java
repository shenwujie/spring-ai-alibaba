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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.annotation;

/**
 * 前端控件类型枚举
 */
public enum FieldType {

	/**
	 * 文本输入框
	 */
	TEXT,

	/**
	 * 多行文本
	 */
	TEXTAREA,

	/**
	 * 数字输入框
	 */
	NUMBER,

	/**
	 * 下拉选择框
	 */
	SELECT,

	/**
	 * 多选下拉框
	 */
	MULTI_SELECT,

	/**
	 * 复选框
	 */
	CHECKBOX,

	/**
	 * 单选框
	 */
	RADIO,

	/**
	 * 密码输入框
	 */
	PASSWORD,

	/**
	 * URL输入框
	 */
	URL,

	/**
	 * 邮箱输入框
	 */
	EMAIL,

	/**
	 * 日期选择器
	 */
	DATE,

	/**
	 * 日期时间选择器
	 */
	DATETIME,

	/**
	 * JSON编辑器
	 */
	JSON_EDITOR,

	/**
	 * 环境变量编辑器
	 */
	ENV_EDITOR

}
