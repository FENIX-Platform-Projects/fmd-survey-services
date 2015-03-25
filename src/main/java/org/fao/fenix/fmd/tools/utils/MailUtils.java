package org.fao.fenix.fmd.tools.utils;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@ApplicationScoped
public class MailUtils {
    @Resource(mappedName="java:/mail/hoolifan")
    private Session mailSession;

    public void sendMail (String subject, String body, String ... toAddressList) throws MessagingException {
        MimeMessage m = new MimeMessage(mailSession);
        Address[] to = new InternetAddress[toAddressList.length];
        for (int i=0; i<to.length; i++)
            to[i] = new InternetAddress(toAddressList[i]);

        m.setRecipients(Message.RecipientType.TO, to);
        m.setSubject(subject);
        m.setSentDate(new Date());
        m.setContent(body,"text/plain");
        Transport.send(m);
    }
}
