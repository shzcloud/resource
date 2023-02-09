package shz.resource.server;

/**
 * 检查资源结果
 */
public final class CheckResult {
    /**
     * 检查资源后判断是否需要上传
     */
    boolean upload;
    /**
     * 文件偏移量,只有当upload为true时才有效
     * 若>0则文件偏移该值进行上传,否则上传整个文件
     */
    long offset;
    /**
     * 文件id
     */
    Long id;
    /**
     * 路径
     */
    String path;

    public boolean isUpload() {
        return upload;
    }

    public long getOffset() {
        return offset;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
}
