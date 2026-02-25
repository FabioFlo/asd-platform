package it.asd.identity.features.getperson;

import it.asd.identity.features.registerperson.PersonResponse;
import it.asd.identity.shared.repository.PersonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetPersonHandler {

    private final PersonRepository personRepository;

    public GetPersonHandler(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public GetPersonResult handle(GetPersonQuery query) {
        return personRepository.findById(query.personId())
                .map(e -> (GetPersonResult) new GetPersonResult.Found(PersonResponse.from(e)))
                .orElseGet(() -> new GetPersonResult.NotFound(query.personId()));
    }
}
