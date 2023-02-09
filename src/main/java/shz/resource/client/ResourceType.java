package shz.resource.client;

import shz.core.enums.NameCodeEnum;
import shz.resource.entity.SysResource;

public interface ResourceType extends NameCodeEnum<String> {
    default void check(SysResource resource) {
    }
}
