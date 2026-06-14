package test;

import service.chat.ChatService;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

public class ChatServiceTest {

    private ChatService chatService;

    @Before
    public void setUp() {
        chatService = new ChatService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrCreateSession_invalidCustomerId_throws() throws Exception {
        chatService.getOrCreateSession(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrCreateSession_invalidOwnerId_throws() throws Exception {
        chatService.getOrCreateSession(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrCreateSession_sameUserId_throws() throws Exception {
        chatService.getOrCreateSession(1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_invalidSessionId_throws() throws Exception {
        chatService.sendMessage(0, 1, "Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_invalidSenderId_throws() throws Exception {
        chatService.sendMessage(1, -1, "Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_blankContent_throws() throws Exception {
        chatService.sendMessage(1, 1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_nullContent_throws() throws Exception {
        chatService.sendMessage(1, 1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_onlyWhitespace_throws() throws Exception {
        chatService.sendMessage(1, 1, "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_htmlTagsOnly_throws() throws Exception {
        chatService.sendMessage(1, 1, "<script>alert('xss')</script>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void markRead_invalidSessionId_throws() throws Exception {
        chatService.markRead(0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void markRead_invalidReaderId_throws() throws Exception {
        chatService.markRead(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMessages_invalidSessionId_throws() throws Exception {
        chatService.getMessages(0);
    }
}
