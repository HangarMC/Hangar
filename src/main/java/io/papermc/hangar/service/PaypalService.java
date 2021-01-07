package io.papermc.hangar.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

@Service
public class PaypalService extends HangarService {

    private static final String paypalCallback = "https://ipnpb.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";
    private static final String paypalSandboxCallback = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";

    private HttpClient client;
    private boolean sandbox = true;

    public PaypalService() {
        client = HttpClient.newHttpClient();
    }

    public void handle(String ipn) throws URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URI((sandbox ? paypalSandboxCallback : paypalCallback) + ipn))
                .header("User-Agent", "Hangar")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            if (r.statusCode() == 200) {
                switch (r.body()) {
                    case "VERIFIED":
                        System.out.println("verified!");
                        MultiValueMap<String, String> parsedIpn = parseIpn(ipn);
                        System.out.println(parsedIpn);
                        break;
                    case "INVALID":
                        System.out.println("invalid");
                        break;
                    default:
                        System.out.println("error: " + r.statusCode() + " " + r.body());
                        break;
                }
            } else {
                System.out.println("error: " + r.statusCode() + " " + r.body());
            }
        });
    }

    public MultiValueMap<String, String> parseIpn(String ipn) {
        // FormHttpMessageConverter#read
        String[] pairs = StringUtils.tokenizeToStringArray(ipn, "&");
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(pairs.length);

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) {
                result.add(URLDecoder.decode(pair, Charset.defaultCharset()), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), Charset.defaultCharset());
                String value = URLDecoder.decode(pair.substring(idx + 1), Charset.defaultCharset());
                result.add(name, value);
            }
        }

        return result;
    }
}
