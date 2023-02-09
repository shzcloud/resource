package shz.resource.server;

import shz.core.model.PageInfo;
import shz.resource.entity.SysResource;
import shz.resource.vo.QueryResourceVo;
import shz.spring.model.PageVo;

import java.util.Collection;
import java.util.List;

public interface ResourceServer {
    /**
     * 检查资源,在上传之前调用该方法判断是否需要上传或者是全传还是续传
     */
    CheckResult check(SysResource resource);

    /**
     * 保存资源文件
     */
    void save(SysResource resource);

    SysResource getById(Long id);

    SysResource getByMd5(String md5);

    List<SysResource> getByIds(Collection<Long> ids);

    /**
     * 下载资源
     */
    void download(Long id, boolean useSecondPath, Long offset);

    /**
     * 删除库及文件
     */
    void deleteByIds(Collection<Long> ids);

    /**
     * 清除库(不包含存在的文件)
     */
    void clearTable(Collection<Long> ids);

    /**
     * 清除指定类型的文件(不包含库中存在的文件)
     */
    void clearFile(Collection<String> types);

    PageInfo<SysResource> page(PageVo<QueryResourceVo, SysResource> pageVo);
}
