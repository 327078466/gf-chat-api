package cn.aezo.chat_gpt.mapper;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.entity.PromptTypeMaster;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Version 1.0
 * @InterfaceName PromptTypeMapper
 * @Description
 * @Date 2023/11/4
 * @Created by 陈冰峰
 */

public interface PromptTypeMapper {
    /**
     * 新增
     * @author BEJSON
     * @date 2023/11/04
     **/
    int insert(PromptType promptType);

    /**
     * 刪除
     * @author BEJSON
     * @date 2023/11/04
     **/
    int delete(int id);

    /**
     * 更新
     * @author BEJSON
     * @date 2023/11/04
     **/
    int update(PromptType promptType);

    /**
     * 查询 根据主键 id 查询
     * @author BEJSON
     * @date 2023/11/04
     **/
    PromptType load(int id);

    /**
     * 查询 分页查询
     * @author BEJSON
     * @date 2023/11/04
     **/
    List<PromptType> pageList(int offset, int pagesize);

    /**
     * 查询 分页查询 count
     * @author BEJSON
     * @date 2023/11/04
     **/
    int pageListCount(int offset,int pagesize);

    List<PromptType> selectTop10();

    List<PromptType> selectFavorite(String userId);

    int deleteByUserId(@Param("userId") String userId, @Param("id") String id);

    int saveFavoritePrompt(@Param("userId") String userId, @Param("id") String id);

    int checkFavoritePrompt(@Param("userId") String userId, @Param("id") String id);

    List<PromptTypeMaster> getPromptType();

    List<PromptType> getPromptById( String id);

    List<PromptType> selectHotByChoiceNum();

}
