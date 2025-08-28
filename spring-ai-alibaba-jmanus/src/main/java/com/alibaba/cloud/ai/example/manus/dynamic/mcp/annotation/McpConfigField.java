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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP配置字段注解，用于声明式配置字段的各种属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpConfigField {

	/**
	 * 对应的数据库字段名
	 */
	String dbField() default "";

	/**
	 * 如果是JSON字段中的key
	 */
	String dbJsonKey() default "";

	/**
	 * 存储类型
	 */
	DbStorageType storageType() default DbStorageType.DIRECT;

	/**
	 * JSON中的字段名
	 */
	String jsonName() default "";

	/**
	 * JSON默认值
	 */
	String jsonDefaultValue() default "";

	/**
	 * JSON中是否必填
	 */
	boolean jsonRequired() default false;

	/**
	 * 前端显示名称
	 */
	String displayName() default "";

	/**
	 * 字段描述
	 */
	String displayDescription() default "";

	/**
	 * 前端是否必填
	 */
	boolean displayRequired() default false;

	/**
	 * 前端控件类型
	 */
	FieldType displayType() default FieldType.TEXT;

	/**
	 * 选择项（用于下拉框等）
	 */
	String[] displayOptions() default {};

	/**
	 * 前端验证规则
	 */
	String displayValidation() default "";

	/**
	 * 前端显示顺序
	 */
	int displayOrder() default 0;

	/**
	 * 业务逻辑是否必填
	 */
	boolean businessRequired() default false;

	/**
	 * 业务验证规则
	 */
	String businessValidation() default "";

	/**
	 * 是否支持动态配置
	 */
	boolean dynamic() default false;

	/**
	 * 字段显示条件，格式：fieldName=value1,value2 例如：connectionType=STUDIO 表示只在连接类型为STUDIO时显示
	 */
	String displayCondition() default "";

	/**
	 * 字段必填条件，格式：fieldName=value1,value2 例如：connectionType=STUDIO 表示只在连接类型为STUDIO时必填
	 */
	String displayRequiredCondition() default "";

}
