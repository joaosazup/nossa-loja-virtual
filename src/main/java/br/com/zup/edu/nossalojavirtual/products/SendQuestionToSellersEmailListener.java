package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.shared.email.Email;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailRepository;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SendQuestionToSellersEmailListener {

    private final Logger logger = LoggerFactory.getLogger(SendQuestionToSellersEmailListener.class);
    private final EmailService sendEmail;
    private final EmailRepository emailRepository;

    SendQuestionToSellersEmailListener(EmailService sendEmail,
                                       EmailRepository emailRepository) {
        this.sendEmail = sendEmail;
        this.emailRepository = emailRepository;
    }

    @EventListener
    void listen(QuestionEvent questionEvent) {

        // TODO: Apply I18N in this messages?
        var subject = " You have a new question";
        var body = questionEvent.getTitle() + " in " + questionEvent.getProductUri();

        Email email = Email.to(questionEvent.getSellersEmail())
                           .from(questionEvent.getPossibleBuyer())
                           .subject(subject)
                           .body(body)
                           .product(questionEvent.getProduct())
                           .build();

        sendEmail.send(email);
        emailRepository.save(email);
        logger.info("Email of the question sent to listeners");
    }
}
