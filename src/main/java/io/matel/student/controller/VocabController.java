package io.matel.student.controller;


import io.matel.assistant.model.Task;
import io.matel.common.service.SearchService;
import io.matel.student.model.Vocab;
import io.matel.student.repository.VocabRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/edu")
@CrossOrigin
public class VocabController {
    private VocabRepository vocabRepository;
    private SearchService searchService;

    public VocabController(VocabRepository vocabRepository, SearchService searchService) {
        this.vocabRepository = vocabRepository;
        this.searchService = searchService;
    }

    @GetMapping("vocabs")
    public List<Vocab> getVocabs() {
        List<Vocab> list =  this.vocabRepository.findAll();
        list.sort((Vocab e1, Vocab e2) -> e1.getName().compareTo(e2.getName()));
        return list;
    }

    @PostMapping("vocabs")
    public Vocab addVocab(@RequestBody Vocab vocab) {
        vocab.setVocabName(Character.toUpperCase(vocab.getName().charAt(0))+vocab.getName().substring(1));
        System.out.println(vocab.toString());
        return this.vocabRepository.save(vocab);
    }

    @DeleteMapping("vocab/delete")
    public boolean deleteVocab(@RequestParam String id){
        long id_ = Long.valueOf(id);
        if(this.vocabRepository.findById(id_).isPresent()) {
            this.vocabRepository.delete(this.vocabRepository.findById(id_).get());
            return true;
        }else{
            return false;
        }
    }

    @PutMapping("vocabs/edit")
    public void editVocab(@RequestBody Vocab vocab){
        this.vocabRepository.save(vocab);
        System.out.println(vocab.toString());
    }

    @GetMapping("vocabs/search")
    public List<Vocab> searchVocabs(@RequestParam String request) {
        if(request.equals("")){
            List<Vocab> list =  this.vocabRepository.findAll();
            list.sort((Vocab e1, Vocab e2) -> e1.getName().compareTo(e2.getName()));
            return list;
        }else{
            List<Vocab> results = this.searchService.search(request, "name", Vocab.class);
            results.forEach(System.out::println);
            return results;
        }
    }

}

