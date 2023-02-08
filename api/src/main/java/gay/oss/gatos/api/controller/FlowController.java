package gay.oss.gatos.api.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.core.models.Flow;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {
    private final LoginRepository userRepository;

    public FlowController(LoginRepository repository) {
        this.userRepository = repository;
    }

    @GetMapping("list")
    public List<Flow> getFlows(@RequestHeader("x-auth-token") String token) {
        var user = this.userRepository.authenticateUser(token);
        return Flow.objects.get("author_id", user.getId());
    }

    private record BodyAddFlow(
            @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PostMapping
    public Flow addFlow(@RequestHeader("x-auth-token") String token, @Valid @RequestBody BodyAddFlow data) {
        var user = this.userRepository.authenticateUser(token);

        Flow flow = new Flow();
        flow.setName(data.name);
        flow.setAuthorId(user.getId());
        flow.setDescription(data.description);

        Flow.objects.insert(flow);
        return flow;
    }

    private record BodyUpdateFlow(
            @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PatchMapping("{flowId}")
    public Flow updateFlow(@RequestHeader("x-auth-token") String token, @PathVariable UUID flowId,
            @Valid @RequestBody BodyUpdateFlow data) {
        var user = this.userRepository.authenticateUser(token);

        Flow flow = Flow.objects.get(flowId);
        if (!flow.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("User does not own flow.");
        }

        Flow partial = new Flow();
        partial.setName(data.name);
        partial.setDescription(data.description);

        return Flow.objects.update(flowId, partial);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@RequestHeader("x-auth-token") String token, @PathVariable UUID flowId) {
        var user = this.userRepository.authenticateUser(token);

        Flow flow = Flow.objects.get(flowId);
        if (!flow.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("User does not own flow.");
        }

        Flow.objects.delete(flowId);
    }
}
