package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.products.Product;
import br.com.zup.edu.nossalojavirtual.shared.email.Email;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.format.DateTimeFormatter;

@Component
class SendPurchaseEmailConfirmation implements PostPurchaseAction {

    private final Logger logger = LoggerFactory.getLogger(SendPurchaseEmailConfirmation.class);
    private final EmailService emailService;

    SendPurchaseEmailConfirmation(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * do the action if purchase is confirmed
     *
     * @param postPaymentPurchase a success post payment purchase
     * @param uriBuilder build uri component
     */
    //TODO: Apply i18n in messages
    @Override
    public void execute(PostPaymentProcessedPurchase postPaymentPurchase, UriComponentsBuilder uriBuilder) {
        if (!postPaymentPurchase.isPaymentSuccessful()) {
            return;
        }

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyy hh:mm:ss");
        Product product = postPaymentPurchase.getProduct();

        String body = "Your " + postPaymentPurchase.getQuantity() + " product(s): " + product.getName() +
                      " is being prepared! Your purchase was confirmed at " + dateFormat.format(postPaymentPurchase.paymentConfirmedTime());

        Email email = Email.to(postPaymentPurchase.buyerEmail())
                .from(postPaymentPurchase.sellerEmail())
                .subject("Payment confirmed! Your product is being prepared")
                .body(body)
                .product(product)
                .build();

        emailService.send(email);
        logger.info("Email of purchase confirmation sent");
    }
}
