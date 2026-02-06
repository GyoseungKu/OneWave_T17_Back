package org.syu_likelion.OneWave.idea;

import org.syu_likelion.OneWave.idea.dto.IdeaCreateRequest;
import org.syu_likelion.OneWave.idea.dto.IdeaResponse;
import org.syu_likelion.OneWave.user.User;
import org.syu_likelion.OneWave.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;

    public IdeaService(IdeaRepository ideaRepository, UserRepository userRepository) {
        this.ideaRepository = ideaRepository;
        this.userRepository = userRepository;
    }

    public IdeaResponse createIdea(String email, IdeaCreateRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(request.getTitle());
        idea.setProblem(request.getProblem());
        idea.setTargetCustomer(request.getTargetCustomer());
        idea.setSolution(request.getSolution());
        idea.setDifferentiation(request.getDifferentiation());
        idea.setCategory(request.getCategory());
        idea.setStage(request.getStage());

        Idea saved = ideaRepository.save(idea);
        return toResponse(saved);
    }

    public void deleteIdea(String email, Long ideaId) {
        Idea idea = ideaRepository.findByIdeaIdAndUserEmail(ideaId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        ideaRepository.delete(idea);
    }

    public List<IdeaResponse> listMyIdeas(String email) {
        return ideaRepository.findAllByUserEmailOrderByCreatedAtDesc(email)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public IdeaResponse getMyIdea(String email, Long ideaId) {
        Idea idea = ideaRepository.findByIdeaIdAndUserEmail(ideaId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        return toResponse(idea);
    }

    private IdeaResponse toResponse(Idea idea) {
        IdeaResponse response = new IdeaResponse();
        response.setIdeaId(idea.getIdeaId());
        response.setTitle(idea.getTitle());
        response.setProblem(idea.getProblem());
        response.setTargetCustomer(idea.getTargetCustomer());
        response.setSolution(idea.getSolution());
        response.setDifferentiation(idea.getDifferentiation());
        response.setCategory(idea.getCategory());
        response.setStage(idea.getStage());
        response.setCreatedAt(idea.getCreatedAt());
        return response;
    }
}
