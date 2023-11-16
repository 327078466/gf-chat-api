package cn.aezo.chat_gpt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "mt_user_msg")
public class UserMsgLog extends BaseEntity{


    private static final long serialVersionUID = 1L;

    private String userId;

    private String mode;

    private String msg;

    private String modeValue;


}
