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

    private record BodyAddFlow(
            String name) {
    };

    @PostMapping
    public Flow addFlow(@RequestBody BodyAddFlow data) {
        Flow flow = new Flow();
        flow.setName(data.name);

        Flow.objects.insert(flow);
        return flow;
    }

    private record BodyUpdateFlow(
            String name) {
    };

    @PutMapping("{flowId}")
    public Flow updateFlow(@PathVariable UUID flowId, @RequestBody BodyUpdateFlow data) {
        Flow partial = new Flow();
        partial.setName(data.name);

        return Flow.objects.update(flowId, partial);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@PathVariable UUID flowId) {
        Flow.objects.delete(flowId);
    }
}
