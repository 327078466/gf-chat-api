package cn.aezo.chat_gpt.service;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.entity.PromptTypeMster;

import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @InterfaceName PromptTypeService
 * @Description
 * @Date 2023/11/4
 * @Created by 陈冰峰
 */

public interface PromptTypeService {
    /**
     * 新增
     */
    public int insert(PromptType promptType);

    /**
     * 删除
     */
    public int delete(int id);

    /**
     * 更新
     */
    public int update(PromptType promptType);

    /**
     * 根据主键 id 查询
     */
    public PromptType load(int id);

    /**
     * 分页查询
     */
    public Map<String,Object> pageList(int offset, int pagesize);

    List<PromptType> selectTop10();

    List<PromptType> selectFavorite(String userId);

    int saveFavoritePrompt(String userId, String id);

    int cannelFavoritePrompt(String userId, String id);

    List<PromptTypeMster> getPromptType();

    List<PromptType> getPromptById(String id);
}
