package io.github.onecx.chat.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ChatSearchCriteria {

    private String type;

    private String topic;

    private String summary;

    private String appId;

    private Integer pageNumber;

    private Integer pageSize;

}
