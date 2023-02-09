package shz.resource.vo;

import shz.orm.annotation.OrderBy;
import shz.orm.annotation.Where;
import shz.orm.enums.Condition;

import java.time.LocalDateTime;
import java.util.List;

public class QueryResourceVo {
    @Where(condition = Condition.BETWEEN)
    private List<LocalDateTime> createTime;
    @Where(condition = Condition.BETWEEN)
    private List<LocalDateTime> updateTime;
    @Where
    private Long createBy;
    @Where
    private Long updateBy;
    @Where
    private String type;
    @Where(condition = Condition.LIKE)
    private String filename;
    @Where
    private String extension;
    @Where(condition = Condition.LIKE)
    private String path;
    @Where(condition = Condition.BETWEEN)
    private List<Long> size;
    @Where(condition = Condition.BETWEEN)
    private List<Long> offset;

    @OrderBy("createTime")
    private Boolean orderByCreateTime;
    @OrderBy("updateTime")
    private Boolean orderByUpdateTime;
    @OrderBy("type")
    private Boolean orderByType;
    @OrderBy("size")
    private Boolean orderBySize;
    @OrderBy("offset")
    private Boolean orderByOffset;

    public List<LocalDateTime> getCreateTime() {
        return createTime;
    }

    public void setCreateTime(List<LocalDateTime> createTime) {
        this.createTime = createTime;
    }

    public List<LocalDateTime> getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(List<LocalDateTime> updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Long> getSize() {
        return size;
    }

    public void setSize(List<Long> size) {
        this.size = size;
    }

    public List<Long> getOffset() {
        return offset;
    }

    public void setOffset(List<Long> offset) {
        this.offset = offset;
    }

    public Boolean getOrderByCreateTime() {
        return orderByCreateTime;
    }

    public void setOrderByCreateTime(Boolean orderByCreateTime) {
        this.orderByCreateTime = orderByCreateTime;
    }

    public Boolean getOrderByUpdateTime() {
        return orderByUpdateTime;
    }

    public void setOrderByUpdateTime(Boolean orderByUpdateTime) {
        this.orderByUpdateTime = orderByUpdateTime;
    }

    public Boolean getOrderByType() {
        return orderByType;
    }

    public void setOrderByType(Boolean orderByType) {
        this.orderByType = orderByType;
    }

    public Boolean getOrderBySize() {
        return orderBySize;
    }

    public void setOrderBySize(Boolean orderBySize) {
        this.orderBySize = orderBySize;
    }

    public Boolean getOrderByOffset() {
        return orderByOffset;
    }

    public void setOrderByOffset(Boolean orderByOffset) {
        this.orderByOffset = orderByOffset;
    }
}
