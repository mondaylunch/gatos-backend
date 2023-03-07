package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.auth.GatosOidcUser;
import club.mondaylunch.gatos.api.repository.FlowRepository;
import club.mondaylunch.gatos.core.models.Flow;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {
    private final FlowRepository flowRepository;

    public FlowController(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
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
    public List<BasicFlowInfo> getFlows(@AuthenticationPrincipal GatosOidcUser principal) {
        return Flow.objects.get("author_id", principal.getUser().getId())
            .stream()
            .map(BasicFlowInfo::new)
            .toList();
    }

    @GetMapping(value = "{flowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFlow(@PathVariable("flowId") UUID flowId, @AuthenticationPrincipal GatosOidcUser principal) {
        return this.flowRepository.getFlow(principal.getUser(), flowId).toJson();
    }

    private record BodyAddFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PostMapping
    public BasicFlowInfo addFlow(@AuthenticationPrincipal GatosOidcUser principal, @Valid @RequestBody BodyAddFlow data) {
        Flow flow = new Flow();
        flow.setName(data.name);
        flow.setAuthorId(principal.getUser().getId());
        flow.setDescription(data.description);

        Flow.objects.insert(flow);
        return new BasicFlowInfo(flow);
    }

    private record BodyUpdateFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PatchMapping("{flowId}")
    public BasicFlowInfo updateFlow(@AuthenticationPrincipal GatosOidcUser principal, @PathVariable UUID flowId,
                           @Valid @RequestBody BodyUpdateFlow data) {
        var flow = this.flowRepository.getFlow(principal.getUser(), flowId);

        Flow partial = new Flow();
        partial.setName(data.name);
        partial.setDescription(data.description);
        partial.setGraph(null);

        var updated = Flow.objects.update(flow.getId(), partial);
        return new BasicFlowInfo(updated);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@AuthenticationPrincipal GatosOidcUser principal, @PathVariable UUID flowId) {
        var flow = this.flowRepository.getFlow(principal.getUser(), flowId);

        Flow.objects.delete(flow.getId());
    }
}
