package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.hangar.MailConfig;
import io.papermc.hangar.model.internal.job.SendMailJob;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService extends HangarComponent {

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

    public void queueEmail(final String subject, final String recipient, final String text) {
        this.jobService.schedule(new SendMailJob(subject, recipient, text));
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
