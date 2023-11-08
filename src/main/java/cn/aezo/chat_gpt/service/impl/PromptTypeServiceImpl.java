package cn.aezo.chat_gpt.service.impl;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.entity.PromptTypeMaster;
import cn.aezo.chat_gpt.mapper.PromptTypeMapper;
import cn.aezo.chat_gpt.service.PromptTypeService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Classname PromptTypeServiceImpl
 * @Description
 * @Date 2023/11/4
 * @Created by 陈冰峰
 */
@Service
public class PromptTypeServiceImpl implements PromptTypeService {
    @Autowired
    private PromptTypeMapper promptTypeMapper;
    @Override
    public int insert(PromptType promptType) {
        return promptTypeMapper.insert(promptType);
    }

    @Override
    public int delete(int id) {
        return promptTypeMapper.delete(id);
    }

    @Override
    public int update(PromptType promptType) {
        return promptTypeMapper.update(promptType);
    }

    @Override
    public PromptType load(int id) {
        return promptTypeMapper.load(id);
    }

    @Override
    public Map<String, Object> pageList(int offset, int pagesize) {
        List<PromptType> pageList = promptTypeMapper.pageList(offset, pagesize);
        int totalCount = promptTypeMapper.pageListCount(offset, pagesize);

        // result
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("pageList", pageList);
        result.put("totalCount", totalCount);
        return result;
    }

    @Override
    public List<PromptType> selectTop10() {
        String userId = StpUtil.getLoginIdAsString();
        List<PromptType> promptTypes1 = promptTypeMapper.selectFavorite(userId);
        List<Integer> collect = promptTypes1.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<PromptType> promptTypes = promptTypeMapper.selectTop10();
        promptTypes.stream().forEach(item ->{
            if(collect.contains(item.getId())){
                item.setIsFavorite("1");
            }else {
                item.setIsFavorite("0");
            }
        });
        return promptTypes;
    }

    @Override
    public List<PromptType> selectFavorite(String userId) {
        List<PromptType> promptTypes = promptTypeMapper.selectFavorite(userId);
        promptTypes.stream().forEach(item ->{
            item.setIsFavorite("1");
        });
        return promptTypes;
    }

    @Override
    public int saveFavoritePrompt(String userId, String id) {
        int i = promptTypeMapper.checkFavoritePrompt(userId,id);
        if(i == 0){
             i = promptTypeMapper.saveFavoritePrompt(userId, id);
        }
        // 收藏成功后 该提示收藏数加一
        PromptType promptType = new PromptType();
        promptType.setId(Integer.valueOf(id));
        PromptType load = promptTypeMapper.load(Integer.valueOf(id));
        promptType.setFavorite(Long.valueOf(load.getFavorite()) + 1 +"");
        promptTypeMapper.update(promptType);
        return i;
    }

    @Override
    public int cannelFavoritePrompt(String userId, String id) {
        return promptTypeMapper.deleteByUserId(userId,id);
    }

    @Override
    public List<PromptTypeMaster> getPromptType() {
        List<PromptTypeMaster> promptType = promptTypeMapper.getPromptType();
        // 点击量最高的放第一个常用
        List<PromptType> list = promptTypeMapper.selectHotByChoiceNum();
        promptType.stream().forEach(item ->{
            if(item.getId() == 1){ // 常用
                item.setItemList(list);
            }else {
                List<PromptType> promptById = promptTypeMapper.getPromptById(item.getId() + "");
                item.setItemList(promptById);
            }
        });
        return  promptType;
    }

    @Override
    public List<PromptType> getPromptById(String id) {
        return promptTypeMapper.getPromptById(id);
    }
}
