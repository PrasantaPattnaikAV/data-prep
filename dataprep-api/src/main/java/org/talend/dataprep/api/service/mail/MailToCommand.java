package org.talend.dataprep.api.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Component
@Scope("request")
public class MailToCommand extends HystrixCommand<Void> {

    public static final HystrixCommandGroupKey MAIL_GROUP = HystrixCommandGroupKey.Factory.asKey("mail"); //$NON-NLS-1$

    @Autowired
    private FeedbackSender feedbackSender;

    private final MailDetails mailDetails;

    private MailToCommand(MailDetails mailDetails) {
        super(MAIL_GROUP);
        this.mailDetails = mailDetails;
    }

    @Override
    protected Void run() throws Exception {
        // TODO: Retrieve and send version with the feedback info object
        try {
            String body = "Version=0.10 BETA<br/>";
            body += "Sender=" + mailDetails.getMail() + "<br/>";
            body += "Type=" + mailDetails.getType() + "<br/>";
            body += "Severity=" + mailDetails.getSeverity() + "<br/>";
            body += "Description=" + mailDetails.getDescription() + "<br/>";
            feedbackSender.send(mailDetails.getTitle(), body);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_SEND_MAIL, e);
        }
        return null;
    }
}