package io.matel.controller;


import io.matel.model.Task;
import io.matel.model.Vocab;
import io.matel.repository.VocabRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/edu")
@CrossOrigin
public class VocabController {
    private VocabRepository vocabRepository;

    public VocabController(VocabRepository vocabRepository) {
        this.vocabRepository = vocabRepository;
    }

    @GetMapping("vocabs")
    public List<Vocab> vocabs() {
        return this.vocabRepository.findAll();
    }


}
