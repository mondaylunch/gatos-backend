package club.mondaylunch.gatos.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.repository.UserRepository;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.data.SettingWidgets;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/data-types")
public class DataTypesController {
    private final UserRepository userRepository;

    @Autowired
    public DataTypesController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record DataTypeInfo(String name, SettingWidgets.Widget widget) {

    }

    @GetMapping
    public List<DataTypeInfo> getDataTypes(@RequestHeader(name = "x-user-email") String userEmail) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        return DataType.REGISTRY.getEntries().stream()
            .filter(kv -> !(kv.getValue() instanceof ListDataType<?> || kv.getValue() instanceof OptionalDataType<?>))
            .map(kv -> new DataTypeInfo(kv.getKey(), SettingWidgets.get(kv.getValue()).createWidget(user)))
            .toList();
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
