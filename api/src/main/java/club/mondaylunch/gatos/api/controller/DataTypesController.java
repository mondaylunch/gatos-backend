package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;

@RestController
@RequestMapping("api/v1/data-types")
public class DataTypesController {

    @GetMapping
    public List<String> getDataTypes() {
        return DataType.REGISTRY.getEntries().stream()
            .map(Map.Entry::getKey).toList();
    }

    public record ConversionInfo(String from, String to) {
    }

    @GetMapping("/conversions")
    public List<ConversionInfo> getConversions() {
        return Conversions.getAllConversions().stream()
            .map(c -> new ConversionInfo(DataType.REGISTRY.getName(c.a()).orElse(null), DataType.REGISTRY.getName(c.b()).orElse(null)))
            .filter(c -> c.from() != null && c.to() != null)
            .toList();
    }
}
