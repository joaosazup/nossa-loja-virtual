package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.products.ProductRepository;
import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/purchase")
class PurchaseController {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    PurchaseController(ProductRepository productRepository,
                       PurchaseRepository purchaseRepository) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> buy(@RequestBody @Valid NewPurchaseRequest newPurchase,
                                 User buyer, // TODO: Injetar o usuário autenticado
                                 UriComponentsBuilder uriBuilder) throws BindException {

        var product = productRepository.findById(newPurchase.getProductId()).get();

        Optional<Purchase> possiblePurchase = product.reserveQuantityFor(newPurchase, buyer);

        if (possiblePurchase.isEmpty()) {
            BindException bindException = new BindException(new Object(), "");
            bindException.reject("purchase.product.outOfStock", "This product is out of stock");
            logger.info("Attempt to purchase the product {} without stock", product.getId());
            throw bindException;
        }

        Purchase purchase = possiblePurchase.get();
        purchaseRepository.save(purchase);

        var redirectUrl = uriBuilder.path("/api/purchases/confirm-payment")
                                    .buildAndExpand(purchase.getId())
                                    .toString();

        String paymentUrl = purchase.paymentUrl(redirectUrl);


        var response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        logger.info("Purchase done with id: {}", purchase.getId());

        return ok(response);
    }

    @InitBinder(value = { "newPurchaseRequest" })
    void initBinder(WebDataBinder binder) {

        binder.addValidators(
                new ObjectIsRegisteredValidator<>("productId",
                        "product.id.dontExist",
                        NewPurchaseRequest.class,
                        productRepository::existsById));
    }
}
