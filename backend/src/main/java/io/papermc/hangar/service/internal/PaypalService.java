package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaypalService extends HangarComponent {

    private static final String PAYPAL_CALLBACK = "https://ipnpb.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";
    private static final String PAYPAL_SANDBOX_CALLBACK = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";

    private final HttpClient client;
    private final boolean sandbox = true;

    public PaypalService() {
        this.client = HttpClient.newHttpClient();
    }

    public void handle(final String ipn) throws URISyntaxException {
        final HttpRequest request = HttpRequest
            .newBuilder(new URI((this.sandbox ? PAYPAL_SANDBOX_CALLBACK : PAYPAL_CALLBACK) + ipn))
            .header("User-Agent", "Hangar")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .GET()
            .build();

        this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            if (r.statusCode() == 200) {
                switch (r.body()) {
                    case "VERIFIED":
                        final Map<String, String> parsedIpn = this.parseIpn(ipn);
                        this.logger.info("Verified ipn: {}", parsedIpn);
                        this.handleIpn(parsedIpn);
                        break;
                    case "INVALID":
                        this.logger.info("Invalid ipn: {}", ipn);

                        break;
                    default:
                        this.logger.error("Error while verifying ipn {}, {}", r.statusCode(), r.body());
                        this.logger.error("IPN was {}", ipn);
                        break;
                }
            } else {
                this.logger.error("Error while verifying ipn {}, {}", r.statusCode(), r.body());
                this.logger.error("IPN was {}", ipn);
            }
        });
    }

    public void handleIpn(final Map<String, String> ipn) {
        // check that recipient is a hangar author
        final String receiver = ipn.get("receiver_email");
        this.logger.info("Receiver is: {}", receiver);
        // check that transaction id and last payment status isnt already here
        final String transactionId = ipn.get("txn_id");
        this.logger.info("transaction id is {}", transactionId);
        final String paymentStatus = ipn.get("payment_status");
        // check that status is completed
        if (paymentStatus.equals("Completed")) {
            // precede
            this.logger.info("Status was completed");
        } else {
            // send notification
            this.logger.info("Status: {}", paymentStatus);
            return;
        }
        // check the amount and currency
        final String amount = ipn.get("mc_gross");
        final String fee = ipn.get("mc_fee");
        final String currency = ipn.get("mc_currency");
        this.logger.info("Currency {} amount {} fee {}", currency, amount, fee);
        // check that its not a test_ipn
        final String test = ipn.get("test_ipn");
        if (test != null && test.equals("1")) {
            this.logger.info("Was test ipn");
        }
        // check that payer is a hangar user and has a pending payment
        final String payer = ipn.get("payer_email");
        this.logger.info("Payer {}", payer);
        // read custom
        final String custom = ipn.get("custom");
        this.logger.info("Custom {}", custom);
    }

    public Map<String, String> parseIpn(final String ipn) {
        // FormHttpMessageConverter#read
        final String[] pairs = StringUtils.tokenizeToStringArray(ipn, "&");
        final Map<String, String> result = new HashMap<>(pairs.length);

        for (final String pair : pairs) {
            final int idx = pair.indexOf("=");
            if (idx == -1) {
                result.put(URLDecoder.decode(pair, Charset.defaultCharset()), null);
            } else {
                final String name = URLDecoder.decode(pair.substring(0, idx), Charset.defaultCharset());
                final String value = URLDecoder.decode(pair.substring(idx + 1), Charset.defaultCharset());
                result.put(name, value);
            }
        }

        return result;
    }
}
