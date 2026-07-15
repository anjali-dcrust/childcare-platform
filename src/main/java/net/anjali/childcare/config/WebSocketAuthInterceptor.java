package net.anjali.childcare.config;

import net.anjali.childcare.security.JwtUtil;
import net.anjali.childcare.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        System.out.println("Command = " + accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            System.out.println("CONNECT received");

            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            System.out.println("Authorization Header = " + authHeaders);

            if (authHeaders != null && !authHeaders.isEmpty()) {

                String header = authHeaders.get(0);
                String token = header.substring(7);

                String email = jwtUtil.extractEmail(token);
                System.out.println("Email = " + email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    accessor.setUser(authentication);

                    System.out.println("User set = " + accessor.getUser());
                }
            }
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            System.out.println("SEND User = " + accessor.getUser());
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            System.out.println("SUBSCRIBE User = " + accessor.getUser());
        }

        return message;
    }


}