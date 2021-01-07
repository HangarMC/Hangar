package io.papermc.hangar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class PaypalService extends HangarService {

    private static final Logger logger = LoggerFactory.getLogger(PaypalService.class);

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
                        Map<String, String> parsedIpn = parseIpn(ipn);
                        logger.info("Verified ipn: {}", parsedIpn);
                        handleIpn(parsedIpn);
                        break;
                    case "INVALID":
                        logger.info("Invalid ipn: {}", ipn);

                        break;
                    default:
                        logger.error("Error while verifying ipn {}, {}", r.statusCode(), r.body());
                        logger.error("IPN was {}", ipn);
                        break;
                }
            } else {
                logger.error("Error while verifying ipn {}, {}", r.statusCode(), r.body());
                logger.error("IPN was {}", ipn);
            }
        });
    }

    public void handleIpn(Map<String, String> ipn) {
        // check that recipient is a hangar author
        String receiver = ipn.get("receiver_email");
        logger.info("Receiver is: {}", receiver);
        // check that transaction id and last payment status isnt already here
        String transactionId = ipn.get("txn_id");
        logger.info("transaction id is {}", transactionId);
        String paymentStatus = ipn.get("payment_status");
        // check that status is completed
        if (paymentStatus.equals("Completed")) {
            // precede
            logger.info("Status was completed");
        } else {
            // send notification
            logger.info("Status: {}", paymentStatus);
            return;
        }
        // check the amount and currency
        String amount = ipn.get("mc_gross");
        String fee = ipn.get("mc_fee");
        String currency = ipn.get("mc_currency");
        logger.info("Currency {} amount {} fee {}", currency, amount, fee);
        // check that its not a test_ipn
        String test = ipn.get("test_ipn");
        if (test != null && test.equals("1")) {
            logger.info("Was test ipn");
        }
        // check that payer is a hangar user and has a pending payment
        String payer = ipn.get("payer_email");
        logger.info("Payer {}", payer);
        // read custom
        String custom = ipn.get("custom");
        logger.info("Custom {}", custom);
    }

    public Map<String, String> parseIpn(String ipn) {
        // FormHttpMessageConverter#read
        String[] pairs = StringUtils.tokenizeToStringArray(ipn, "&");
        Map<String, String> result = new HashMap<>(pairs.length);

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) {
                result.put(URLDecoder.decode(pair, Charset.defaultCharset()), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), Charset.defaultCharset());
                String value = URLDecoder.decode(pair.substring(idx + 1), Charset.defaultCharset());
                result.put(name, value);
            }
        }

        return result;
    }
}
