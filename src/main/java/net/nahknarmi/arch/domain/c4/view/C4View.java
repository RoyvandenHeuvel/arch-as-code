package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Tag;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class C4View {
    private String key;
    @NonNull
    private String name;
    @NonNull
    private String description;
    private List<C4Tag> tags = emptyList();
    private List<C4ViewReference> references = emptyList();

    public String getKey() {
        return Optional.ofNullable(key).orElse(getName() + "-" + getDescription());
    }
}
