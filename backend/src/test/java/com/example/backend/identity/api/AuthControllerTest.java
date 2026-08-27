package com.example.backend.identity.api;

import com.example.backend.identity.internal.application.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginParsesRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "studentIndex": "IN 20/2021",
                            "password": "password123"
                        }
                        """));

        verify(authService).login(
                new LoginRequest(
                        "IN 20/2021",
                        "password123"
                )
        );
    }

    @Test
    void loginReturnsTokensForValidRequest() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse(
                        "access-token",
                        "Bearer",
                        900,
                        "refresh-token",
                        2_592_000
                ));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "studentIndex": "IN 20/2021",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh-token"))
                .andExpect(jsonPath("$.refreshExpiresIn")
                        .value(2_592_000));
    }

    @Test
    void refreshParsesRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "refreshToken": "refresh-token"
                        }
                        """));

        verify(authService).refresh(
                new RefreshRequest("refresh-token")
        );
    }

    @Test
    void refreshReturnsRotatedTokenForValidRequest() throws Exception {
        when(authService.refresh(any(RefreshRequest.class)))
                .thenReturn(new TokenResponse(
                        "new-access-token",
                        "Bearer",
                        900,
                        "new-refresh-token",
                        2_591_900
                ));

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "refreshToken": "refresh-token"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900))
                .andExpect(jsonPath("$.refreshToken")
                        .value("new-refresh-token"))
                .andExpect(jsonPath("$.refreshExpiresIn")
                        .value(2_591_900));
    }

    @Test
    void logoutParsesRequest() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "refreshToken": "refresh-token"
                        }
                        """))
                .andExpect(status().isNoContent());

        verify(authService).logout(
                new RefreshRequest("refresh-token")
        );
    }

    @Test
    void loginReturnsBadRequestWhenStudentIndexIsBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "studentIndex": "",
                        "password": "password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshReturnsBadRequestWhenRefreshTokenIsBlank() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "refreshToken": ""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
}
