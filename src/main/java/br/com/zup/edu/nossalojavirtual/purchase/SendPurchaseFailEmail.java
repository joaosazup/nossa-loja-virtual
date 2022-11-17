package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.shared.email.Email;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class SendPurchaseFailEmail implements PostPurchaseAction {

    private final Logger logger = LoggerFactory.getLogger(SendPurchaseFailEmail.class);
    private final EmailService emailService;

    SendPurchaseFailEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * do the action if payment was not confirmed
     *
     * @param postPaymentPurchase an unsuccessful post payment purchase
     * @param uriBuilder build uri component
     */
    @Override
    public void execute(PostPaymentProcessedPurchase postPaymentPurchase, UriComponentsBuilder uriBuilder) {
        if (postPaymentPurchase.isPaymentSuccessful()) {
            return;
        }

        var retryPaymentUrl = uriBuilder.path("/api/purchases/{id}")
                                        .buildAndExpand(postPaymentPurchase.getId())
                                        .toString();

        String body = "An error occurred when processing your payment, try again in this link: " + postPaymentPurchase.paymentUrl(retryPaymentUrl);
        Email email = Email.to(postPaymentPurchase.buyerEmail())
                .from(postPaymentPurchase.sellerEmail())
                .subject("Payment could not be confirmed")
                .body(body)
                .product(postPaymentPurchase.getProduct())
                .build();

        emailService.send(email);
        logger.info("Purchave fail email sent");
    }
}
