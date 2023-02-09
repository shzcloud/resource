package shz.resource.client;

import org.springframework.web.multipart.MultipartFile;
import shz.core.model.PageInfo;
import shz.resource.entity.SysResource;
import shz.resource.vo.QueryResourceVo;
import shz.spring.model.PageVo;

import java.util.Collection;
import java.util.List;

public interface ResourceClient {
    void upload(SysResource resource);

    SysResource upload(ResourceType type, MultipartFile file, boolean useSecondPath);

    List<SysResource> upload(ResourceType type, MultipartFile[] files, boolean useSecondPath);

    SysResource getById(Long id);

    SysResource getByMd5(String md5);

    List<SysResource> getByIds(Collection<Long> ids);

    void download(Long id, boolean useSecondPath, Long offset);

    void deleteByIds(Collection<Long> ids);

    void clearTable(Collection<Long> ids);

    void clearFile(Collection<String> types);

    PageInfo<SysResource> page(PageVo<QueryResourceVo, SysResource> pageVo);
}
