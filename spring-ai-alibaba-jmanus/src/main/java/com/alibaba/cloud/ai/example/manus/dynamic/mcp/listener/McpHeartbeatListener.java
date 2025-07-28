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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigEntity;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.repository.McpConfigRepository;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpServiceEntity;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.service.McpConnectionFactory;

import io.modelcontextprotocol.client.McpAsyncClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * MCP心跳监听器
 */
@Component
public class McpHeartbeatListener extends AbstractMcpListener {

	@Autowired
	private McpConnectionFactory connectionFactory;

	@Autowired
	private McpConfigRepository mcpConfigRepository;

	@Autowired
	private McpClientCleanupListener cleanupManager;

	private final ScheduledExecutorService heartbeatExecutor = java.util.concurrent.Executors.newScheduledThreadPool(2);

	private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

	private final Map<String, Long> lastHeartbeatTimes = new ConcurrentHashMap<>();

	@PostConstruct
	public void initListener() {
		init();
	}

	@PreDestroy
	public void shutdownListener() {
		shutdown();
	}

	@Override
	protected String getListenerName() {
		return "MCP Heartbeat Listener";
	}

	@Override
	protected boolean shouldStart() {
		return cacheProperties.isPreloadOnStartup() && cacheProperties.getHeartbeatInterval() > 0;
	}

	@Override
	protected void startListener() {
		startHeartbeatForAllServers();
	}

	@Override
	protected void stopListener() {
		stopAllHeartbeats();
		heartbeatExecutor.shutdown();
		try {
			if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				heartbeatExecutor.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			heartbeatExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 为所有MCP服务器启动心跳检测
	 */
	public void startHeartbeatForAllServers() {
		try {
			var configs = mcpConfigRepository.findAll();
			for (McpConfigEntity config : configs) {
				if (isHeartbeatSupported(config.getConnectionType())) {
					startHeartbeat(config.getMcpServerName());
				}
			}
		}
		catch (Exception e) {
			logger.error("Failed to start heartbeat for all servers", e);
		}
	}

	/**
	 * 启动指定服务器的心跳检测
	 * @param serverName 服务器名称
	 */
	public void startHeartbeat(String serverName) {
		if (heartbeatTasks.containsKey(serverName)) {
			logger.debug("Heartbeat already running for server: {}", serverName);
			return;
		}

		// 获取服务器配置
		McpConfigEntity config = mcpConfigRepository.findByMcpServerName(serverName);
		if (config == null) {
			logger.warn("Server configuration not found for heartbeat: {}", serverName);
			return;
		}

		ScheduledFuture<?> task = heartbeatExecutor.scheduleAtFixedRate(() -> {
			try {
				performHeartbeat(serverName, config);
			}
			catch (Exception e) {
				logger.error("Heartbeat task error for server: {}", serverName, e);
			}
		}, 0, cacheProperties.getHeartbeatInterval(), TimeUnit.SECONDS);

		heartbeatTasks.put(serverName, task);
		logger.info("Started heartbeat for server: {} with interval: {} seconds", serverName, cacheProperties.getHeartbeatInterval());
	}

	/**
	 * 停止指定服务器的心跳检测
	 * @param serverName 服务器名称
	 */
	public void stopHeartbeat(String serverName) {
		ScheduledFuture<?> task = heartbeatTasks.remove(serverName);
		if (task != null) {
			task.cancel(false);
			lastHeartbeatTimes.remove(serverName);
			logger.info("Stopped heartbeat for server: {}", serverName);
		}
	}

	/**
	 * 停止所有心跳检测
	 */
	public void stopAllHeartbeats() {
		heartbeatTasks.values().forEach(task -> task.cancel(false));
		heartbeatTasks.clear();
		lastHeartbeatTimes.clear();
		logger.info("Stopped all heartbeat tasks");
	}

	/**
	 * 执行心跳检测
	 * @param serverName 服务器名称
	 * @param config MCP配置
	 */
	private void performHeartbeat(String serverName, McpConfigEntity config) {
		try {
			logger.debug("Performing heartbeat for server: {}", serverName);
			
			boolean isHealthy = checkServerHealth(config);
			
			if (isHealthy) {
				updateConnectionStatus(serverName, ConnectionStatus.CONNECTED);
				lastHeartbeatTimes.put(serverName, System.currentTimeMillis());
				logger.debug("Heartbeat successful for server: {}", serverName);
			}
			else {
				logger.warn("Heartbeat failed for server: {}, attempting recovery", serverName);
				updateConnectionStatus(serverName, ConnectionStatus.ERROR);
				
				// 尝试恢复连接
				attemptRecovery(serverName, config);
			}
		}
		catch (Exception e) {
			logger.error("Heartbeat error for server: {}", serverName, e);
			updateConnectionStatus(serverName, ConnectionStatus.ERROR);
		}
	}

	/**
	 * 检查服务器健康状态
	 * @param config MCP配置
	 * @return true if healthy, false otherwise
	 */
	private boolean checkServerHealth(McpConfigEntity config) {
		try {
			String serverName = config.getMcpServerName();
			
			// 从缓存中获取服务映射
			Map<String, McpServiceEntity> services = cacheManager.getOrLoadServices("DEFAULT");
			if (services == null || services.isEmpty()) {
				logger.warn("No services found in cache for health check of server: {}", serverName);
				return false;
			}
			
			// 获取指定服务器的服务实体
			McpServiceEntity serviceEntity = services.get(serverName);
			if (serviceEntity == null) {
				logger.warn("Service entity not found in cache for server: {}", serverName);
				return false;
			}
			
			// 根据连接类型执行不同的健康检查
			switch (config.getConnectionType()) {
				case SSE:
				case STREAMING:
					return checkSseHealth(serviceEntity, serverName);
				case STUDIO:
					return checkStudioHealth(serviceEntity, serverName);
				default:
					logger.warn("Unknown connection type for health check: {}", config.getConnectionType());
					return false;
			}
		}
		catch (Exception e) {
			logger.error("Health check failed for server: {}", config.getMcpServerName(), e);
			return false;
		}
	}

	/**
	 * 检查SSE连接健康状态
	 * @param serviceEntity MCP服务实体
	 * @param serverName 服务器名称
	 * @return true if healthy, false otherwise
	 */
	private boolean checkSseHealth(McpServiceEntity serviceEntity, String serverName) {
		try {
			// 获取McpAsyncClient并调用ping指令
			if (serviceEntity.getMcpAsyncClient() != null) {
				return pingMcpClient(serviceEntity.getMcpAsyncClient(), serverName);
			}
			else {
				logger.warn("McpAsyncClient is null for server: {}", serverName);
				return false;
			}
		}
		catch (Exception e) {
			logger.debug("SSE health check failed for server: {}", serverName, e);
			return false;
		}
	}

	/**
	 * 检查Studio连接健康状态
	 * @param serviceEntity MCP服务实体
	 * @param serverName 服务器名称
	 * @return true if healthy, false otherwise
	 */
	private boolean checkStudioHealth(McpServiceEntity serviceEntity, String serverName) {
		try {
			// Studio连接通常是本地连接，检查McpAsyncClient是否可用
			if (serviceEntity.getMcpAsyncClient() != null) {
				return pingMcpClient(serviceEntity.getMcpAsyncClient(), serverName);
			}
			else {
				logger.warn("McpAsyncClient is null for server: {}", serverName);
				return false;
			}
		}
		catch (Exception e) {
			logger.debug("Studio health check failed for server: {}", serverName, e);
			return false;
		}
	}

	/**
	 * 对McpAsyncClient执行ping操作
	 * @param mcpAsyncClient MCP异步客户端
	 * @param serverName 服务器名称
	 * @return true if ping successful, false otherwise
	 */
	private boolean pingMcpClient(McpAsyncClient mcpAsyncClient, String serverName) {
		try {
			// 使用McpAsyncClient的ping方法进行健康检查
			// ping()方法返回Mono<Object>，我们需要阻塞等待结果
			Object pingResult = mcpAsyncClient.ping()
				.timeout(java.time.Duration.ofSeconds(5)) // 设置5秒超时
				.block(); // 阻塞等待ping结果
			
			if (pingResult != null) {
				logger.debug("Ping successful for server: {} - response: {}", serverName, pingResult);
				return true;
			}
			else {
				logger.debug("Ping failed for server: {} - null response", serverName);
				return false;
			}
			
		}
		catch (Exception e) {
			logger.debug("Ping failed for server: {} - {}", serverName, e.getMessage());
			return false;
		}
	}

	/**
	 * 尝试重新连接
	 * @param serverName 服务器名称
	 * @param config MCP配置
	 */
	private void attemptReconnect(String serverName, McpConfigEntity config) {
		logger.info("Attempting to reconnect to server: {}", serverName);
		
		try {
			// 从缓存中获取所有MCP服务
			Map<String, McpServiceEntity> services = cacheManager.getOrLoadServices("DEFAULT");
			if (services == null || services.isEmpty()) {
				logger.warn("No services found in cache for reconnection of server: {}", serverName);
				return;
			}
			
			// 获取对应的McpServiceEntity实例
			McpServiceEntity serviceEntity = services.get(serverName);
			if (serviceEntity == null) {
				logger.warn("Service entity not found in cache for server: {}", serverName);
				return;
			}
			
			// 获取旧的McpAsyncClient
			McpAsyncClient oldClient = serviceEntity.getMcpAsyncClient();
			
			// 重新创建McpServiceEntity（包含新的McpAsyncClient）
			McpServiceEntity newServiceEntity = connectionFactory.createConnection(config);
			if (newServiceEntity == null) {
				logger.error("Failed to create new McpServiceEntity for server: {}", serverName);
				return;
			}
			
			// 获取新的McpAsyncClient
			McpAsyncClient newClient = newServiceEntity.getMcpAsyncClient();
			if (newClient == null) {
				logger.error("New McpServiceEntity has null McpAsyncClient for server: {}", serverName);
				return;
			}
			
			// 测试新客户端的ping
			try {
				Object pingResult = newClient.ping()
					.timeout(java.time.Duration.ofSeconds(5))
					.block();
				
				if (pingResult != null) {
					logger.info("New McpAsyncClient ping successful for server: {}", serverName);
					
					// 更新缓存中的McpServiceEntity
					services.put(serverName, newServiceEntity);
					cacheManager.forceRefreshCache("DEFAULT");
					
					// 更新连接状态
					updateConnectionStatus(serverName, ConnectionStatus.CONNECTED);
					
					// 将旧的McpAsyncClient放入清理队列
					if (oldClient != null) {
						cleanupManager.scheduleCleanup(oldClient, serverName, 5); // 5分钟后清理
						logger.info("Scheduled cleanup for old McpAsyncClient of server: {}", serverName);
					}
					
					logger.info("Successfully reconnected to server: {}", serverName);
				}
				else {
					logger.warn("New McpAsyncClient ping failed for server: {}", serverName);
					// 将新客户端也放入清理队列
					cleanupManager.scheduleCleanup(newClient, serverName, 5);
				}
			}
			catch (Exception e) {
				logger.error("Ping test failed for new McpAsyncClient of server: {}", serverName, e);
				// 将新客户端放入清理队列
				cleanupManager.scheduleCleanup(newClient, serverName, 5);
			}
		}
		catch (Exception e) {
			logger.error("Reconnection error for server: {}", serverName, e);
		}
	}

	/**
	 * 尝试恢复连接
	 * @param serverName 服务器名称
	 * @param config MCP配置
	 */
	private void attemptRecovery(String serverName, McpConfigEntity config) {
		logger.info("Attempting to recover connection for server: {}", serverName);
		
		// 尝试获取刷新令牌
		if (!tryAcquireRefreshToken()) {
			logTokenAcquisitionFailed();
			return;
		}
		
		try {
			// 强制刷新缓存中的连接
			cacheManager.forceRefreshCache(serverName);
			
			// 检查恢复是否成功
			var serviceEntity = connectionFactory.createConnection(config);
			if (serviceEntity != null) {
				updateConnectionStatus(serverName, ConnectionStatus.CONNECTED);
				logger.info("Successfully recovered connection for server: {}", serverName);
			}
			else {
				logger.warn("Recovery failed for server: {}", serverName);
				// 如果恢复失败，则尝试重新连接
				attemptReconnect(serverName, config);
			}
		}
		catch (Exception e) {
			logger.error("Recovery error for server: {}", serverName, e);
			// 如果恢复失败，则尝试重新连接
			attemptReconnect(serverName, config);
		}
		finally {
			// 释放刷新令牌
			releaseRefreshToken();
		}
	}

	/**
	 * 检查连接类型是否支持心跳检测
	 * @param connectionType 连接类型
	 * @return true if supported, false otherwise
	 */
	private boolean isHeartbeatSupported(com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigType connectionType) {
		return com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigType.SSE.equals(connectionType) 
			|| com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigType.STREAMING.equals(connectionType);
	}

	/**
	 * 获取最后心跳时间
	 * @param serverName 服务器名称
	 * @return 最后心跳时间戳
	 */
	public Long getLastHeartbeatTime(String serverName) {
		return lastHeartbeatTimes.get(serverName);
	}

	/**
	 * 获取心跳统计信息
	 * @return 统计信息字符串
	 */
	public String getHeartbeatStats() {
		long activeTasks = heartbeatTasks.size();
		long totalServers = lastHeartbeatTimes.size();
		
		return String.format("Active heartbeat tasks: %d, Total servers: %d", activeTasks, totalServers);
	}
} 