package com.gravifox.security.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String keyId;

    public JwtService(@Value("${jwt.upload.private-key:}") String privateKeyPem,
                      @Value("${jwt.upload.public-key:}") String publicKeyPem,
                      @Value("${jwt.upload.key-id:}") String configuredKeyId) {
        KeyPair keyPair = loadKeyPair(privateKeyPem, publicKeyPem);
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.keyId = resolveKeyId(configuredKeyId, publicKey);
    }

    public String issueUploadToken(String uploadId, String jti, Duration ttl, Instant issuedAt) {
        Instant expiresAt = issuedAt.plus(ttl);

        return Jwts.builder()
                .header()
                .add("kid", keyId)
                .add("typ", "JWT")
                .and()
                .id(jti)
                .issuer("gravifox-upload")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims()
                .add("uploadId", uploadId)
                .add("jti", jti)
                .and()
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public UploadTokenClaims parseUploadToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String uploadId = claims.get("uploadId", String.class);
            String jti = claims.get("jti", String.class);
            Date expiration = claims.getExpiration();

            if (!StringUtils.hasText(uploadId) || !StringUtils.hasText(jti) || expiration == null) {
                throw new IllegalArgumentException("필수 클레임이 누락됐어요.");
            }

            return new UploadTokenClaims(uploadId, jti, expiration.toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            throw e;
        }
    }

    public List<Map<String, Object>> jwks() {
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("kid", keyId);
        jwk.put("n", base64Url(publicKey.getModulus()));
        jwk.put("e", base64Url(publicKey.getPublicExponent()));
        return List.of(jwk);
    }

    private KeyPair loadKeyPair(String privateKeyPem, String publicKeyPem) {
        try {
            if (StringUtils.hasText(privateKeyPem)) {
                RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);
                RSAPublicKey publicKey = StringUtils.hasText(publicKeyPem)
                        ? parsePublicKey(publicKeyPem)
                        : derivePublicFromPrivate(privateKey);
                return new KeyPair(publicKey, privateKey);
            }

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            log.warn("Generated ephemeral RSA key pair for upload tokens. Provide jwt.upload.private-key to persist keys.");
            return pair;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize RSA key pair", e);
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
        byte[] content = decodePem(pem);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(content));
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("지원하지 않는 RSA 개인키 형식이에요. PKCS#8 형식을 사용해주세요.", ex);
        }
    }

    private RSAPublicKey parsePublicKey(String pem) throws GeneralSecurityException {
        byte[] content = decodePem(pem);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(content));
    }

    private RSAPublicKey derivePublicFromPrivate(RSAPrivateKey privateKey) throws GeneralSecurityException {
        if (privateKey instanceof RSAPrivateCrtKey crtKey) {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent()));
        }
        throw new IllegalStateException("공개키를 찾을 수 없어요. jwt.upload.public-key를 설정해주세요.");
    }

    private byte[] decodePem(String pem) {
        String normalized = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resolveKeyId(String configuredKeyId, RSAPublicKey publicKey) {
        if (StringUtils.hasText(configuredKeyId)) {
            return configuredKeyId;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getEncoded());
            byte[] truncated = Arrays.copyOf(hash, 8);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(truncated);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없어요.", e);
        }
    }
}
