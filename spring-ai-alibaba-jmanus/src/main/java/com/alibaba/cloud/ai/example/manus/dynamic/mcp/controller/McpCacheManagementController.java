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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener.McpAutoRefreshListener;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.service.McpCacheManager;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener.AbstractMcpListener;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener.McpHeartbeatListener;

/**
 * MCP缓存管理控制器
 */
@RestController
@RequestMapping("/api/mcp/cache")
public class McpCacheManagementController {

	@Autowired
	private McpCacheManager cacheManager;

	@Autowired
	private AbstractMcpListener connectionMonitor;

	@Autowired
	private McpHeartbeatListener heartbeatService;

	@Autowired
	private McpAutoRefreshListener autoRefreshService;

	/**
	 * 获取缓存统计信息
	 * @return 缓存统计信息
	 */
	@GetMapping("/stats")
	public ResponseEntity<Map<String, String>> getCacheStats() {
		Map<String, String> stats = Map.of(
			"cacheStats", cacheManager.getCacheStats(),
			"connectionStats", connectionMonitor.getConnectionStatistics(),
			"heartbeatStats", heartbeatService.getHeartbeatStats(),
			"autoRefreshStats", autoRefreshService.getAutoRefreshStats()
		);
		return ResponseEntity.ok(stats);
	}

	/**
	 * 获取连接状态
	 * @return 所有连接状态
	 */
	@GetMapping("/connections")
	public ResponseEntity<Map<String, AbstractMcpListener.ConnectionStatus>> getConnectionStatuses() {
		return ResponseEntity.ok(connectionMonitor.getAllConnectionStatuses());
	}

	/**
	 * 获取指定服务器的连接状态
	 * @param serverName 服务器名称
	 * @return 连接状态
	 */
	@GetMapping("/connections/{serverName}")
	public ResponseEntity<AbstractMcpListener.ConnectionStatus> getConnectionStatus(@PathVariable String serverName) {
		return ResponseEntity.ok(connectionMonitor.getConnectionStatus(serverName));
	}

	/**
	 * 获取指定服务器的心跳时间
	 * @param serverName 服务器名称
	 * @return 最后心跳时间
	 */
	@GetMapping("/heartbeat/{serverName}")
	public ResponseEntity<Long> getLastHeartbeatTime(@PathVariable String serverName) {
		Long lastHeartbeat = heartbeatService.getLastHeartbeatTime(serverName);
		return ResponseEntity.ok(lastHeartbeat != null ? lastHeartbeat : 0L);
	}

	/**
	 * 手动刷新缓存
	 * @param planId 计划ID（可选）
	 * @return 刷新结果
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refreshCache() {
		cacheManager.forceRefreshCache("DEFAULT");
		return ResponseEntity.ok(Map.of("message", "Cache refresh triggered successfully"));
	}

	/**
	 * 手动刷新指定计划的缓存
	 * @param planId 计划ID
	 * @return 刷新结果
	 */
	@PostMapping("/refresh/{planId}")
	public ResponseEntity<Map<String, String>> refreshCache(@PathVariable String planId) {
		cacheManager.forceRefreshCache(planId);
		return ResponseEntity.ok(Map.of("message", "Cache refresh triggered successfully for plan: " + planId));
	}

	/**
	 * 触发自动刷新
	 * @return 刷新结果
	 */
	@PostMapping("/auto-refresh")
	public ResponseEntity<Map<String, String>> triggerAutoRefresh() {
		autoRefreshService.triggerManualRefresh();
		return ResponseEntity.ok(Map.of("message", "Auto-refresh triggered successfully"));
	}

	/**
	 * 启动指定服务器的心跳检测
	 * @param serverName 服务器名称
	 * @return 操作结果
	 */
	@PostMapping("/heartbeat/{serverName}/start")
	public ResponseEntity<Map<String, String>> startHeartbeat(@PathVariable String serverName) {
		heartbeatService.startHeartbeat(serverName);
		return ResponseEntity.ok(Map.of("message", "Heartbeat started for server: " + serverName));
	}

	/**
	 * 停止指定服务器的心跳检测
	 * @param serverName 服务器名称
	 * @return 操作结果
	 */
	@PostMapping("/heartbeat/{serverName}/stop")
	public ResponseEntity<Map<String, String>> stopHeartbeat(@PathVariable String serverName) {
		heartbeatService.stopHeartbeat(serverName);
		return ResponseEntity.ok(Map.of("message", "Heartbeat stopped for server: " + serverName));
	}

	/**
	 * 检查缓存是否正在刷新
	 * @return 刷新状态
	 */
	@GetMapping("/refreshing")
	public ResponseEntity<Map<String, Boolean>> isRefreshing() {
		return ResponseEntity.ok(Map.of("refreshing", cacheManager.isRefreshing()));
	}

	/**
	 * 检查自动刷新是否正在运行
	 * @return 自动刷新状态
	 */
	@GetMapping("/auto-refresh/running")
	public ResponseEntity<Map<String, Boolean>> isAutoRefreshRunning() {
		return ResponseEntity.ok(Map.of("running", autoRefreshService.isAutoRefreshRunning()));
	}
} 