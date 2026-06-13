package com.vtmer.microteachingquality.util;


import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

/**
 * @ClassName MailUtils
 * @Description 邮件发送工具
 */
@Component
public class MailUtils {

    /**
     * 发送纯文本邮件信息
     *
     * @param to      接收方
     * @param subject 邮件主题
     * @param content 邮件内容（发送内容）
     */
    public static void sendMessage(String to, String subject, String content) {
        Properties prop = new Properties();
        //协议
        prop.setProperty("mail.transport.protocol", "smtp");
        //服务器
        prop.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
        //端口
        prop.setProperty("mail.smtp.port", "465");
        //使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        //使用SSL，企业邮箱必需！
        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        //
        //获取Session对象
        Session s = Session.getDefaultInstance(prop, new Authenticator() {
            //此访求返回用户和密码的对象
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                /**
                 * 暂时理解成魔法值了，企业邮箱授权码随时可能会变
                 */
                PasswordAuthentication pa = new PasswordAuthentication("3120004759@mail2.gdut.edu.cn", "qn39838duP5GhYab");
                return pa;
            }
        });
        //设置session的调试模式，发布时取消
        s.setDebug(true);
        MimeMessage mimeMessage = new MimeMessage(s);
        try {
            //mimeMessage.setFrom(new InternetAddress(from,from));
            mimeMessage.setFrom("3120004759@mail2.gdut.edu.cn");
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            //设置主题
            mimeMessage.setSubject(subject);
            mimeMessage.setSentDate(new Date());
            //设置内容
            mimeMessage.setText(content);
            mimeMessage.saveChanges();
            //发送
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }


}


