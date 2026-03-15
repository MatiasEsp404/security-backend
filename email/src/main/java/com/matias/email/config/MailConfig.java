package com.matias.email.config;

import com.matias.email.properties.EmailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuración del JavaMailSender para envío de emails vía SMTP.
 */
@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(emailProperties.getSmtp().getHost());
        mailSender.setPort(emailProperties.getSmtp().getPort());

        if (emailProperties.getSmtp().getUsername() != null) {
            mailSender.setUsername(emailProperties.getSmtp().getUsername());
        }
        if (emailProperties.getSmtp().getPassword() != null) {
            mailSender.setPassword(emailProperties.getSmtp().getPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", emailProperties.getSmtp().isAuth());
        props.put("mail.smtp.starttls.enable", emailProperties.getSmtp().isStarttlsEnable());
        props.put("mail.debug", "false");

        return mailSender;
    }
}
