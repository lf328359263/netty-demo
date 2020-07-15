package cn.morphling.common.keepalive;

import cn.morphling.common.OperationResult;
import lombok.Data;

@Data
public class KeepaliveOperationResult extends OperationResult {

    private final long time;

}
