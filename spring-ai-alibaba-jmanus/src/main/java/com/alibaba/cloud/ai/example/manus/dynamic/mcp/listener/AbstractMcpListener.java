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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.config.McpCacheProperties;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.service.McpCacheManager;

/**
 * MCP监听器抽象基类
 * 提供公共方法和变量，供所有MCP监听器继承使用
 */
public abstract class AbstractMcpListener {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected McpCacheProperties cacheProperties;

	@Autowired
	protected McpCacheManager cacheManager;

	// 运行状态控制
	protected final AtomicBoolean running = new AtomicBoolean(false);

	// 刷新令牌管理
	private final AtomicBoolean refreshToken = new AtomicBoolean(false);
	private volatile String currentTokenHolder = null;
	private volatile LocalDateTime lastTokenAcquireTime = null;

	// 连接状态监控
	private final Map<String, ConnectionStatus> connectionStatuses = new ConcurrentHashMap<>();
	private final Map<String, LocalDateTime> lastUpdateTimes = new ConcurrentHashMap<>();

	public enum ConnectionStatus {
		CONNECTED, DISCONNECTED, CONNECTING, ERROR
	}

	/**
	 * 获取监听器名称
	 * @return 监听器名称
	 */
	protected abstract String getListenerName();

	/**
	 * 检查是否应该启动监听器
	 * @return true if should start, false otherwise
	 */
	protected abstract boolean shouldStart();

	/**
	 * 启动监听器
	 */
	protected abstract void startListener();

	/**
	 * 停止监听器
	 */
	protected abstract void stopListener();

	/**
	 * 初始化监听器
	 */
	public void init() {
		if (shouldStart()) {
			logger.info("Starting {} (preload enabled)", getListenerName());
			running.set(true);
			startListener();
		}
		else {
			logger.info("{} is disabled", getListenerName());
		}
	}

	/**
	 * 关闭监听器
	 */
	public void shutdown() {
		if (!shouldStart()) {
			logger.info("{} was not started, skipping shutdown", getListenerName());
			return;
		}

		logger.info("Shutting down {}...", getListenerName());
		running.set(false);
		stopListener();
	}

	/**
	 * 检查监听器是否正在运行
	 * @return true if running, false otherwise
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * 获取监听器状态信息
	 * @return 状态信息字符串
	 */
	public String getStatus() {
		if (isRunning()) {
			return String.format("%s: Running", getListenerName());
		}
		else {
			return String.format("%s: Stopped", getListenerName());
		}
	}

	// ==================== 刷新令牌管理方法 ====================

	/**
	 * 尝试获取刷新令牌
	 * @param requester 请求者名称
	 * @return true if token acquired, false otherwise
	 */
	public boolean tryAcquireToken(String requester) {
		boolean acquired = refreshToken.compareAndSet(false, true);
		if (acquired) {
			currentTokenHolder = requester;
			lastTokenAcquireTime = LocalDateTime.now();
			logger.info("Refresh token acquired by: {} at {}", requester, lastTokenAcquireTime);
		}
		else {
			logger.info("Refresh token acquisition failed for: {} (currently held by: {} since {})", 
				requester, currentTokenHolder, lastTokenAcquireTime);
		}
		return acquired;
	}

	/**
	 * 释放刷新令牌
	 * @param requester 请求者名称
	 */
	public void releaseToken(String requester) {
		if (refreshToken.compareAndSet(true, false)) {
			String previousHolder = currentTokenHolder;
			currentTokenHolder = null;
			lastTokenAcquireTime = null;
			logger.info("Refresh token released by: {} (was held by: {})", requester, previousHolder);
		}
		else {
			logger.warn("Attempted to release token by: {} but token was not held", requester);
		}
	}

	/**
	 * 检查令牌是否被持有
	 * @return true if token is held, false otherwise
	 */
	public boolean isTokenHeld() {
		return refreshToken.get();
	}

	/**
	 * 获取当前令牌持有者
	 * @return 当前令牌持有者名称，如果未被持有则返回null
	 */
	public String getCurrentTokenHolder() {
		return currentTokenHolder;
	}

	/**
	 * 获取最后获取令牌的时间
	 * @return 最后获取令牌的时间，如果未被持有则返回null
	 */
	public LocalDateTime getLastTokenAcquireTime() {
		return lastTokenAcquireTime;
	}

	/**
	 * 强制重置令牌状态（用于紧急情况）
	 */
	public void forceResetToken() {
		refreshToken.set(false);
		currentTokenHolder = null;
		lastTokenAcquireTime = null;
		logger.warn("Refresh token force reset");
	}

	/**
	 * 获取令牌状态信息
	 * @return 令牌状态描述
	 */
	public String getTokenStatus() {
		if (isTokenHeld()) {
			return String.format("Token held by: %s since: %s", currentTokenHolder, lastTokenAcquireTime);
		}
		else {
			return "Token available";
		}
	}

	// ==================== 连接状态监控方法 ====================

	/**
	 * 更新连接状态
	 * @param serverName 服务器名称
	 * @param status 连接状态
	 */
	public void updateConnectionStatus(String serverName, ConnectionStatus status) {
		connectionStatuses.put(serverName, status);
		lastUpdateTimes.put(serverName, LocalDateTime.now());
		logger.debug("Updated connection status for server '{}': {}", serverName, status);
	}

	/**
	 * 获取连接状态
	 * @param serverName 服务器名称
	 * @return 连接状态
	 */
	public ConnectionStatus getConnectionStatus(String serverName) {
		return connectionStatuses.getOrDefault(serverName, ConnectionStatus.DISCONNECTED);
	}

	/**
	 * 获取最后更新时间
	 * @param serverName 服务器名称
	 * @return 最后更新时间
	 */
	public LocalDateTime getLastUpdateTime(String serverName) {
		return lastUpdateTimes.get(serverName);
	}

	/**
	 * 检查连接是否可用
	 * @param serverName 服务器名称
	 * @return true if connected, false otherwise
	 */
	public boolean isConnected(String serverName) {
		return getConnectionStatus(serverName) == ConnectionStatus.CONNECTED;
	}

	/**
	 * 获取所有连接状态
	 * @return 连接状态映射
	 */
	public Map<String, ConnectionStatus> getAllConnectionStatuses() {
		return new ConcurrentHashMap<>(connectionStatuses);
	}

	/**
	 * 清除服务器状态
	 * @param serverName 服务器名称
	 */
	public void removeConnectionStatus(String serverName) {
		connectionStatuses.remove(serverName);
		lastUpdateTimes.remove(serverName);
		logger.debug("Removed connection status for server '{}'", serverName);
	}

	/**
	 * 清除所有连接状态
	 */
	public void clearAllConnectionStatuses() {
		connectionStatuses.clear();
		lastUpdateTimes.clear();
		logger.debug("Cleared all connection statuses");
	}

	/**
	 * 获取连接统计信息
	 * @return 统计信息字符串
	 */
	public String getConnectionStatistics() {
		long connected = connectionStatuses.values().stream()
				.filter(status -> status == ConnectionStatus.CONNECTED)
				.count();
		long total = connectionStatuses.size();

		return String.format("Total: %d, Connected: %d, Disconnected: %d", total, connected, total - connected);
	}

	// ==================== 便捷方法 ====================

	/**
	 * 尝试获取刷新令牌（使用当前监听器名称）
	 * @return true if token acquired, false otherwise
	 */
	protected boolean tryAcquireRefreshToken() {
		return tryAcquireToken(getListenerName());
	}

	/**
	 * 释放刷新令牌（使用当前监听器名称）
	 */
	protected void releaseRefreshToken() {
		releaseToken(getListenerName());
	}

	/**
	 * 检查刷新令牌是否被持有
	 * @return true if token is held, false otherwise
	 */
	protected boolean isRefreshTokenHeld() {
		return isTokenHeld();
	}

	/**
	 * 获取当前刷新令牌持有者
	 * @return 当前持有者名称
	 */
	protected String getCurrentTokenHolderName() {
		return getCurrentTokenHolder();
	}

	/**
	 * 记录令牌获取失败日志
	 */
	protected void logTokenAcquisitionFailed() {
		logger.info("Skipping {} - refresh token held by: {}", getListenerName(), getCurrentTokenHolderName());
	}
} 