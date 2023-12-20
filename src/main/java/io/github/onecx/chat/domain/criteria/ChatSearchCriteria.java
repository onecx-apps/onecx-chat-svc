package io.github.onecx.chat.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ChatSearchCriteria {

    private String itemId;

    private String context;

    private String baseUrl;

    private String resourceUrl;

    private String appId;

    private Integer pageNumber;

    private Integer pageSize;

}
