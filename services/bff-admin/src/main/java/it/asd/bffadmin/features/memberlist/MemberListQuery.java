package it.asd.bffadmin.features.memberlist;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberListQuery(
        @NotNull UUID asdId,
        @NotNull UUID seasonId,
        int page,
        int size,
        String searchTerm  // optional name/CF filter
) {
    public MemberListQuery {
        if (page < 0)  page = 0;
        if (size <= 0) size = 20;
    }
}
