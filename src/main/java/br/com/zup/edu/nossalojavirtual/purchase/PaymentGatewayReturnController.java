package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Set;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/purchases/confirm-payment")
class PaymentGatewayReturnController {

    private final Logger logger = LoggerFactory.getLogger(PaymentGatewayReturnController.class);
    private final PurchaseRepository purchaseRepository;
    private final Set<PostPurchaseAction> postPurchaseActions;

    PaymentGatewayReturnController(PurchaseRepository purchaseRepository,
                                   Set<PostPurchaseAction> postPurchaseActions) {
        this.purchaseRepository = purchaseRepository;
        this.postPurchaseActions = postPurchaseActions;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> confirmPayment(@RequestBody @Valid PaymentReturn paymentReturn,
                                            UriComponentsBuilder uriBuilder) throws BindException {

        var purchase = purchaseRepository.findById(paymentReturn.getPurchaseId()).get();

        PostPaymentProcessedPurchase postPaymentPurchase = purchase.process(paymentReturn);

        postPurchaseActions.forEach(action -> action.execute(postPaymentPurchase, uriBuilder));

        return ok().build();
    }

    @InitBinder(value = { "paymentReturn" })
    void initBinder(WebDataBinder binder) {
        binder.addValidators(
                new ObjectIsRegisteredValidator<>("purchaseId",
                        "purchase.id.dontExist",
                        PaymentReturn.class,
                        purchaseRepository::existsById));
    }
}
