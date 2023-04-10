package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.hangar.MailConfig;
import io.papermc.hangar.model.internal.job.SendMailJob;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService extends HangarComponent {

    public enum MailType {
        EMAIL_CHANGED("Hangar Email Changed", "email-changed.html"),
        USERNAME_CHANGED("Hangar Username Changed", "username-changed.html"),
        PASSWORD_CHANGED("Hangar Password Changed", "password-changed.html"),
        PASSWORD_RESET("Hangar Password Reset", "password-reset.html"),
        EMAIL_CONFIRMATION("Hangar Email Verification", "email-verification.html");

        final String subject;
        final String text;

        MailType(final String subject, final String file) {
            this.subject = subject;
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("mail-templates/" + file))))) {
                this.text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (final IOException e) {
                throw new RuntimeException("Error while loading text for mail template " + this.name(), e);
            }
        }
    }

    private final JavaMailSender mailSender;
    private final JobService jobService;

    public MailService(final JobService jobService, final MailConfig mailConfig) {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailConfig.host());
        mailSender.setPort(mailConfig.port());
        mailSender.setUsername(mailConfig.user());
        mailSender.setPassword(mailConfig.pass());
        mailSender.setProtocol("smtps");

        final Properties props = new Properties();
        props.put("mail.smtps.connectiontimeout", "5000");
        props.put("mail.smtps.timeout", "5000");
        if (mailConfig.dev()) {
            props.put("mail.smtps.ssl.checkserveridentity", "false");
            props.put("mail.smtps.ssl.trust", "*");
            // props.put("mail.debug", "true");
        }
        mailSender.setJavaMailProperties(props);

        this.mailSender = mailSender;
        this.jobService = jobService;
    }

    public void queueMail(final MailType type, final String recipient, final Map<String, String> placeholders) {
        String text = type.text;
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replaceAll("%" + entry.getKey() + "%", entry.getValue());
        }
        this.jobService.schedule(new SendMailJob(type.subject, recipient, text));
    }

    public void sendMail(final String subject, final String recipient, final String text) throws MessagingException {
        final MimeMessage message = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(this.config.mail.from());
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(text, true);
        this.mailSender.send(message);
    }
}
