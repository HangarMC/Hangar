package io.papermc.hangar.components.index.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

@Component
public class WebhookMessageConverter extends AbstractHttpMessageConverter<List<Webhook>> {
    private final ObjectMapper objectMapper;

    public WebhookMessageConverter(ObjectMapper objectMapper) {
        super(MediaType.APPLICATION_NDJSON);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(@NotNull Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected @NotNull List<Webhook> readInternal(final @NotNull Class<? extends List<Webhook>> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        String encoding = inputMessage.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);

        InputStream body = inputMessage.getBody();
        if (encoding != null && encoding.contains("gzip")) {
            body = new GZIPInputStream(body);
        }
        return new BufferedReader(new InputStreamReader(body))
            .lines()
            .map(line -> {
                try {
                    return objectMapper.readValue(line, Webhook.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    protected void writeInternal(final @NotNull List<Webhook> webhooks, final @NotNull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("Not supported");
    }
}
