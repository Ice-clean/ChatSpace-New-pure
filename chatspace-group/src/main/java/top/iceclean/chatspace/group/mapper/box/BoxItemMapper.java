package top.iceclean.chatspace.group.mapper.box;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.iceclean.chatspace.infrastructure.po.BoxItem;
import top.iceclean.chatspace.infrastructure.po.Item;
import top.iceclean.chatspace.infrastructure.vo.BoxVO;

import java.util.List;

/**
 * @author : Ice'Clean
 * @date : 2022-12-13
 */
@Mapper
public interface BoxItemMapper extends BaseMapper<BoxItem> {
    /**
     * 查询出箱子的物品列表
     * @param boxId 箱子 ID
     * @return 箱子物品列表
     */
    @Select("select box_item.id as box_item_id, box_idx, item_name, item_extend->>'$.name' as alias_name, amount " +
            "from t_box_item box_item " +
            "join t_item item on box_item.item_id = item.id " +
            "join t_meta_item meta on item.meta_item_id = meta.id " +
            "where box_id = #{boxId} and box_item.amount > 0")
    List<BoxVO.BoxItem> getBoxItemList(int boxId);

    /**
     * 查询出箱子物品项对应的物品详情
     * @param boxItemId 箱子物品项 ID
     * @return 物品详情
     */
    @Select("select item.id as id, item.meta_item_id as meta_item_id, item_name, item_cate, item_intro, item_extend " +
            "from t_box_item box_item " +
            "join t_item item on box_item.item_id = item.id " +
            "join t_meta_item meta on item.meta_item_id = meta.id " +
            "where box_item.id = #{boxItemId}")
    Item getItemDetail(int boxItemId);

    /**
     * 查询出指定箱子中指定类型的物品列表
     * @param boxId 箱子 ID
     * @param metaItemId 物品元类型（即 type）
     * @return 物品列表
     */
    @Select("select item.id as id, item.meta_item_id as meta_item_id, item_name, item_cate, item_intro, item_extend " +
            "from t_box_item box_item " +
            "join t_item item on box_item.item_id = item.id " +
            "join t_meta_item meta on item.meta_item_id = meta.id " +
            "where box_item.id = #{boxItemId}")
    List<Item> getItemsByType(int boxId, int metaItemId);

    /**
     * 指定获取某个箱子某个位置的物品
     * @param boxId 箱子 ID
     * @param boxIdx 箱子内的位置索引
     * @return 箱子物品项
     */
    @Select("select id, box_idx, item_name, amount " +
            "from t_box_item join t_item using(item_id) " +
            "where box_id = #{boxId} and box_idx = #{boxIdx} " +
            "and amount > 0 limit 1")
    BoxVO.BoxItem getBoxItem(@Param("boxId") int boxId,
                             @Param("boxIdx") int boxIdx);
}
