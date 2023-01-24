package gay.oss.gatos.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gay.oss.gatos.core.models.Flow;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {

    @GetMapping("{userId}")
    public List<Flow> getFlows(@PathVariable UUID userId) {
        return Flow.objects.get("author_id", userId);
    }

    @PostMapping
    public Flow addFlow(@RequestBody Flow flow) {
        flow.setId(UUID.randomUUID());
        Flow.objects.insert(flow);
        return flow;
    }

    @PutMapping("{flowId}")
    public Flow updateFlow(@PathVariable UUID flowId, @RequestBody Flow flow) {
        return Flow.objects.update(flowId, flow);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@PathVariable UUID flowId) {
        Flow.objects.delete(flowId);
    }
}
