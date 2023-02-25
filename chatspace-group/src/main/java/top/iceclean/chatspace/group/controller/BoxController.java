package top.iceclean.chatspace.group.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.group.server.BoxService;
import top.iceclean.chatspace.infrastructure.pojo.Response;
import top.iceclean.chatspace.infrastructure.vo.ItemVO;

/**
 * 箱子区域服务
 * @author : Ice'Clean
 * @date : 2022-12-15
 */
@RestController
@RequestMapping("/zone/box")
public class BoxController {

    /** 箱子区域服务 */
    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    /**
     * 将一个箱子资源放在空间中，创建箱子区域
     *
     */
    @PostMapping("")
    public Response createBoxZone(int boxItemId, int groupId, String boxName) {
        return boxService.checkedCreateBoxZone(boxItemId, groupId, boxName);
    }

    /**
     * 查看箱子内容（由用户当前位置确认）
     * @return 箱子内容列表
     */
    @GetMapping("/list/item")
    public Response getBoxItemList() {
        return boxService.checkedGetBoxItemList();
    }

    /**
     * 获取用户随生箱子中的物品列表
     * @return 随身箱子物品列表
     */
    @GetMapping("/user/list/item")
    public Response getUserBoxItemList() {
        return boxService.getUserBoxItemList();
    }

    /**
     * 在箱子之间移动物品
     * @param boxItemId 要操作的物品项 ID
     * @param num 要移动的数量
     * @return 操作结果
     */
    @PatchMapping("/move/{boxItemId}/{num}")
    public Response moveItem(@PathVariable int boxItemId, @PathVariable int num) {
        return boxService.moveItem(boxItemId, num);
    }

    /**
     * 查询出箱子物品项对应的物品详情
     * @param boxItemId 箱子物品项 ID
     * @return 物品详情
     */
    @GetMapping("/item/detail/{boxItemId}")
    public Response getItemDetail(@PathVariable int boxItemId) {
        return boxService.getItemDetail(boxItemId);
    }

    /**
     * 开启新一轮的夺宝
     * 同时发送一条广播，根据箱子的权限发送世界广播、空间广播或者群组广播
     * @return 新一轮夺宝详情
     */
    @PatchMapping("/hunt/new")
    public Response newHunt() {
        return boxService.newHunt();
    }

    /**
     * 用户打开当前位置的宝箱，随机拿出一件物品
     * @return 结果响应
     */
    @PostMapping("/open/random")
    public Response openBoxRandom() throws InterruptedException {
        return boxService.openBoxRandom();
    }

    /**
     * 设置箱子的上锁时间，如果为 null 则立即上锁
     * @param time 上锁时间
     * @return 上锁响应
     */
    @PatchMapping("/lock")
    public Response lockBox(String time) {
        return boxService.setBoxLock(time, true);
    }

    /**
     * 设置箱子的解锁时间，如果为 null 则立即解锁
     * @param time 解锁时间
     * @return 解锁响应
     */
    @PatchMapping("/unlock")
    public Response unlockBox(String time) {
        return boxService.setBoxLock(time, false);
    }
//
//
//
//    @PostMapping("/open/catch")
//    public Response openBoxCatch(int zoneId, int index, int num) {
//
//    }
}
