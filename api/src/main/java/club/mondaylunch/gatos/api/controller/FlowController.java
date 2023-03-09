package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.repository.FlowRepository;
import club.mondaylunch.gatos.api.repository.UserRepository;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {
    private final FlowRepository flowRepository;
    private final UserRepository userRepository;

    @Autowired
    public FlowController(FlowRepository flowRepository, UserRepository userRepository) {
        this.flowRepository = flowRepository;
        this.userRepository = userRepository;
    }

    private record BasicFlowInfo(
        @JsonProperty("_id") UUID id,
        String name,
        String description,
        @JsonProperty("author_id") UUID authorId
    ) {
        BasicFlowInfo(Flow flow) {
            this(flow.getId(), flow.getName(), flow.getDescription(), flow.getAuthorId());
        }
    }

    @GetMapping("list")
    public List<BasicFlowInfo> getFlows(@RequestHeader(name = "x-user-email") String userEmail) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        return Flow.objects.get("author_id", user.getId())
            .stream()
            .map(BasicFlowInfo::new)
            .toList();
    }

    @GetMapping(value = "{flowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFlow(@RequestHeader(name = "x-user-email") String userEmail, @PathVariable("flowId") UUID flowId) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        return this.flowRepository.getFlow(user, flowId).toJson();
    }

    private record BodyAddFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PostMapping
    public BasicFlowInfo addFlow(@RequestHeader(name = "x-user-email") String userEmail, @Valid @RequestBody BodyAddFlow data) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        Flow flow = new Flow();
        flow.setName(data.name);
        flow.setAuthorId(user.getId());
        flow.setDescription(data.description);

        Flow.objects.insert(flow);
        return new BasicFlowInfo(flow);
    }

    private record BodyUpdateFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PatchMapping("{flowId}")
    public BasicFlowInfo updateFlow(@RequestHeader(name = "x-user-email") String userEmail, @PathVariable UUID flowId,
                                    @Valid @RequestBody BodyUpdateFlow data) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);

        Flow partial = new Flow();
        partial.setName(data.name);
        partial.setDescription(data.description);
        partial.setGraph(null);

        var updated = Flow.objects.update(flow.getId(), partial);
        return new BasicFlowInfo(updated);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@RequestHeader(name = "x-user-email") String userEmail, @PathVariable UUID flowId) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);

        Flow.objects.delete(flow.getId());
    }
}
