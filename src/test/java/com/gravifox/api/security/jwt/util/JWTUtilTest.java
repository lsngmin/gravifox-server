//package com.tvb.api.jwt.security.util;
//
//
//
//import com.tvb.api.jwt.security.util.JWTUtil;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.Map;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@ExtendWith(SpringExtension.class)  // JUnit 5에서는 @ExtendWith 사용
//@SpringBootTest
//public class JWTUtilTest {
//    @Autowired
//    private JWTUtil jwtUtil;
//
//    @Test
//    public void testCreateToken() {
//        Map<String, String> valueMap = Map.of("email", "test@test.com");
//        String token = jwtUtil.createToken(valueMap, 10);
//        System.out.println(token);
//        assertNotNull(token);
//    }
//
//    @Test
//    public void testValidateToken() {
//        Map<String, String> valueMap = Map.of("email", "test@test.com");
//        String token = jwtUtil.createToken(valueMap, 10);
//        Map<String, Object> claims = jwtUtil.validateToken(token);
//        assertEquals("test@test.com", claims.get("email"));
//        System.out.println(claims);
//    }
//}
