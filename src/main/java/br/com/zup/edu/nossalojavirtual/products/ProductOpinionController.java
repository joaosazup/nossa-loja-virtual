package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/opinions")
class ProductOpinionController {

    private final Logger logger = LoggerFactory.getLogger(ProductOpinionController.class);
    private final ProductOpinionRepository productOpinionRepository;
    private final ProductRepository productRepository;

    public ProductOpinionController(ProductOpinionRepository productOpinionRepository,
                                    ProductRepository productRepository) {
        this.productOpinionRepository = productOpinionRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody @Valid NewOpinionRequest newOpinion,
                             User user // TODO: Injetar usuário autenticado
                            ) {

        var opinion = newOpinion.toProductOpinion(productRepository::findById, user);
        productOpinionRepository.save(opinion);

        URI location = URI.create("/api/opinions/" + opinion.getId());
        logger.info("opinion created to product of id: {}", newOpinion.getProductId());
        return created(location).build();
    }

    @InitBinder(value = { "newOpinionRequest" })
    void initBinder(WebDataBinder binder) {
        binder.addValidators(new ObjectIsRegisteredValidator<>("productId",
                "product.id.dontExist",
                NewOpinionRequest.class,
                productRepository::existsById));
    }

}
