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
 * @Description: websocket�࣬���ڲ��ԣ���Ҫдһ�����Խű�
 * @author �׺���
 */
@ServerEndpoint(value = "/ChineseChess/{player}")
public class Websocket {
	private Session session;
	private String player;
	private static boolean isPlaying =false; // �Ƿ�������Ϸ��
	private static Map<String, Websocket> map; // ���ڱ��������Ҷ�Ӧ��һ��session��������������������ʱ�������������Ÿ��ñ���µ����е�session��������
	static {
		map = new HashMap<String, Websocket>();
	}

	// ����ʱִ��
	@OnOpen
	public void onOpen(@PathParam("player") String player, Session session) throws Exception {
		this.player = player;
		this.session = session;
		if (!map.containsKey(player)) {
			map.put(player, this);
			System.out.println("������� " + player + "  ��ǰ�� " + map.size() + " �����");
			if (map.size() == 2) {
				startGame();
			}
		} else {
			Websocket websocketTest = (Websocket) map.get(player);
			websocketTest.session.close();
			map.remove(player);
			map.put(player, this);
			System.out.println("������������ң�" + player);
		}
	}

	// �ر�ʱִ��
	@OnClose
	public void onClose() throws IOException {
		if (map.containsKey(player)) {
			Websocket websocketTest = (Websocket) map.get(player);
			websocketTest.session.close();
			map.remove(player);
			System.out.println("�Ͽ���� " + player + "  ��ʣ�� " + map.size() + " �����");
		}
		isPlaying = false;
	}

	// �յ���Ϣʱִ��
	@OnMessage
	public void onMessage(String message, Session session) throws Exception {
		// ͬ����Ҫ����Ϣת��Ϊjson��ʽ��map��ʽ��
		Map messageMap;
		if (message.equals(""))
			messageMap = new HashMap();
		else
			messageMap = JsonPluginsUtil.jsonToMap(message);
		switch (messageMap.get("message_type").toString()) {
			case "move": // ������ƶ�����
				String detail = messageMap.get("message_detail").toString();
				System.out.println("���" + player + "���ӣ�" + detail);
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

	// ���Ӵ���ʱִ��
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("��ң�" + this.player + "�������Ӵ���");
		System.out.println(error);
	}

	public static boolean sendMessage(String player, String message) {
		if (map.containsKey(player)) {
			try {
				map.get(player).session.getBasicRemote().sendText(message);
				return true;
			} catch (IOException e) {
				System.out.println("���͸������Ϣʧ��: " + player);
				return false;
			}
		} else {
			System.out.println("websocketû������: " + player);
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
		// ���͸�����ǰ�������������
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

