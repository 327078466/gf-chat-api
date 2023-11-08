package cn.aezo.chat_gpt.controller;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.entity.PromptTypeMster;
import cn.aezo.chat_gpt.service.PromptTypeService;
import cn.aezo.chat_gpt.util.Result;
import cn.dev33.satoken.stp.StpUtil;
import org.bouncycastle.cert.ocsp.Req;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @Classname PromptTypeController
 * @Description 提示词控制类
 * @Date 2023/11/4
 * @Created by 陈冰峰
 */
@RestController
@RequestMapping("/prompt/type")
public class PromptTypeController {

    @Autowired
    private PromptTypeService promptTypeService;

    /**
     * 查询最热门的提示词 排名前十
     */

    @GetMapping("/selectTop10")
    public Result selectTop10(){
        try {
            List<PromptType> list = promptTypeService.selectTop10();
            return Result.success("sucess",list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 查找当前用户收藏
     */
    @PostMapping("/selectFavorite")
    public Result selectFavorite(){
        String userId = StpUtil.getLoginIdAsString();
        try {
            List<PromptType> list = promptTypeService.selectFavorite(userId);
            return Result.success("sucess",list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }


    /**
     * 添加收藏
     */
    @PostMapping("/saveFavoritePrompt")
    public Result saveFavoritePrompt(@RequestBody Map<String, Object>parmas){
        String userId = StpUtil.getLoginIdAsString();
        String id = parmas.get("id") + "";
        try {
            int i = promptTypeService.saveFavoritePrompt(userId,id);
            return Result.success("sucess");
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 查询所有分类
     */
    @PostMapping("/getPromptType")
    public Result getPromptType(){
        try {
            List<PromptTypeMster> list = promptTypeService.getPromptType();
            return Result.success("sucess",list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 取消收藏
     */
    @PostMapping("/cannelFavoritePrompt")
    public Result cannelFavoritePrompt(@RequestBody Map<String, Object>parmas){
        String userId = StpUtil.getLoginIdAsString();
        String id = parmas.get("id") + "";
        try {
            int i = promptTypeService.cannelFavoritePrompt(userId,id);
            return Result.success("sucess");
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }


}
