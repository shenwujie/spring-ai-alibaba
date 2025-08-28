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
 * 数据库存储类型枚举
 */
public enum DbStorageType {

	/**
	 * 直接存储到数据库字段
	 */
	DIRECT,

	/**
	 * 存储到JSON字段的指定key
	 */
	JSON_KEY,

	/**
	 * 计算字段，不存储
	 */
	COMPUTED,

	/**
	 * 虚拟字段，用于展示
	 */
	VIRTUAL

}
