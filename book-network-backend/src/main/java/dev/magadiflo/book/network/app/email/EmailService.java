package dev.magadiflo.book.network.app.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(String to, String username, EmailTemplateName emailTemplateName,
                          String confirmationUrl, String activationCode, String subject) throws MessagingException {

        String templateName = emailTemplateName == null ? "confirm-email" : emailTemplateName.getName();

        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("confirmationUrl", confirmationUrl);
        variables.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(variables);

        helper.setFrom("contact@magadiflo.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String template = this.templateEngine.process(templateName, context);

        helper.setText(template, true);

        this.javaMailSender.send(mimeMessage);
    }
}