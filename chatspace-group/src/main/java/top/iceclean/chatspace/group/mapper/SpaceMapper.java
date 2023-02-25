package top.iceclean.chatspace.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.iceclean.chatspace.infrastructure.po.Space;

/**
 * 空间元数据的操作
 * @author : Ice'Clean
 * @date : 2022-10-27
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {
}
