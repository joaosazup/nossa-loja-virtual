package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;
import java.util.function.Function;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PhotoUploader photoUploader;
    private final UserRepository userRepository;

    public ProductController(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            PhotoUploader photoUploader, UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.photoUploader = photoUploader;
        this.userRepository = userRepository;
    }

    @PostMapping
    ResponseEntity<?> create(
            @RequestBody @Valid NewProductRequest newProduct,
            @AuthenticationPrincipal(expression = "claims[email]") String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Usuário não econtrado"));
        Product product = newProduct.toProduct(photoUploader, categoryRepository::findCategoryById, user);
        productRepository.save(product);

        URI location = URI.create("/api/products/" + product.getId());
        return created(location).build();
    }

    @InitBinder(value = {"newProductRequest"})
    void initBinder(WebDataBinder binder) {
        binder.addValidators(
                new ObjectIsRegisteredValidator<>(
                        "categoryId",
                        "category.id.dontExist",
                        NewProductRequest.class,
                        categoryRepository::existsById
                ));
    }
}
