package br.com.meritcoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
class MailService {
    private final JavaMailSender smtp;
    private final ObjectMapper json;
    private final HttpClient http=HttpClient.newHttpClient();
    @Value("${app.mail.brevo-key:}") String brevoKey;
    @Value("${app.mail.from:}") String from;
    MailService(JavaMailSender smtp,ObjectMapper json) { this.smtp=smtp; this.json=json; }

    void send(String to,String subject,String body) {
        if(from.isBlank()) throw new IllegalStateException("Configure MAIL_FROM para enviar emails.");
        try {
            if(!brevoKey.isBlank()) {
                String payload=json.writeValueAsString(Map.of("sender",Map.of("email",from),"to",new Object[]{Map.of("email",to)},"subject",subject,"textContent",body));
                HttpRequest request=HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key",brevoKey).header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
                HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
                if(response.statusCode()<200 || response.statusCode()>=300) throw new IllegalStateException("Serviço de email recusou o envio (HTTP "+response.statusCode()+").");
            } else {
                SimpleMailMessage message=new SimpleMailMessage();
                message.setFrom(from); message.setTo(to); message.setSubject(subject); message.setText(body);
                smtp.send(message);
            }
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt(); throw new IllegalStateException("Envio de email interrompido.",ex);
        } catch(Exception ex) { throw new IllegalStateException("Falha ao enviar email para "+to+": "+ex.getMessage(),ex); }
    }
}
