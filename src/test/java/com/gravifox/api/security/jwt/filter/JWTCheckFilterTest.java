//package com.tvb.api.jwt.security.filter;
//
//
//import com.tvb.api.jwt.security.filter.JWTCheckFilter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import static org.mockito.Mockito.*;
//
//@WebMvcTest(controllers = JWTCheckFilter.class)
//public class JWTCheckFilterTest {
//    @Autowired
//    private JWTCheckFilter filter;
//
//    private HttpServletRequest mockRequest;
//    private HttpServletResponse mockResponse;
//    private FilterChain mockFilterChain;
//
//
//    @BeforeEach
//    void setUp() {
//        mockRequest = mock(HttpServletRequest.class);
//        mockResponse = mock(HttpServletResponse.class);
//        mockFilterChain = mock(FilterChain.class);
//    }
//
//    @Test
//    void shouldReturnForbidden_whenNoAuthorizationHeader() throws ServletException, IOException {
//        PrintWriter writer = mock(PrintWriter.class);
//
//        when(mockResponse.getWriter()).thenReturn(writer);
//        when(mockRequest.getHeader("Authorization")).thenReturn(null);
//
//        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
//        verify(mockResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
//    }
//
//    @Test
//    void shouldReturnForbidden_whenInvalidAuthorizationHeader() throws ServletException, IOException {
//        PrintWriter writer = mock(PrintWriter.class);
//
//        when(mockResponse.getWriter()).thenReturn(writer);
//        when(mockRequest.getHeader("Authorization")).thenReturn("invalid token");
//
//        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
//        verify(mockResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
//    }
//}
