package com.ChineseChess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @Class: WebSocket
 * @Description: websocket类，用于测试，需要写一个测试脚本
 * @author 孔翰文
 */
@ServerEndpoint(value = "/ChineseChess/{player}")
public class Websocket {
	private Session session;
	private String player;
	private static boolean isPlaying =false; // 是否正在游戏中
	private static Map<String, Websocket> map; // 用于保存各个玩家对应的一组session，服务器主动发送数据时，会根据主机编号给该编号下的所有的session发送数据
	static {
		map = new HashMap<String, Websocket>();
	}

	// 连接时执行
	@OnOpen
	public void onOpen(@PathParam("player") String player, Session session) throws Exception {
		this.player = player;
		this.session = session;
		if (!map.containsKey(player)) {
			map.put(player, this);
			System.out.println("连入玩家 " + player + "  当前有 " + map.size() + " 个玩家");
			if (map.size() == 2) {
				startGame();
			}
		} else {
			Websocket websocketTest = (Websocket) map.get(player);
			websocketTest.session.close();
			map.remove(player);
			map.put(player, this);
			System.out.println("重新新连入玩家：" + player);
		}
	}

	// 关闭时执行
	@OnClose
	public void onClose() throws IOException {
		if (map.containsKey(player)) {
			Websocket websocketTest = (Websocket) map.get(player);
			websocketTest.session.close();
			map.remove(player);
			System.out.println("断开玩家 " + player + "  还剩下 " + map.size() + " 个玩家");
		}
		isPlaying = false;
	}

	// 收到消息时执行
	@OnMessage
	public void onMessage(String message, Session session) throws Exception {
		// 同样需要将消息转换为json格式（map格式）
		Map messageMap;
		if (message.equals(""))
			messageMap = new HashMap();
		else
			messageMap = JsonPluginsUtil.jsonToMap(message);
		switch (messageMap.get("message_type").toString()) {
			case "move": // 如果是移动棋子
				String detail = messageMap.get("message_detail").toString();
				System.out.println("玩家" + player + "走子：" + detail);
				String[] strings = detail.split("-");
				 int col = 10 - Integer.parseInt(strings[1]);
				 int row = 11 - Integer.parseInt(strings[2]);
				 String newDetail = String.format("\"%s-%d-%d\"", strings[0], col, row);
				 movePiece(player, newDetail);
				 break;
			default:
				break;
		}
	}

	// 连接错误时执行
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("玩家：" + this.player + "出现连接错误：");
		System.out.println(error);
	}

	public static boolean sendMessage(String player, String message) {
		if (map.containsKey(player)) {
			try {
				map.get(player).session.getBasicRemote().sendText(message);
				return true;
			} catch (IOException e) {
				System.out.println("发送给玩家消息失败: " + player);
				return false;
			}
		} else {
			System.out.println("websocket没有连上: " + player);
			return false;
		}
	}
	
	public static void startGame() throws Exception {
		if (isPlaying)
			return;
		isPlaying = true;
		List<String> keys = new ArrayList<>(map.keySet());
		String message = "";
		message = "{"
				+ "\"message_type\" : \"player_type\""
				+ " , "
				+ "\"message_detail\" : \"red\""
				+ "}";
		map.get(keys.get(0)).session.getBasicRemote().sendText(message);
		message = "{"
				+ "\"message_type\" : \"player_type\""
				+ " , "
				+ "\"message_detail\" : \"black\""
				+ "}";
		map.get(keys.get(1)).session.getBasicRemote().sendText(message);
	}
	
	public static void movePiece(String currentPlayer, String detail) throws Exception {
		if (!isPlaying)
			return;
		List<String> keys = new ArrayList<>(map.keySet());
		// 发送给除当前玩家外的其他玩家
		for (String key : keys) {
			if (key.equals(currentPlayer))
				continue;
			String message = "{"
					+ "\"message_type\" : \"move\""
					+ ","
					+ "\"message_detail\" : " + detail
					+ "}";
			map.get(key).session.getBasicRemote().sendText(message);
			break;
		}
	}
}

