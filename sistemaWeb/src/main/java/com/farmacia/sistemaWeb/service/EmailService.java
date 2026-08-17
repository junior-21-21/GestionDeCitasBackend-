package com.farmacia.sistemaWeb.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCredenciales(String destinatario, String nombres, String password, String rol) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("Bienvenido a Veterinaria Petizoos - Credenciales de acceso");

            String html = """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8fafc;">
                            <!-- Header -->
                            <div style="background: linear-gradient(135deg, #3f51b5, #303f9f); padding: 30px 25px; border-radius: 12px 12px 0 0; text-align: center;">
                                <h1 style="color: white; margin: 0; font-size: 24px;"> Veterinaria Petizoos</h1>
                                <p style="color: rgba(255,255,255,0.8); margin: 8px 0 0; font-size: 14px;">Sistema de Gestión</p>
                            </div>

                            <!-- Body -->
                            <div style="background: white; padding: 30px 25px; border: 1px solid #e2e8f0; border-top: none;">
                                <h2 style="color: #1e293b; margin: 0 0 10px; font-size: 20px;">¡Hola, %s! </h2>
                                <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin: 0 0 20px;">
                                    Tu cuenta ha sido creada exitosamente en el sistema. A continuación encontrarás tus credenciales de acceso:
                                </p>

                                <!-- Credentials Box -->
                                <div style="background: #f1f5f9; border-radius: 10px; padding: 20px; border-left: 4px solid #3f51b5; margin-bottom: 20px;">
                                    <table style="width: 100%%; border-collapse: collapse;">
                                        <tr>
                                            <td style="padding: 8px 0; color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">📧 Correo:</td>
                                            <td style="padding: 8px 0; color: #1e293b; font-size: 15px; font-weight: 700;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">🔑 Contraseña:</td>
                                            <td style="padding: 8px 0; color: #1e293b; font-size: 15px; font-weight: 700; font-family: 'Courier New', monospace; background: #e2e8f0; padding: 6px 10px; border-radius: 6px; display: inline-block;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">🏷️ Rol:</td>
                                            <td style="padding: 8px 0;"><span style="background: #3f51b5; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;">%s</span></td>
                                        </tr>
                                    </table>
                                </div>

                                <!-- Warning -->
                                <div style="background: #fff7ed; border: 1px solid #fed7aa; border-radius: 8px; padding: 12px 16px; margin-bottom: 20px;">
                                    <p style="color: #c2410c; font-size: 13px; margin: 0;">
                                        <strong>Importante:</strong> Si el usuario y contraseña no son correctos, por favor, contacta a tu administrador.
                                    </p>
                                </div>
                            </div>

                            <!-- Footer -->
                            <div style="background: #f1f5f9; padding: 20px 25px; border-radius: 0 0 12px 12px; border: 1px solid #e2e8f0; border-top: none; text-align: center;">
                                <p style="color: #94a3b8; font-size: 12px; margin: 0;">
                                    Este es un mensaje automático del sistema Veterinaria Petizoos.<br>
                                    © 2026 Veterinaria Petizoos - Todos los derechos reservados
                                </p>
                            </div>
                        </div>
                    """
                    .formatted(nombres, destinatario, password, rol);

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email de credenciales enviado a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email a " + destinatario + ": " + e.getMessage());
            // No lanzamos excepción para no bloquear el registro del usuario
        }
    }

    public void enviarEnlaceResetPassword(String destinatario, String nombres, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("Restablecer Contraseña - Veterinaria PetyZoos");

            // URL del frontend para recuperar contraseña
            String urlReset = "http://localhost:4200/reset-password?token=" + token;

            String html = """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8fafc;">
                            <div style="background: linear-gradient(135deg, #0d9488, #0f766e); padding: 30px 25px; border-radius: 12px 12px 0 0; text-align: center;">
                                <h1 style="color: white; margin: 0; font-size: 24px;">Veterinaria PetyZoos</h1>
                            </div>
                            <div style="background: white; padding: 30px 25px; border: 1px solid #e2e8f0; border-top: none;">
                                <h2 style="color: #1e293b; margin: 0 0 10px; font-size: 20px;">¡Hola, %s!</h2>
                                <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin: 0 0 20px;">
                                    Hemos recibido una solicitud para restablecer tu contraseña. Si no fuiste tú, puedes ignorar este correo.
                                </p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s" style="background: #0d9488; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">
                                        Restablecer Contraseña
                                    </a>
                                </div>
                                <p style="color: #64748b; font-size: 13px; line-height: 1.6; margin: 0;">
                                    Este enlace expirará en 1 hora.
                                </p>
                            </div>
                        </div>
                    """.formatted(nombres, urlReset);

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email de reset de password enviado a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de reset a " + destinatario + ": " + e.getMessage());
        }
    }
}
