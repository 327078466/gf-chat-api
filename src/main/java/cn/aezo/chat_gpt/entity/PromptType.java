package cn.aezo.chat_gpt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * @Version 1.0
 * @Classname PromptType
 * @Description
 * @Date 2023/11/4
 * @Created by 陈冰峰
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode
@TableName(value = "mt_prompt_type"
        , excludeProperty = {"isFavorite"}
)
/**
 * @description mt_prompt_type
 * @author BEJSON
 * @date 2023-11-04
 */
public class PromptType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 分类
     */
    private String type;

    /**
     * 限定角色语句
     */
    private String contentZh;

    /**
     * 限定角色语句
     */
    private String contentEn;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 收藏数
     */
    private String favorite;

    /**
     * 使用数量
     */
    private String choiceNum;

    private String image;

    private String isFavorite;



}
