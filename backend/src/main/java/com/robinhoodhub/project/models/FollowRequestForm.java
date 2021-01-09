package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowRequestForm {
    public String target;
    public Boolean unfollow;
}
