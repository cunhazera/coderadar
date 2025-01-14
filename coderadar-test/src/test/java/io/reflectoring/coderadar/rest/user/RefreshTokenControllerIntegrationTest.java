package io.reflectoring.coderadar.rest.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.reflectoring.coderadar.graph.projectadministration.domain.RefreshTokenEntity;
import io.reflectoring.coderadar.graph.projectadministration.domain.UserEntity;
import io.reflectoring.coderadar.graph.projectadministration.user.repository.RefreshTokenRepository;
import io.reflectoring.coderadar.graph.projectadministration.user.repository.UserRepository;
import io.reflectoring.coderadar.projectadministration.port.driver.user.refresh.RefreshTokenCommand;
import io.reflectoring.coderadar.projectadministration.service.user.security.PasswordUtil;
import io.reflectoring.coderadar.projectadministration.service.user.security.SecretKeyService;
import io.reflectoring.coderadar.projectadministration.service.user.security.TokenService;
import io.reflectoring.coderadar.projectadministration.service.user.security.TokenType;
import io.reflectoring.coderadar.rest.ControllerTestTemplate;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;

import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class RefreshTokenControllerIntegrationTest extends ControllerTestTemplate {

  @Autowired private UserRepository userRepository;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private TokenService tokenService;

  @Autowired private SecretKeyService secretKeyService;

  @Test
  void refreshTokenSuccessfully() throws Exception {
    UserEntity testUser = new UserEntity();
    testUser.setUsername("radar");
    testUser.setPassword(PasswordUtil.hash("Password12!"));
    testUser = userRepository.save(testUser);

    RefreshTokenEntity userRefreshToken = new RefreshTokenEntity();
    userRefreshToken.setToken(
        tokenService.generateRefreshToken(testUser.getId(), testUser.getUsername()));
    userRefreshToken.setUser(testUser);
    refreshTokenRepository.save(userRefreshToken);

    RefreshTokenCommand command =
        new RefreshTokenCommand(createExpiredAccessToken(), userRefreshToken.getToken());

    mvc()
        .perform(
            post("/user/refresh").content(toJson(command)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("token").exists())
            .andDo(documentRefresh());
  }

  @Test
  void refreshTokenReturnsErrorOnInvalidToken() throws Exception {
    UserEntity testUser = new UserEntity();
    testUser.setUsername("radar");
    testUser.setPassword(PasswordUtil.hash("Password12!"));
    userRepository.save(testUser);

    RefreshTokenCommand command =
        new RefreshTokenCommand(createExpiredAccessToken(), "iqupiugapsfw");

    mvc()
        .perform(
            post("/user/refresh").content(toJson(command)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  void refreshTokenReturnsErrorOnNonExpiredAccessToken() throws Exception {
    UserEntity testUser = new UserEntity();
    testUser.setUsername("radar");
    testUser.setPassword(PasswordUtil.hash("Password12!"));
    testUser = userRepository.save(testUser);

    RefreshTokenEntity userRefreshToken = new RefreshTokenEntity();
    userRefreshToken.setToken(
        tokenService.generateRefreshToken(testUser.getId(), testUser.getUsername()));
    userRefreshToken.setUser(testUser);
    refreshTokenRepository.save(userRefreshToken);

    RefreshTokenCommand command =
        new RefreshTokenCommand(
            tokenService.generateAccessToken(testUser.getId(), testUser.getUsername()),
            userRefreshToken.getToken());

    mvc()
        .perform(
            post("/user/refresh").content(toJson(command)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  private String createExpiredAccessToken() {
    byte[] secret = secretKeyService.getSecretKey().getEncoded();

    Date expireAt = DateTime.now().minusMinutes(14).toDate();
    Date issuedAt = DateTime.now().minusMinutes(29).toDate();
    return JWT.create()
        .withExpiresAt(expireAt)
        .withIssuedAt(issuedAt)
        .withIssuer("coderadar")
        .withClaim("userId", 1)
        .withClaim("username", "radar")
        .withClaim("type", TokenType.ACCESS.toString())
        .sign(Algorithm.HMAC256(secret));
  }

  private ResultHandler documentRefresh() {
    ConstrainedFields fields = fields(RefreshTokenCommand.class);
    return document(
            "user/refresh",
            requestFields(
                    fields.withPath("accessToken").description("The expired access token"),
                    fields.withPath("refreshToken").description("The valid refresh token")));
  }
}
