package cn.vvbbnn00.canteen.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础列表响应类，用于返回列表数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BasicListResponse extends BasicResponse {
    private int total;
    private Object list;
    private int currentPage;
    private int pageSize;
}