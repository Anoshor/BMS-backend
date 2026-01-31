package com.bms.backend.config;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DocuSignConfig {

    @Value("${docusign.enabled:false}")
    private boolean enabled;

    @Value("${docusign.integration-key}")
    private String integrationKey;

    @Value("${docusign.user-id}")
    private String userId;

    @Value("${docusign.account-id}")
    private String accountId;

    @Value("${docusign.base-path}")
    private String basePath;

    @Value("${docusign.oauth-base-path}")
    private String oauthBasePath;

    @Value("${docusign.private-key-path}")
    private Resource privateKeyResource;

    @Value("${docusign.webhook-secret}")
    private String webhookSecret;

    private static final int TOKEN_EXPIRY_SECONDS = 3600; // 1 hour

    public boolean isEnabled() {
        return enabled;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getBasePath() {
        return basePath;
    }

    public ApiClient getApiClient() throws Exception {
        if (!enabled) {
            throw new IllegalStateException("DocuSign integration is not enabled");
        }

        ApiClient apiClient = new ApiClient(basePath);
        apiClient.setOAuthBasePath(oauthBasePath);

        byte[] privateKeyBytes = readPrivateKey();

        List<String> scopes = new ArrayList<>();
        scopes.add(OAuth.Scope_SIGNATURE);
        scopes.add(OAuth.Scope_IMPERSONATION);

        OAuth.OAuthToken token = apiClient.requestJWTUserToken(
                integrationKey,
                userId,
                scopes,
                privateKeyBytes,
                TOKEN_EXPIRY_SECONDS
        );

        apiClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
        return apiClient;
    }

    private byte[] readPrivateKey() throws IOException {
        return privateKeyResource.getContentAsByteArray();
    }

    public ApiClient getApiClientWithToken(String accessToken, long expiresIn) {
        ApiClient apiClient = new ApiClient(basePath);
        apiClient.setAccessToken(accessToken, expiresIn);
        return apiClient;
    }
}
