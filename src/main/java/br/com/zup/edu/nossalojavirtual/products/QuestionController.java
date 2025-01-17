package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.users.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.notFound;

@RestController
@RequestMapping("/api/products/{id}/questions")
class QuestionController {

    private final ProductRepository productRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher publisher;

    QuestionController(ProductRepository productRepository,
                       QuestionRepository questionRepository,
                       ApplicationEventPublisher publisher) {
        this.productRepository = productRepository;
        this.questionRepository = questionRepository;
        this.publisher = publisher;
    }

    @PostMapping
    ResponseEntity<?> askQuestion(@PathVariable("id") UUID id,
                                  @RequestBody @Valid NewQuestionRequest newQuestion,
                                  User user, // TODO: Injetar usuário autenticado
                                  UriComponentsBuilder uriBuilder) {

        Optional<Product> possibleProduct = productRepository.findById(id);

        if (possibleProduct.isEmpty()) {
            return notFound().build();
        }

        Product product = possibleProduct.get();
        var question = newQuestion.toQuestion(user, product);
        questionRepository.save(question);

        publisher.publishEvent(new QuestionEvent(question, uriBuilder));

        var location = URI.create("/api/products/" + id.toString() + "/questions/" + question.getId());
        List<Question> questions = questionRepository.findByProduct(possibleProduct.get());

        List<QuestionResponse> response = QuestionResponse.from(questions);

        return created(location).body(response);

    }
}
