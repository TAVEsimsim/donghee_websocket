//package tave.websocket.chatserver.chat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final WebSocketHandler webSocketHandler;
//
//    public WebSocketConfig(WebSocketHandler webSocketHandler) {
//        this.webSocketHandler = webSocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        // /connect Url 로  webSocket 연결 요청이 들어오면, 핸들러 클래스가 처리
//        registry.addHandler(webSocketHandler, "/connect")
//        //securityconfig에서의 cors 예외는 http에 대한 예외 -> 따라서 websocket (ws)프로토콜에 대한 cors 처리도 따로 필요
//            .setAllowedOrigins("http://localhost:3000");
//    }
//
//}
