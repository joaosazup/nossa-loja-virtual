package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.purchase.SellersRankingClient.SellersRankingRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class SendPurchaseConfirmationToSellersSystem implements PostPurchaseAction {

    
    private final Logger logger = LoggerFactory.getLogger(SendPurchaseConfirmationToSellersSystem.class);
    private final SellersRankingClient sellersRankingClient;

    SendPurchaseConfirmationToSellersSystem(SellersRankingClient sellersRankingClient) {
        this.sellersRankingClient = sellersRankingClient;
    }

    /**
     * do the action if purchase is confirmed
     *
     * @param postPaymentPurchase a success post payment purchase
     * @param uriBuilder build uri component
     */
    @Override
    public void execute(PostPaymentProcessedPurchase postPaymentPurchase, UriComponentsBuilder uriBuilder) {
        if (!postPaymentPurchase.isPaymentSuccessful()) {
            return;
        }

        SellersRankingRequest request = new SellersRankingRequest(postPaymentPurchase.getId(), postPaymentPurchase.sellerEmail());
        sellersRankingClient.requestInvoice(request);
        logger.info("Confirmation sended to sellers system"); 
    }
}
