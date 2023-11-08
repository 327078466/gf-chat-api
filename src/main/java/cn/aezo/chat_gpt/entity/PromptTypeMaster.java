package cn.aezo.chat_gpt.entity;

import lombok.*;

import java.util.List;

/**
 * @Version 1.0
 * @Classname PromptTypeMaster
 * @Description
 * @Date 2023/11/5
 * @Created by 陈冰峰
 */

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Data
public class PromptTypeMaster {

    private Integer id;

    private String name;

    private List<PromptType> itemList;


}
