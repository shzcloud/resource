package shz.resource.entity;

import shz.orm.annotation.Column;
import shz.orm.annotation.Table;

import java.time.LocalDateTime;
import java.util.Arrays;

@Table("sys_resource")
public class SysResource {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private String type;
    private String filename;
    private String extension;
    private String md5;
    private String path;
    private Long size;
    private Long offset;

    @Column(exist = false)
    private byte[] data;
    @Column(exist = false)
    private boolean useSecondPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isUseSecondPath() {
        return useSecondPath;
    }

    public void setUseSecondPath(boolean useSecondPath) {
        this.useSecondPath = useSecondPath;
    }

    @Override
    public String toString() {
        return "SysResource{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createBy=" + createBy +
                ", updateBy=" + updateBy +
                ", type='" + type + '\'' +
                ", filename='" + filename + '\'' +
                ", extension='" + extension + '\'' +
                ", md5='" + md5 + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", offset=" + offset +
                ", data=" + Arrays.toString(data) +
                ", useSecondPath=" + useSecondPath +
                '}';
    }
}
