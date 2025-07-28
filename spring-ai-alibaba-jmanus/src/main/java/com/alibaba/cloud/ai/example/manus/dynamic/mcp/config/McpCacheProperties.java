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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP缓存配置属性
 */
@Component
@ConfigurationProperties("mcp.cache")
public class McpCacheProperties {

	/**
	 * 是否启动时全量加载缓存
	 */
	private boolean preloadOnStartup = true;

	/**
	 * 心跳检测间隔（秒），0表示不启用心跳
	 */
	private int heartbeatInterval = 30;

	/**
	 * 全量自动刷新间隔（秒），0表示不启用自动刷新
	 */
	private int autoRefreshInterval = 600; // 10分钟

	/**
	 * 缓存获取时的锁等待时间（秒）
	 */
	private int lockWaitTimeout = 3;

	/**
	 * 缓存获取重试次数
	 */
	private int retryCount = 3;

	// Getters and Setters
	public boolean isPreloadOnStartup() {
		return preloadOnStartup;
	}

	public void setPreloadOnStartup(boolean preloadOnStartup) {
		this.preloadOnStartup = preloadOnStartup;
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public int getAutoRefreshInterval() {
		return autoRefreshInterval;
	}

	public void setAutoRefreshInterval(int autoRefreshInterval) {
		this.autoRefreshInterval = autoRefreshInterval;
	}

	public int getLockWaitTimeout() {
		return lockWaitTimeout;
	}

	public void setLockWaitTimeout(int lockWaitTimeout) {
		this.lockWaitTimeout = lockWaitTimeout;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
} 