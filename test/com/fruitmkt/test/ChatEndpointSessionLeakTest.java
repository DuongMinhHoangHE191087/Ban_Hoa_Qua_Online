package com.fruitmkt.test;

import com.fruitmkt.websocket.ChatEndpoint;
import org.junit.Test;

import jakarta.websocket.Session;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.*;

/**
 * ChatEndpointSessionLeakTest — verifies that @OnError removes the failed session
 * from ROOM_MAP, preventing the memory leak where errored sessions were never
 * evicted (only @OnClose removed them previously).
 *
 * Strategy: use reflection to reach the private static ROOM_MAP, seed it with a
 * mock Session, fire onError(), then assert the map is empty.
 *
 * No database, no network, no Spring context required.
 */
public class ChatEndpointSessionLeakTest {

    /** Key used by ChatEndpoint to stash the chat sessionId in ws UserProperties */
    private static final String SESSION_ID_KEY = "sessionId";
    private static final int ROOM_ID = 42;

    @Test
    public void onError_removesSessionFromRoomMap() throws Exception {
        // ---- Arrange ----
        ChatEndpoint endpoint = new ChatEndpoint();

        // Reach the private static ROOM_MAP via reflection
        Field roomMapField = ChatEndpoint.class.getDeclaredField("ROOM_MAP");
        roomMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Set<Session>> roomMap =
                (Map<Integer, Set<Session>>) roomMapField.get(null);

        // Seed the map with a mock session in room #42
        Map<String, Object> userProps = new java.util.HashMap<>();
        userProps.put(SESSION_ID_KEY, ROOM_ID);
        Session mockSession = createSessionMock(userProps);

        Set<Session> room = new CopyOnWriteArraySet<>();
        room.add(mockSession);
        roomMap.put(ROOM_ID, room);

        assertEquals("Pre-condition: room must contain the session before error",
                1, roomMap.get(ROOM_ID).size());

        // ---- Act ----
        // Simulate a transport-level error (network drop / timeout)
        Method onError = ChatEndpoint.class.getDeclaredMethod("onError", Session.class, Throwable.class);
        onError.setAccessible(true);
        onError.invoke(endpoint, mockSession, new RuntimeException("simulated network drop"));

        // ---- Assert ----
        assertFalse("Room #" + ROOM_ID + " must be evicted from ROOM_MAP after @OnError",
                roomMap.containsKey(ROOM_ID));
    }

    /**
     * Sanity check: ensure a second onError call on the same session is idempotent
     * (does not throw, map stays empty).
     */
    @Test
    public void onError_calledTwice_isIdempotent() throws Exception {
        ChatEndpoint endpoint = new ChatEndpoint();

        Field roomMapField = ChatEndpoint.class.getDeclaredField("ROOM_MAP");
        roomMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Set<Session>> roomMap =
                (Map<Integer, Set<Session>>) roomMapField.get(null);

        Map<String, Object> userProps = new java.util.HashMap<>();
        userProps.put(SESSION_ID_KEY, ROOM_ID);
        Session mockSession = createSessionMock(userProps);

        Set<Session> room = new CopyOnWriteArraySet<>();
        room.add(mockSession);
        roomMap.put(ROOM_ID, room);

        Method onError = ChatEndpoint.class.getDeclaredMethod("onError", Session.class, Throwable.class);
        onError.setAccessible(true);

        // First call removes it
        onError.invoke(endpoint, mockSession, new RuntimeException("first error"));
        // Second call on same session — must not throw
        onError.invoke(endpoint, mockSession, new RuntimeException("second error"));

        assertFalse("ROOM_MAP must remain empty after duplicate @OnError",
                roomMap.containsKey(ROOM_ID));
    }

    /**
     * Guard: @OnError on a session that was never in the map (e.g. auth failed
     * before @OnOpen added it) must not throw NullPointerException.
     */
    @Test
    public void onError_sessionNotInMap_doesNotThrow() throws Exception {
        ChatEndpoint endpoint = new ChatEndpoint();

        Map<String, Object> userProps = new java.util.HashMap<>();
        userProps.put(SESSION_ID_KEY, 999);
        Session mockSession = createSessionMock(userProps);

        Method onError = ChatEndpoint.class.getDeclaredMethod("onError", Session.class, Throwable.class);
        onError.setAccessible(true);

        // Must not throw any exception
        onError.invoke(endpoint, mockSession, new RuntimeException("error on unknown session"));
    }

    @SuppressWarnings("unchecked")
    private Session createSessionMock(Map<String, Object> userProps) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getUserProperties".equals(method.getName())) {
                return userProps;
            }
            if ("isOpen".equals(method.getName())) {
                return true;
            }
            if ("toString".equals(method.getName())) {
                return "MockSession";
            }
            Class<?> returnType = method.getReturnType();
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) return false;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0f;
            if (returnType == double.class) return 0d;
            if (returnType == char.class) return '\0';
            return null;
        };
        return (Session) Proxy.newProxyInstance(
                Session.class.getClassLoader(),
                new Class<?>[]{Session.class},
                handler);
    }
}
