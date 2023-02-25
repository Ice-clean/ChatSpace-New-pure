package top.iceclean.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import top.iceclean.chatspace.infrastructure.po.User;

import java.util.List;

/**
 * 用户远程调用接口
 * @author : Ice'Clean
 * @date : 2022-10-07
 */
@FeignClient(value = "chatspace-user", path = "/space")
public interface UserClient {
    /**
     * 根据用户 ID 列表获取所有用户
     * @param userIdList 用户 ID 列表
     * @return 用户集合
     */
    @PostMapping("/list")
    List<User> getUserList(@RequestBody List<Integer> userIdList);

    /**
     * 通过用户 ID 获取用户对象
     * @param userId 用户 ID
     * @return 用户对象
     */
    @GetMapping("/id")
    User getUserById(@RequestParam int userId);
}
